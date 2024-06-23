export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your/service-account-file.json"
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class GoogleCloudStorageService {

    private final Storage storage;

    public GoogleCloudStorageService() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(System.getenv("GOOGLE_APPLICATION_CREDENTIALS")));
        this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    public void uploadFile(String bucketName, String localFilePath, String destinationBlobName) throws IOException {
        Path path = Paths.get(localFilePath);
        byte[] data = Files.readAllBytes(path);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, destinationBlobName).build();
        storage.create(blobInfo, data);
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class UploadController {

    @Autowired
    private GoogleCloudStorageService googleCloudStorageService;

    @GetMapping("/upload")
    public String uploadFile(@RequestParam String localFilePath, @RequestParam String bucketName, @RequestParam String destinationBlobName) {
        try {
            googleCloudStorageService.uploadFile(bucketName, localFilePath, destinationBlobName);
            return "File uploaded successfully!";
        } catch (IOException e) {
            return "Failed to upload file: " + e.getMessage();
        }
    }
}


curl "http://localhost:8080/upload?localFilePath=/path/to/your/file.csv&bucketName=your-bucket-name&destinationBlobName=your-destination-blob-name"
