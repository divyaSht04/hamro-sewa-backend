package project.hamrosewa.dto;

import lombok.Data;

@Data
public class AdminDTO extends UserDTO {
    private String fullName;
    private String department;
}
