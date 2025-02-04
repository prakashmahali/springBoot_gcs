import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;

@Service
public class DirectoryCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryCleanupService.class);
    private static final String BASE_DIR = "/opt/wrk/";
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{8}"); // Matches yyyymmdd format

    public void deleteOldDirectories() {
        try {
            Path basePath = Paths.get(BASE_DIR);
            if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
                logger.warn("Base directory does not exist or is not a directory: {}", BASE_DIR);
                return;
            }

            Files.list(basePath)
                .filter(Files::isDirectory) // Only process directories
                .filter(path -> DATE_PATTERN.matcher(path.getFileName().toString()).matches()) // Match yyyymmdd format
                .forEach(this::deleteDirectory);

        } catch (IOException e) {
            logger.error("Error listing directories under {}", BASE_DIR, e);
        }
    }

    private void deleteDirectory(Path dirPath) {
        try {
            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            logger.info("Deleted directory: {}", dirPath);
        } catch (IOException e) {
            logger.error("Failed to delete directory: {}", dirPath, e);
        }
    }
}
