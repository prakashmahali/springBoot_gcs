package com.example.demo;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

@Component
public class FileCopyTask {

    @Value("${gcs.bucket.name}")
    private String bucketName;

    @Value("${gcs.file.name}")
    private String gcsFileName;

    @Value("${pvc.file.path}")
    private String pvcFilePath;

    @Scheduled(cron = "0 0 23 * * ?")
    public void copyFile() {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Blob blob = storage.get(bucketName, gcsFileName);

        if (blob == null) {
            System.err.println("No such file exists in GCS.");
            return;
        }

        File file = new File(pvcFilePath);
        try (FileOutputStream fos = new FileOutputStream(file);
             var readableByteChannel = Channels.newChannel(blob.reader())) {
            fos.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            System.out.println("File copied successfully.");
        } catch (IOException e) {
            System.err.println("Error copying file: " + e.getMessage());
        }
    }
}
