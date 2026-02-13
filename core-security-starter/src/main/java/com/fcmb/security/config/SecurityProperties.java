package com.fcmb.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security.jwt")
public class SecurityProperties {
    private String secretKey;
    private long expirationMs;
    private String issuer;
    private String headerName;
    private String tokenPrefix;
    private boolean enableLogging;
}
