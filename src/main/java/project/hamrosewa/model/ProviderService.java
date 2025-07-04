package project.hamrosewa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "provider_services")
// Removed JsonIdentityInfo to prevent inconsistent serialization
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

    private String imagePath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_provider_id", foreignKey = @ForeignKey(name = "FK_PROV_SERV"))
    private ServiceProvider serviceProvider;

    private String category;

    @OneToMany(mappedBy = "providerService", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Keep this to prevent infinite recursion
    private List<ServiceBooking> bookings;

    @OneToMany(mappedBy = "providerService", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Keep this to prevent infinite recursion
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
