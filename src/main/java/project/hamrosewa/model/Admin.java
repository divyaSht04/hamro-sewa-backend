package project.hamrosewa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@PrimaryKeyJoinColumn(name = "admin_id")
public class Admin extends User {

    private LocalDate dateOfBirth;
    private String fullName;
    private String department;

    private BigDecimal earnings;
}
