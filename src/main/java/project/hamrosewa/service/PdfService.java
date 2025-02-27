package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Transactional
@Service
public class PdfService {

    @Value("${pdf.upload.dir}")
    private String uploadPath;

    public String savePdf(MultipartFile pdf) throws IOException {
        // Validate file type
        if (pdf == null || !isPdfFile(pdf)) {
            throw new IllegalArgumentException("Invalid file type. Only PDF files are allowed.");
        }

        // Create upload directory if it doesn't exist
        Path uploadDir = Paths.get(uploadPath, "pdfs");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Generate unique filename
        String originalFilename = pdf.getOriginalFilename();
        String filename = UUID.randomUUID().toString();
        if (originalFilename != null && originalFilename.contains(".")) {
            filename += originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            filename += ".pdf";
        }

        // Save file
        Path filePath = uploadDir.resolve(filename);
        Files.copy(pdf.getInputStream(), filePath);

        return filename;
    }

    public void deletePdf(String pdfPath) {
        if (pdfPath != null && !pdfPath.isEmpty()) {
            try {
                Path path = Paths.get(uploadPath, "pdfs", pdfPath);
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("application/pdf");
    }

    public Path getPdfPath(String filename) {
        return Paths.get(uploadPath, "pdfs", filename);
    }
}
