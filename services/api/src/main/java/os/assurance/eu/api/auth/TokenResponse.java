package os.assurance.eu.api.auth;

public record TokenResponse(String accessToken, String refreshToken, long expiresIn) {}