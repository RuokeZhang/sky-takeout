package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.alioss")
@Data
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public void printProperties() {
        System.out.println("Endpoint: " + endpoint);
        System.out.println("Access Key ID: " + accessKeyId);
        System.out.println("Access Key Secret: " + accessKeySecret);
        System.out.println("Bucket Name: " + bucketName);
    }

}
