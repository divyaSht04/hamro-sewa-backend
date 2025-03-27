package project.hamrosewa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "service_provider")
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "service_provider_id")
public class ServiceProvider extends User {
    private String businessName;

    @JsonIgnore
    @OneToMany(mappedBy = "serviceProvider")
    private List<ProviderService> services;
}
