package project.hamrosewa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "service_provider")
@Data
@PrimaryKeyJoinColumn(name = "service_provider_id")
public class ServiceProvider extends User {
    private String businessName;

    @JsonIgnore
    @OneToMany(mappedBy = "serviceProvider")
    private List<ProviderService> services;
}
