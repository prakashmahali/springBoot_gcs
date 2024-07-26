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

    @Value("${gcs.destination.path}")
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
