package project.hamrosewa.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServiceBookingDTO {
    private Long id;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Provider service ID is required")
    private Long providerServiceId;
    
    @NotNull(message = "Booking date and time is required")
    @Future(message = "Booking date and time must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime bookingDateTime;
    
    private String bookingNotes;
    
    private Boolean discountApplied;
    
    private BigDecimal discountPercentage;
    
    private BigDecimal originalPrice;
    
    private BigDecimal discountedPrice;

    private Integer completedBookingsCount;

    private Boolean applyLoyaltyDiscount;

    private BigDecimal findPrice;

    private BigDecimal finalPrice;
}
