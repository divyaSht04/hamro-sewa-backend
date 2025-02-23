package project.hamrosewa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${image.upload.dir}") // From application.properties
    private String uploadDir;

    public String saveProfileImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }
        // Get the backend project directory path using System.getProperty("user.dir")
        String projectRoot = System.getProperty("user.dir"); // This gives backend project directory
        String finalUploadDir = Paths.get(projectRoot, uploadDir).toString();

        // Ensure the "uploads" directory exists inside the backend folder
        File directory = new File(finalUploadDir);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("Failed to create upload directory!");
            }
        }

        // Generate a unique filename
        String originalFileName = file.getOriginalFilename();
        String safeFileName = UUID.randomUUID().toString() + "_" + (originalFileName != null ? originalFileName.replace(" ", "_") : "image.png");
        String filePath = finalUploadDir + File.separator + safeFileName;

        // Save the file to the "uploads" directory in the backend folder
        file.transferTo(new File(filePath));

        return safeFileName;
    }

    public byte[] getProfileImage(String fileName) throws IOException {
        String filePath = uploadDir + File.separator + fileName;
        return Files.readAllBytes(Paths.get(filePath));
    }
}
