package project.hamrosewa.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ServiceProviderDTO extends UserDTO{

    private String businessName;
    private String serviceCategory;
    private String description;
    private Double hourlyRate;
    private LocalDate date = LocalDate.now();
}
