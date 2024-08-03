import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GcsToSftpScheduler {

    @Scheduled(fixedRate = 21600000) // Run every 6 hours
    public void executeTask() {
        // Implement the task logic here
    }
}

//Copy file from GCS to GKE POd folder
import java.io.BufferedReader;
import java.io.InputStreamReader;

public void copyFoldersFromGcs(String gcsBucket, String destinationPath) {
    try {
        String command = "gsutil -m rsync -r gs://" + gcsBucket + " " + destinationPath + 
                         " -x '.*_done$'";
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        process.waitFor();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

//SFTP to unix server 
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public void sftpFilesToUnix(String localPath, String remotePath, String username, String host, int port, String privateKeyPath) {
    JSch jsch = new JSch();
    try {
        jsch.addIdentity(privateKeyPath);
        Session session = jsch.getSession(username, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        
        sftpChannel.put(localPath, remotePath, ChannelSftp.OVERWRITE);
        
        sftpChannel.disconnect();
        session.disconnect();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
@Scheduled(fixedRate = 21600000)
public void executeTask() {
    String gcsBucket = "your-gcs-bucket";
    String destinationPath = "/path/in/pod/filesystem";
    String sftpHost = "unix-server-address";
    int sftpPort = 22;
    String sftpUsername = "your-username";
    String privateKeyPath = "/path/to/private/key";
    String remotePath = "/path/on/remote/system";

    copyFoldersFromGcs(gcsBucket, destinationPath);
    sftpFilesToUnix(destinationPath, remotePath, sftpUsername, sftpHost, sftpPort, privateKeyPath);
}
