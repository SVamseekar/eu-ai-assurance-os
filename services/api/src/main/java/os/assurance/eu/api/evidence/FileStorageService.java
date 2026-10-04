package os.assurance.eu.api.evidence;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final FileStorageProperties props;
    private final S3Client s3;
    private final S3Presigner presigner;

    public FileStorageService(FileStorageProperties props) {
        this.props = props;
        if (props.enabled()) {
            var credProvider = (props.accessKeyId() != null && !props.accessKeyId().isBlank())
                ? StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(props.accessKeyId(), props.secretAccessKey()))
                : DefaultCredentialsProvider.create();
            var clientBuilder = S3Client.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(credProvider)
                .forcePathStyle(props.pathStyleEnabled());
            if (props.endpoint() != null && !props.endpoint().isBlank()) {
                clientBuilder.endpointOverride(URI.create(props.endpoint()));
            }
            this.s3 = clientBuilder.build();
            var presignerBuilder = S3Presigner.builder()
                .region(Region.of(props.region()))
                .credentialsProvider(credProvider)
                .serviceConfiguration(
                    S3Configuration.builder()
                        .pathStyleAccessEnabled(props.pathStyleEnabled())
                        .build());
            if (props.endpoint() != null && !props.endpoint().isBlank()) {
                presignerBuilder.endpointOverride(URI.create(props.endpoint()));
            }
            this.presigner = presignerBuilder.build();
        } else {
            this.s3 = null;
            this.presigner = null;
        }
    }

    public String upload(String key, InputStream content, long contentLength, String contentType) {
        if (!props.enabled() || s3 == null) {
            // When storage is disabled, return a synthetic s3:// URI as a stub
            return "s3://" + props.bucket() + "/" + key;
        }
        s3.putObject(PutObjectRequest.builder()
            .bucket(props.bucket())
            .key(key)
            .contentType(contentType != null ? contentType : "application/octet-stream")
            .build(), RequestBody.fromInputStream(content, contentLength));
        log.info("Uploaded {} bytes to s3://{}/{}", contentLength, props.bucket(), key);
        return "s3://" + props.bucket() + "/" + key;
    }

    /** Deletes every object whose key starts with {@code prefix}; a no-op when storage is disabled. */
    public void deletePrefix(String prefix) {
        if (!props.enabled() || s3 == null) {
            return;
        }
        var listing = s3.listObjectsV2Paginator(ListObjectsV2Request.builder()
            .bucket(props.bucket()).prefix(prefix).build());
        for (var page : listing) {
            if (page.contents().isEmpty()) {
                continue;
            }
            var ids = page.contents().stream()
                .map(o -> ObjectIdentifier.builder().key(o.key()).build())
                .toList();
            s3.deleteObjects(DeleteObjectsRequest.builder().bucket(props.bucket())
                .delete(Delete.builder().objects(ids).quiet(true).build()).build());
        }
        log.info("Deleted stored objects under s3://{}/{}", props.bucket(), prefix);
    }

    public InputStream download(String bucket, String key) {
        if (!props.enabled() || s3 == null) {
            throw new UnsupportedOperationException("S3 storage not enabled");
        }
        return s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
    }

    public String presignedUrl(String bucket, String key) {
        if (!props.enabled() || presigner == null) return null;
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofHours(1))
            .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
            .build())
            .url().toString();
    }
}
