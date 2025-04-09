package project.hamrosewa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.hamrosewa.model.LoyaltyTracker;
import java.util.Optional;

@Repository
public interface LoyaltyTrackerRepository extends JpaRepository<LoyaltyTracker, Long> {

    Optional<LoyaltyTracker> findByCustomerIdAndServiceProviderId(int customerId, int serviceProviderId);
}
