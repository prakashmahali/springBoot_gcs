package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class FileCopyTask {

    private static final String SOURCE_PATH = "/path/to/source/file.txt";
    private static final String DESTINATION_PATH = "/path/to/destination/file.txt";

    @Scheduled(cron = "0 0 23 * * ?")
    public void copyFile() {
        try {
            Path source = new File(SOURCE_PATH).toPath();
            Path destination = new File(DESTINATION_PATH).toPath();
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied successfully.");
        } catch (IOException e) {
            System.err.println("Error copying file: " + e.getMessage());
        }
    }
}
