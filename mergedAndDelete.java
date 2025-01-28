import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileMerger {

    public static void mergeFiles(String inputDirPath, String outputDirPath, String mergedFilename) throws IOException {
        // Validate input and output directories
        Path inputDir = Paths.get(inputDirPath);
        Path outputDir = Paths.get(outputDirPath);

        if (!Files.isDirectory(inputDir)) {
            throw new IllegalArgumentException("Input directory does not exist: " + inputDirPath);
        }

        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        Path mergedFilePath = outputDir.resolve(mergedFilename);

        try (BufferedWriter writer = Files.newBufferedWriter(mergedFilePath)) {
            boolean headerWritten = false;

            // Process files in the input directory
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(inputDir)) {
                for (Path filePath : directoryStream) {
                    if (Files.isRegularFile(filePath)) {
                        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                            String line;
                            boolean isFirstLine = true;
                            while ((line = reader.readLine()) != null) {
                                if (isFirstLine) {
                                    isFirstLine = false;
                                    if (!headerWritten) {
                                        writer.write(line);
                                        writer.newLine();
                                        headerWritten = true;
                                    }
                                } else {
                                    writer.write(line);
                                    writer.newLine();
                                }
                            }
                        }
                    }
                }
            }
        }

        // Delete all files in the input directory
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(inputDir)) {
            for (Path filePath : directoryStream) {
                if (Files.isRegularFile(filePath)) {
                    Files.delete(filePath);
                }
            }
        }

        System.out.println("Files merged successfully into: " + mergedFilePath);
    }

    public static void main(String[] args) {
        try {
            mergeFiles("/path/to/inputDir", "/path/to/outputDir", "mergedFile.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
