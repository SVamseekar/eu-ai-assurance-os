package os.assurance.eu.api.evidence;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assurance.storage")
public record FileStorageProperties(
    boolean enabled,
    String bucket,
    String region,
    String endpoint,
    String accessKeyId,
    String secretAccessKey
) {
    public FileStorageProperties {
        if (bucket == null) bucket = "eu-ai-assurance-evidence";
        if (region == null) region = "eu-west-1";
    }
}
