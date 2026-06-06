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
import jakarta.annotation.PreDestroy;

public class DjlSentenceEmbeddingProvider implements EvidenceEmbeddingProvider {
    private final ZooModel<NDList, NDList> model;
    private final HuggingFaceTokenizer tokenizer;

    public DjlSentenceEmbeddingProvider() throws Exception {
        Criteria<NDList, NDList> criteria = Criteria.builder()
            .setTypes(NDList.class, NDList.class)
            .optModelUrls("djl://ai.djl.huggingface.onnxruntime/all-MiniLM-L6-v2")
            .optEngine("OnnxRuntime")
            .build();
        this.model = criteria.loadModel();
        this.tokenizer = HuggingFaceTokenizer.newInstance("sentence-transformers/all-MiniLM-L6-v2");
    }

    @Override
    public String name() {
        return "djl-sentence";
    }

    @Override
    public String embed(String text) {
        try (Predictor<NDList, NDList> predictor = model.newPredictor();
             NDManager manager = NDManager.newBaseManager()) {
            Encoding encoding = tokenizer.encode(text, true, true);
            long[] ids = encoding.getIds();
            long[] mask = encoding.getAttentionMask();
            NDArray inputIds = manager.create(ids, new Shape(1, ids.length));
            NDArray attentionMask = manager.create(mask, new Shape(1, mask.length));
            NDArray tokenTypeIds = manager.zeros(new Shape(1, ids.length), DataType.INT64);
            NDList output = predictor.predict(new NDList(inputIds, attentionMask, tokenTypeIds));
            NDArray tokenEmbeddings = output.get(0);
            NDArray maskExpanded = attentionMask.reshape(1, mask.length, 1)
                .broadcast(tokenEmbeddings.getShape())
                .toType(DataType.FLOAT32, false);
            NDArray sumEmbeddings = tokenEmbeddings.mul(maskExpanded).sum(new int[]{1});
            NDArray sumMask = maskExpanded.sum(new int[]{1}).clip(1e-9f, Float.MAX_VALUE);
            NDArray pooled = sumEmbeddings.div(sumMask);
            NDArray norm = pooled.norm(new int[]{1}, true).clip(1e-9f, Float.MAX_VALUE);
            NDArray normalized = pooled.div(norm);
            float[] vector = normalized.toFloatArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < vector.length; i++) {
                if (i > 0) sb.append(',');
                sb.append(vector[i]);
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
        tokenizer.close();
    }
}
