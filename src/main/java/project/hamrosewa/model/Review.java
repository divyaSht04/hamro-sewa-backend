package project.hamrosewa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "REVIEWS")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    @JsonIgnore
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "PROVIDER_SERVICE_ID", nullable = false)
    @JsonIgnore
    private ProviderService providerService;

    @OneToOne
    @JoinColumn(name = "BOOKING_ID", nullable = false, unique = true)
    @JsonIgnore
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