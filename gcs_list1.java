import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GcsDirectoryService {

    // Regular expression to match directories in yyyymmdd format
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{8}$");

    public List<String> listDirectories(String bucketName) {
        List<String> directories = new ArrayList<>();

        try {
            // Use ProcessBuilder to execute gsutil command
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "gsutil", "ls", "-d", "gs://" + bucketName + "/*"
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Use BufferedReader to read the command's output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Extract and clean the directory name (remove trailing slashes)
                String directoryName = extractDirectoryName(line);

                // Validate if the directory matches yyyymmdd and ignore yyyymmdd_done
                if (isValidDirectory(directoryName)) {
                    directories.add(directoryName);
                }
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return directories;
    }

    // Method to extract the directory name from the GCS path (removing trailing slash)
    private String extractDirectoryName(String gcsPath) {
        String[] parts = gcsPath.split("/");
        return parts[parts.length - 1].replaceAll("/", "");  // Remove any trailing slashes
    }

    // Method to check if the directory name is in yyyymmdd format and does not end with _done
    private boolean isValidDirectory(String directoryName) {
        // Match the directory name with yyyymmdd format
        Matcher matcher = DATE_PATTERN.matcher(directoryName);
        return matcher.matches();  // Only return true if the directory matches exactly 8 digits
    }

    public static void main(String[] args) {
        GcsDirectoryService service = new GcsDirectoryService();
        List<String> directories = service.listDirectories("your-bucket-name");
        directories.forEach(System.out::println);  // Output the valid directories
    }
}
