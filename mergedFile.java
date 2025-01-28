import java.io.*;
import java.nio.file.*;
import java.util.Objects;

public class FileMerger {

    public static void mergeFiles(String inputDir, String outputDir, String mergedFilename) {
        File inputDirectory = new File(inputDir);
        File outputDirectory = new File(outputDir);

        // Validate input directory
        if (!inputDirectory.exists() || !inputDirectory.isDirectory()) {
            System.err.println("Input directory does not exist or is not a directory.");
            return;
        }

        // Ensure output directory exists
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        File mergedFile = new File(outputDirectory, mergedFilename);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(mergedFile))) {
            boolean isFirstFile = true;

            for (File file : Objects.requireNonNull(inputDirectory.listFiles())) {
                if (file.isFile()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        boolean isHeaderLine = true;

                        while ((line = reader.readLine()) != null) {
                            // Write header only from the first file
                            if (isHeaderLine) {
                                if (isFirstFile) {
                                    writer.write(line);
                                    writer.newLine();
                                }
                                isHeaderLine = false;
                            } else {
                                writer.write(line);
                                writer.newLine();
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + file.getName());
                        e.printStackTrace();
                    }
                }
                isFirstFile = false; // After the first file, headers should be ignored
            }

            System.out.println("Merged file created at: " + mergedFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing to merged file.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Example usage
        String inputDir = "path/to/input/directory";
        String outputDir = "path/to/output/directory";
        String mergedFilename = "merged_file.txt";

        mergeFiles(inputDir, outputDir, mergedFilename);
    }
}
