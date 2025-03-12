package project.hamrosewa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.hamrosewa.model.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCustomerId(int customerId);
    
    List<Review> findByProviderServiceId(Long providerServiceId);
    
    // Replace the derived method with a custom query that works with Oracle
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.booking.id = :bookingId")
    boolean existsByBookingId(Long bookingId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.providerService.id = :providerServiceId")
    Double calculateAverageRatingForService(Long providerServiceId);
    
    @Query("SELECT r FROM Review r WHERE r.providerService.serviceProvider.id = :serviceProviderId")
    List<Review> findByServiceProviderId(int serviceProviderId);
}
