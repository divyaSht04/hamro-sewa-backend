package project.hamrosewa.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "REVIEWS")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    @JsonIgnore // Keep this to prevent infinite recursion with Customer
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "PROVIDER_SERVICE_ID", nullable = false)
    private ProviderService providerService; // No @JsonIgnore here so we get the service ID

    @OneToOne
    @JoinColumn(name = "BOOKING_ID", nullable = false, unique = true)
    @JsonIgnore // Add this back to prevent cycles
    private ServiceBooking booking;

    @Column(name = "RATING", nullable = false)
    private int rating;

    @Column(name = "cust_comment", columnDefinition="CLOB")
    private String comment;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}