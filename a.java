import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GcsDirectoryCopier {

    public static void main(String[] args) {
        String bucketName = "your-gcs-bucket";
        String destinationPath = "/local/destination/path";

        copyDirectories(bucketName, destinationPath);
    }

    public static void copyDirectories(String bucketName, String destinationPath) {
        try {
            // List directories in the GCS bucket
            String listCommand = String.format("gsutil ls -d gs://%s/**", bucketName);
            Process listProcess = Runtime.getRuntime().exec(listCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(listProcess.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String directoryName = line.substring(line.lastIndexOf('/') + 1, line.length() - 1);

                // Check if directory name is exactly 8 numeric characters
                if (directoryName.matches("\\d{8}")) {
                    // Construct the gsutil cp command to copy the directory
                    String copyCommand = String.format("gsutil -m cp -r %s %s", line, destinationPath);
                    executeCommand(copyCommand);
                }
            }

            listProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void executeCommand(String command) {
        try {
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
}
