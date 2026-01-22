package com.cloudstorage.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // ✅ REQUIRED for auto-clean trash
public class CloudMetaStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudMetaStorageApplication.class, args);
    }
}
