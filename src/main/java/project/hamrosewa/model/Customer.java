package project.hamrosewa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@PrimaryKeyJoinColumn(name = "customer_id")
public class Customer extends User{

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;
    private String fullName;
    
    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private List<ServiceBooking> bookings;
    
    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private List<Review> reviews;
}
