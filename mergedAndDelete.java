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



import java.io.*;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;
import org.springframework.stereotype.Component;

@Component
public class FileMerger {

    public void mergeAndZipFiles(Path inputDir, Path outputDir, String mergedFileName) throws IOException {
        // Clean the output directory
        cleanDirectory(outputDir);

        // Create the merged file
        Path mergedFilePath = outputDir.resolve(mergedFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(mergedFilePath)) {
            boolean isHeaderWritten = false;

            // Iterate over all files in the input directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir)) {
                for (Path filePath : stream) {
                    if (Files.isRegularFile(filePath)) {
                        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                            String line;
                            boolean isFirstLine = true;

                            while ((line = reader.readLine()) != null) {
                                if (isFirstLine) {
                                    // Write header only if not already written
                                    if (!isHeaderWritten) {
                                        writer.write(line);
                                        writer.newLine();
                                        isHeaderWritten = true;
                                    }
                                    isFirstLine = false;
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

        // Zip the merged file
        Path zipFilePath = outputDir.resolve(mergedFileName + ".gz");
        try (FileInputStream fis = new FileInputStream(mergedFilePath.toFile());
             FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
             GZIPOutputStream gzipOut = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                gzipOut.write(buffer, 0, bytesRead);
            }
        }

        // Delete the merged file after zipping
        Files.delete(mergedFilePath);

        // Clean the input directory
        cleanDirectory(inputDir);
    }

    private void cleanDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path filePath : stream) {
                    Files.deleteIfExists(filePath);
                }
            }
        } else {
            Files.createDirectories(dir);
        }
    }
}

