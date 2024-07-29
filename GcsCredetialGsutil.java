package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class GoogleCloudConfig {

    @Bean
    public Storage googleStorage() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("/path/to/key.json"))
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }
}


package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class GsutilService {

    @Value("${gcs.source.path}")
    private String sourcePath;

    @Value("${local.destination.path}")
    private String destinationPath;

    public void copyFileUsingGsutil() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("gsutil", "cp", sourcePath, destinationPath);

        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        
        if (exitCode == 0) {
            System.out.println("File copied successfully.");
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println(line);
                }
            }
            throw new RuntimeException("Failed to copy file.");
        }
    }
}

package com.example.demo.controller;

import com.example.demo.service.GsutilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class GsutilController {

    @Autowired
    private GsutilService gsutilService;

    @GetMapping("/copy-file")
    public String copyFile() {
        try {
            gsutilService.copyFileUsingGsutil();
            return "File copied successfully.";
        } catch (IOException | InterruptedException e) {
            return "Failed to copy file: " + e.getMessage();
        }
    }
}
