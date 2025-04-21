package project.hamrosewa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.hamrosewa.model.ProviderService;
import project.hamrosewa.model.ServiceProvider;
import project.hamrosewa.model.ServiceStatus;

import java.util.List;

@Repository
public interface ProviderServiceRepository extends JpaRepository<ProviderService, Long> {
    List<ProviderService> findByServiceProvider(ServiceProvider serviceProvider);
    List<ProviderService> findByStatus(ServiceStatus status);

    List<ProviderService> findByServiceProviderId(int providerIdInt);
}
