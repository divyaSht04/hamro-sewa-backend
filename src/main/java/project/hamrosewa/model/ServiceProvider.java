package project.hamrosewa.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;


@EqualsAndHashCode(callSuper = true)
@Entity(name = "service_provider")
@Data
@PrimaryKeyJoinColumn(name = "service_provider_id")
public class ServiceProvider extends User{
    private String businessName;
    private String serviceCategory;
    private String description;
    private boolean isVerified;
    private Double hourlyRate;
    @Column(name = "provider_date")
    private LocalDate date;
}
