package project.hamrosewa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @OneToMany(mappedBy = "providerService")
    @JsonIgnore
    private List<ServiceBooking> bookings;
    
    @OneToMany(mappedBy = "providerService")
    @JsonIgnore
    private List<Review> reviews;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ServiceStatus status = ServiceStatus.PENDING;
    
    @Column(name = "admin_feedback")
    private String adminFeedback;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
