import java.io.*;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;
import org.springframework.stereotype.Component;

@Component
public class FileMerger {

    public void mergeAndCompressFiles(Path inputDir, Path outputDir, String mergedFilename) throws IOException {
        // Ensure output directory exists
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        Path mergedFilePath = outputDir.resolve(mergedFilename);

        try (BufferedWriter writer = Files.newBufferedWriter(mergedFilePath)) {
            boolean isFirstFile = true;

            // Iterate through all files in the input directory
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(inputDir)) {
                for (Path filePath : directoryStream) {
                    if (Files.isRegularFile(filePath)) {
                        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                            String line;
                            boolean isFirstLine = true;

                            while ((line = reader.readLine()) != null) {
                                if (isFirstFile || !isFirstLine) {
                                    writer.write(line);
                                    writer.newLine();
                                }
                                isFirstLine = false;
                            }
                        }
                        isFirstFile = false;
                    }
                }
            }
        }

        // Compress the merged file into .gz
        compressFileToGzip(mergedFilePath);

        // Delete all files in the input directory
        deleteFilesInDirectory(inputDir);
    }

    private void compressFileToGzip(Path filePath) throws IOException {
        Path gzipFilePath = Paths.get(filePath.toString() + ".gz");
        try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(filePath));
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(gzipFilePath))) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                gzipOutputStream.write(buffer, 0, bytesRead);
            }
        }

        // Delete the original uncompressed merged file
        Files.delete(filePath);
    }

    private void deleteFilesInDirectory(Path directory) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            for (Path filePath : directoryStream) {
                Files.deleteIfExists(filePath);
            }
        }
    }
}
