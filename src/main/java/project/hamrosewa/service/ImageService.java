package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

@Transactional
@Service
public class ImageService {

    @Value("${image.upload.dir}")
    private String uploadPath;

    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    };

    public String saveImage(MultipartFile image) throws IOException {
        // Validate file type
        if (image == null || !isImageFile(image)) {
            throw new IllegalArgumentException("Invalid file type. Only image files (JPEG, PNG, GIF, WEBP) are allowed.");
        }

        // Create upload directory if it doesn't exist
        Path uploadDir = Paths.get(uploadPath, "images");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Generate unique filename
        String originalFilename = image.getOriginalFilename();
        String filename = UUID.randomUUID().toString();
        if (originalFilename != null && originalFilename.contains(".")) {
            filename += originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            filename += ".jpg"; // Default extension
        }

        // Save file
        Path filePath = uploadDir.resolve(filename);
        Files.copy(image.getInputStream(), filePath);

        return filename;
    }

    public void deleteImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Path path = Paths.get(uploadPath, "images", imagePath);
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && Arrays.asList(ALLOWED_IMAGE_TYPES).contains(contentType);
    }

    public byte[] getImage(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Image filename cannot be null or empty");
        }
        Path path = Paths.get(uploadPath, "images", filename);
        if (!Files.exists(path)) {
            throw new IOException("Image not found: " + filename);
        }
        return Files.readAllBytes(path);
    }

    public Path getImagePath(String filename) {
        return Paths.get(uploadPath, "images", filename);
    }
}
