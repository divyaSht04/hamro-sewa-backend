package project.hamrosewa.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "provider_services")
public class ProviderService {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String serviceName;

    @Column(columnDefinition = "CLOB")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String pdfPath;
    
    // Add image field to store the image filename
    private String imagePath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_provider_id", foreignKey = @ForeignKey(name = "FK_PROV_SERV"))
    private ServiceProvider serviceProvider;

    private String category;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
