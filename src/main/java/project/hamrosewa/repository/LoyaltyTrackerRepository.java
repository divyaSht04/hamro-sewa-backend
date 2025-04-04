package project.hamrosewa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.hamrosewa.model.LoyaltyTracker;
import java.util.Optional;

@Repository
public interface LoyaltyTrackerRepository extends JpaRepository<LoyaltyTracker, Long> {
    
    /**
     * Find loyalty tracker by customer and service provider
     * 
     * @param customerId customer ID
     * @param serviceProviderId service provider ID
     * @return optional loyalty tracker
     */
    Optional<LoyaltyTracker> findByCustomerIdAndServiceProviderId(int customerId, int serviceProviderId);
}
