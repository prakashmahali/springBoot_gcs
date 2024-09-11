import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>



public void sendEmailWithAttachment(String fileName) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    
    helper.setTo("recipient@example.com");
    helper.setSubject("SFTP File Transfer Successful");
    helper.setText("<html><body><h3>SFTP Transfer Complete</h3><p>File: " + fileName + " has been successfully transferred.</p></body></html>", true);
    
    mailSender.send(message);
}

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SftpTaskScheduler {

    @Scheduled(fixedRate = 3600000)  // Every hour
    public void performScheduledTask() {
        List<File> directories = getDirectoriesWithDateFormat("/app/");
        for (File dir : directories) {
            try {
                mergeAndZipFiles(dir);
                File zipFile = new File(dir.getAbsolutePath() + ".zip");
                sftpFile(zipFile, "/remote/directory/");
                sendEmailWithAttachment(zipFile.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}







import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DirectoryProcessorService {

    private static final String BASE_DIR = "/app"; // Change this to your base directory
    private static final String ZIP_DIR = "/app/zipped"; // Where zipped files will be saved
    private static final String SFTP_HOST = "remote-server.com";
    private static final int SFTP_PORT = 22;
    private static final String SFTP_USER = "your-username";
    private static final String SFTP_PRIVATE_KEY = "/path/to/id_rsa";

    @Scheduled(cron = "0 0 * * * ?") // Every hour
    public void processDirectories() throws Exception {
        File baseDir = new File(BASE_DIR);
        File[] directories = baseDir.listFiles(File::isDirectory);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String currentDate = dateFormat.format(new Date());

        if (directories != null) {
            for (File directory : directories) {
                if (directory.getName().matches("\\d{8}")) { // Matches yyyymmdd format
                    mergeAndZipFiles(directory);
                    sftpFile(directory.getName());
                }
            }
        }
    }

    private void mergeAndZipFiles(File directory) throws IOException {
        File zipOutput = new File(ZIP_DIR, directory.getName() + ".zip");
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipOutput))) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    zipFile(file, zipOut);
                }
            }
        }
    }

    private void zipFile(File file, ZipOutputStream zipOut) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }

    private void sftpFile(String directoryName) throws Exception {
        JSch jsch = new JSch();
        jsch.addIdentity(SFTP_PRIVATE_KEY);

        Session session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        File zipFile = new File(ZIP_DIR + "/" + directoryName + ".zip");
        try (FileInputStream fis = new FileInputStream(zipFile)) {
            channelSftp.put(fis, "/remote/path/" + zipFile.getName());
        }

        channelSftp.disconnect();
        session.disconnect();
    }
}
