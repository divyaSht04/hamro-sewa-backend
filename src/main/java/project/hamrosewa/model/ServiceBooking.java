package project.hamrosewa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "service_bookings")
// Removed JsonIdentityInfo to prevent inconsistent object/id serialization
public class ServiceBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
//    @JsonIgnore // Keep this to prevent infinite recursion with Customer
    private Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "provider_service_id", nullable = false)
    private ProviderService providerService; // Keep without @JsonIgnore so we get service data
    
    @Column(nullable = false)
    private LocalDateTime bookingDateTime = LocalDateTime.now();
    
    private String bookingNotes;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;
    
    @OneToOne(mappedBy = "booking", cascade = CascadeType.REFRESH, orphanRemoval = false)
    @JsonIgnore // Add this back to prevent cycles
    private Review review;
    
    @Column(name = "status_comment")
    private String statusComment;
    
    @Column(name = "discount_applied")
    private Boolean discountApplied = false;
    
    @Column(name = "discount_percentage")
    private BigDecimal discountPercentage;
    
    @Column(name = "original_price")
    private BigDecimal originalPrice;
    
    @Column(name = "discounted_price")
    private BigDecimal discountedPrice;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
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
