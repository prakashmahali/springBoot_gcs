import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DirectoryProcessor {

    private static final String BASE_DIR = "/app/";

    public List<Path> getDirectories() throws IOException {
        // Pattern to match yyyymmdd directory names
        Pattern pattern = Pattern.compile("\\d{8}");

        // Get directories matching the pattern
        return Files.list(Paths.get(BASE_DIR))
                .filter(Files::isDirectory)
                .filter(path -> pattern.matcher(path.getFileName().toString()).matches())
                .collect(Collectors.toList());
    }
}
import java.io.*;
import java.nio.file.*;
import java.util.List;

public class FileMerger {

    public void mergeFiles(Path dir, String mergedFileName) throws IOException {
        File mergedFile = new File(dir.toString(), mergedFileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mergedFile))) {
            Files.list(dir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.write(line);
                            writer.newLine();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
    }
}
import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class Zipper {

    public void zipFile(Path sourceFile, Path targetZip) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(targetZip.toFile());
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            File fileToZip = sourceFile.toFile();
            try (FileInputStream fis = new FileInputStream(fileToZip)) {
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);
                
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
            }
        }
    }
}
<dependency>
    <groupId>com.jcraft</groupId>
    <artifactId>jsch</artifactId>
    <version>0.1.55</version>
</dependency>

  import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.FileInputStream;
import java.util.Properties;

public class SftpUploader {

    public void uploadFile(String localFilePath, String remoteDir, String remoteHost, String username, String password) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, remoteHost, 22);
        session.setPassword(password);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();

        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        try (FileInputStream fis = new FileInputStream(localFilePath)) {
            channelSftp.put(fis, remoteDir + "/" + new File(localFilePath).getName());
        }

        channelSftp.disconnect();
        session.disconnect();
    }
}
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DirectoryTaskScheduler {

    private final DirectoryProcessor directoryProcessor = new DirectoryProcessor();
    private final FileMerger fileMerger = new FileMerger();
    private final Zipper zipper = new Zipper();
    private final SftpUploader sftpUploader = new SftpUploader();

    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    public void processDirectories() {
        try {
            List<Path> directories = directoryProcessor.getDirectories();

            for (Path dir : directories) {
                String mergedFileName = "merged_" + dir.getFileName().toString() + ".txt";
                String zipFileName = "merged_" + dir.getFileName().toString() + ".zip";

                // Merge files
                fileMerger.mergeFiles(dir, mergedFileName);

                // Zip merged file
                Path mergedFilePath = Paths.get(dir.toString(), mergedFileName);
                Path zipFilePath = Paths.get(dir.toString(), zipFileName);
                zipper.zipFile(mergedFilePath, zipFilePath);

                // SFTP zip file
                String remoteDir = "/remote/dir";
                String remoteHost = "remote.host.com";
                String username = "sftpUser";
                String password = "sftpPassword";

                sftpUploader.uploadFile(zipFilePath.toString(), remoteDir, remoteHost, username, password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
sftp.remote.host=remote.host.com
sftp.remote.username=sftpUser
sftp.remote.password=sftpPassword
sftp.remote.dir=/remote/dir
