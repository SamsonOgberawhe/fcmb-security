package com.fcmb.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security.jwt")
public class SecurityProperties {
    private String secretKey = "defaultSecretKeyForJWTSigningAndValidationPleaseChangeThis1234567890";
    private long expirationMs = 86400000L;
    private String issuer = "spring-security-starter";
    private String headerName = "Authorization";
    private String tokenPrefix = "Bearer ";
    private boolean enableLogging = true;
}
