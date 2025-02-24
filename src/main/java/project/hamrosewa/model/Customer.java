package project.hamrosewa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@PrimaryKeyJoinColumn(name = "customer_id")
public class Customer extends User{
    private LocalDate dateOfBirth;
    private String fullName;
}
