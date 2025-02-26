package project.hamrosewa.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class ProviderServiceDTO {
    private Long id;
    private String serviceName;
    private String description;
    private BigDecimal price;
    private MultipartFile pdf;
    private String pdfPath;
    private String category;
    // to get service provider ID!
    private Long serviceProviderId;
}
