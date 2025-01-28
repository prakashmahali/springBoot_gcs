import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileMerger {

    public static void mergeAndZipFiles(Path inputDir, Path outputDir, String mergedFilename) throws IOException {
        if (!Files.exists(inputDir) || !Files.isDirectory(inputDir)) {
            throw new IllegalArgumentException("Input directory does not exist or is not a directory.");
        }
        
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        Path mergedFile = outputDir.resolve(mergedFilename);
        boolean headerAdded = false;

        try (BufferedWriter writer = Files.newBufferedWriter(mergedFile)) {
            // Iterate through files in the input directory
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file)) {
                        try (BufferedReader reader = Files.newBufferedReader(file)) {
                            String line;
                            boolean isFirstLine = true;
                            while ((line = reader.readLine()) != null) {
                                if (isFirstLine) {
                                    isFirstLine = false;
                                    if (headerAdded) {
                                        continue; // Skip header of subsequent files
                                    } else {
                                        headerAdded = true; // Add header from the first file
                                    }
                                }
                                writer.write(line);
                                writer.newLine();
                            }
                        }
                    }
                }
            }
        }

        // Zip the merged file
        Path zipFile = outputDir.resolve(mergedFilename + ".zip");
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            zipOut.putNextEntry(new ZipEntry(mergedFilename));
            Files.copy(mergedFile, zipOut);
            zipOut.closeEntry();
        }

        // Delete all files in the input directory
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir)) {
            for (Path file : stream) {
                Files.deleteIfExists(file);
            }
        }

        // Optionally, delete the merged file if only the zip is needed
        Files.deleteIfExists(mergedFile);
    }

    public static void main(String[] args) {
        try {
            Path inputDir = Paths.get("/path/to/input/dir");
            Path outputDir = Paths.get("/path/to/output/dir");
            String mergedFilename = "merged_file.txt";

            mergeAndZipFiles(inputDir, outputDir, mergedFilename);
            System.out.println("Files merged, zipped, and input directory cleared successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
