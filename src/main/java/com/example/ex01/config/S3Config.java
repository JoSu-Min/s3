package com.example.ex01.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
    @Value("${s3.access-key}")
    private String accessKey;

    @Value("${s3.secret-key}")
    private String secretKey;
    @Bean
    public AmazonS3 amazonS3() {
        System.out.println(accessKey +"-==="+ secretKey);

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3Client.builder()
                .withRegion(Regions.AP_NORTHEAST_2)  // 사용하는 리전으로 변경ap-northeast-2
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }
}


