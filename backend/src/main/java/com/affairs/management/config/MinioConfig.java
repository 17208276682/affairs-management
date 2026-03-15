package com.affairs.management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置（预留，暂不实现）
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    // TODO: 后续实现 MinIO 客户端 Bean
    // @Bean
    // @ConditionalOnProperty(prefix = "minio", name = "endpoint")
    // public MinioClient minioClient() {
    //     return MinioClient.builder()
    //             .endpoint(endpoint)
    //             .credentials(accessKey, secretKey)
    //             .build();
    // }
}
