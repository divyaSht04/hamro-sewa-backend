package project.hamrosewa.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdminDTO extends UserDTO {

    private LocalDate dateOfBirth;
    private String fullName;
    private String department;
}
