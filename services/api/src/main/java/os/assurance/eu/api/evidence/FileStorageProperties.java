package os.assurance.eu.api.evidence;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assurance.storage")
public record FileStorageProperties(
    boolean enabled,
    String bucket,
    String region,
    String endpoint,
    String accessKeyId,
    String secretAccessKey,
    /**
     * Path-style S3 URLs. Required for MinIO and many S3-compatible endpoints.
     * When null, defaults to true whenever a custom {@code endpoint} is set.
     */
    Boolean pathStyle
) {
    public FileStorageProperties {
        if (bucket == null) bucket = "eu-ai-assurance-evidence";
        if (region == null) region = "eu-west-1";
    }

    public boolean pathStyleEnabled() {
        if (pathStyle != null) {
            return pathStyle;
        }
        return endpoint != null && !endpoint.isBlank();
    }
}
