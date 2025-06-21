package org.bookswap.shared.image;

import lombok.extern.slf4j.Slf4j;
import org.bookswap.common.exception.InternalServerErrorException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class ImageService {

    private static final String BASE_DIR = "uploads";

    public String saveImage(String folder, Long entityId, MultipartFile file) {
        try {
            String filename = sanitizeFilename(file.getOriginalFilename());
            String uniqueName = UUID.randomUUID() + "-" + filename;

            Path targetDir = Paths.get(BASE_DIR, folder, String.valueOf(entityId));
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(uniqueName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/" + BASE_DIR + "/" + folder + "/" + entityId + "/" + uniqueName;
        } catch (IOException e) {
            log.error("Failed to save image", e);
            throw new InternalServerErrorException("Failed to save image", e);
        }
    }

    public void deleteImageByUrl(String url) {
        try {
            String relativePath = url.replaceFirst("^/uploads/", "");
            Path filePath = Paths.get(BASE_DIR, relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete image: {}", url, e);
        }
    }

    private String sanitizeFilename(String name) {
        if (name == null) return "unknown";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
