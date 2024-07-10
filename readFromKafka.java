import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class FileHandlerService {

    private static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L; // 1 GB in bytes
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";
    private BufferedWriter writer;
    private File currentFile;
    private int fileCounter = 0;

    @Value("${file.directory.path}")
    private String directoryPath;

    @Value("${file.name.prefix}")
    private String fileNamePrefix;

    @Value("${gcp.bucket.name}")
    private String bucketName;

    private final Storage storage;

    public FileHandlerService() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(System.getenv("GOOGLE_APPLICATION_CREDENTIALS")));
        this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    @Scheduled(fixedRate = 60000) // 1 minute in milliseconds
    public void checkAndConsumeData() throws IOException {
        if (writer == null || currentFile == null || !currentFile.exists()) {
            createNewFile();
        }
    }

    private void createNewFile() throws IOException {
        if (writer != null) {
            writer.close();
            uploadFileToGCS();
            Files.delete(currentFile.toPath());
        }

        String timestamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String fileName = fileNamePrefix + "_" + timestamp + "_" + fileCounter++ + ".txt";
        currentFile = new File(directoryPath, fileName);
        writer = new BufferedWriter(new FileWriter(currentFile));

        System.out.println("Created new file: " + currentFile.getAbsolutePath());
    }

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "${kafka.group.id}")
    public void consumeData(ConsumerRecord<String, String> record) {
        try {
            if (writer == null || currentFile == null || !currentFile.exists()) {
                createNewFile();
            }
            writer.write(record.value());
            writer.newLine();
            writer.flush();

            if (currentFile.length() >= MAX_FILE_SIZE) {
                createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadFileToGCS() throws IOException {
        Path filePath = currentFile.toPath();
        byte[] data = Files.readAllBytes(filePath);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, currentFile.getName()).build();
        storage.create(blobInfo, data);

        System.out.println("Uploaded file to GCS: " + currentFile.getName());
    }
}
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableScheduling
@EnableKafka
public class MySpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApplication.class, args);
    }
}
# Directory path where files will be created
file.directory.path=/path/to/your/directory
file.name.prefix=myfile

# GCP bucket name
gcp.bucket.name=your-bucket-name

# Kafka configuration
kafka.bootstrap-servers=localhost:9092
kafka.topic.name=mytopic
kafka.group.id=mygroup
