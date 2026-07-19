package os.assurance.eu.api.evidence;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import jakarta.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DjlSentenceEmbeddingProvider implements EvidenceEmbeddingProvider {

    private static final String HF_BASE =
        "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main";
    private static final String MODEL_URL = HF_BASE + "/onnx/model.onnx";
    private static final String TOKENIZER_URL = HF_BASE + "/tokenizer.json";

    private final ZooModel<NDList, NDList> model;
    private final HuggingFaceTokenizer tokenizer;

    public DjlSentenceEmbeddingProvider() throws Exception {
        Path modelDir = Files.createTempDirectory("all-minilm-l6-v2");
        Path modelFile = modelDir.resolve("model.onnx");
        Path tokenizerFile = modelDir.resolve("tokenizer.json");
        HttpClient client = HttpClient.newBuilder()
            .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
            .build();
        downloadIfMissing(client, MODEL_URL, modelFile, "ONNX model");
        // Load tokenizer from an absolute file path. Passing a bare HuggingFace repo id
        // breaks inside some container runtimes (RelativeUrlWithoutBase).
        downloadIfMissing(client, TOKENIZER_URL, tokenizerFile, "tokenizer.json");

        Criteria<NDList, NDList> criteria = Criteria.builder()
            .setTypes(NDList.class, NDList.class)
            .optModelPath(modelDir)
            .optModelName("model")
            .optEngine("OnnxRuntime")
            .optTranslator(new PassThroughTranslator())
            .build();
        this.model = criteria.loadModel();
        this.tokenizer = HuggingFaceTokenizer.builder()
            .optTokenizerPath(tokenizerFile)
            .build();
    }

    private static void downloadIfMissing(HttpClient client, String url, Path dest, String label)
            throws Exception {
        if (Files.exists(dest) && Files.size(dest) > 0) {
            return;
        }
        HttpResponse<Path> resp = client.send(
            HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
            HttpResponse.BodyHandlers.ofFile(dest));
        if (resp.statusCode() != 200) {
            throw new IllegalStateException(
                "Failed to download " + label + " from " + url + ": HTTP " + resp.statusCode());
        }
    }

    @Override
    public String name() {
        return "djl-sentence";
    }

    @Override
    public String embed(String text) {
        try (Predictor<NDList, NDList> predictor = model.newPredictor();
             NDManager manager = NDManager.newBaseManager("OnnxRuntime")) {
            Encoding encoding = tokenizer.encode(text, true, true);
            long[] ids = encoding.getIds();
            long[] mask = encoding.getAttentionMask();
            int seqLen = ids.length;

            NDArray inputIds = manager.create(ids, new Shape(1, seqLen));
            NDArray attentionMask = manager.create(mask, new Shape(1, seqLen));
            NDArray tokenTypeIds = manager.zeros(new Shape(1, seqLen), DataType.INT64);
            NDList output = predictor.predict(new NDList(inputIds, attentionMask, tokenTypeIds));

            // output.get(0) is token_embeddings: shape [1, seqLen, hiddenSize]
            // Copy to float[] immediately — ONNX NDArrays don't support tensor math ops
            float[] tokenEmbFlat = output.get(0).toFloatArray();
            int hiddenSize = tokenEmbFlat.length / seqLen;

            // Attention-mask mean pooling in pure Java
            float[] pooled = new float[hiddenSize];
            float maskSum = 0f;
            for (int t = 0; t < seqLen; t++) {
                if (mask[t] == 0) continue;
                maskSum += 1f;
                for (int h = 0; h < hiddenSize; h++) {
                    pooled[h] += tokenEmbFlat[t * hiddenSize + h];
                }
            }
            if (maskSum < 1e-9f) maskSum = 1e-9f;
            for (int h = 0; h < hiddenSize; h++) {
                pooled[h] /= maskSum;
            }

            // L2 normalise
            float norm = 0f;
            for (float v : pooled) norm += v * v;
            norm = (float) Math.sqrt(norm);
            if (norm < 1e-9f) norm = 1e-9f;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hiddenSize; i++) {
                if (i > 0) sb.append(',');
                sb.append(pooled[i] / norm);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Embedding failed", e);
        }
    }

    @Override
    public double similarity(String left, String right) {
        float[] l = parse(left);
        float[] r = parse(right);
        double dot = 0;
        for (int i = 0; i < Math.min(l.length, r.length); i++) dot += l[i] * r[i];
        return dot;
    }

    private float[] parse(String embedding) {
        String[] parts = embedding.split(",");
        float[] v = new float[parts.length];
        for (int i = 0; i < parts.length; i++) v[i] = Float.parseFloat(parts[i]);
        return v;
    }

    @PreDestroy
    public void close() {
        model.close();
    }

    private static final class PassThroughTranslator implements Translator<NDList, NDList> {
        @Override
        public NDList processInput(TranslatorContext ctx, NDList input) {
            return input;
        }
        @Override
        public NDList processOutput(TranslatorContext ctx, NDList output) {
            return output;
        }
        @Override
        public Batchifier getBatchifier() {
            return null;
        }
    }
}
