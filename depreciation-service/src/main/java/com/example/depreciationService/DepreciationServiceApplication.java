package com.example.depreciationService;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableBatchProcessing
public class DepreciationServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(DepreciationServiceApplication.class, args);
	}

}
