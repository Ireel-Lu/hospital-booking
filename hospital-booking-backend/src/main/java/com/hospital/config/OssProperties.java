package com.hospital.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {
    private String endpoint;
    private String bucket;
    private String accessKeyId;
    private String accessKeySecret;
    private String avatarDir = "avatar";
    private String caseDir = "case";
    private int maxAttempts = 3;
    private long retryIntervalMs = 500;
    private int connectionTimeoutMs = 10000;
    private int socketTimeoutMs = 30000;
    private long signedUrlExpireSeconds = 3600;
}
