package project.hamrosewa.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminDTO extends UserDTO {

    private LocalDate dateOfBirth;
    private String fullName;
    private String department;
}
