package project.hamrosewa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.hamrosewa.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
//    List<Review> findByCustomerId(int customerId);
    
    List<Review> findByProviderServiceId(Long providerServiceId);
    
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.booking.id = :bookingId")
    boolean existsByBookingId(Long bookingId);
    
    @Query("SELECT r FROM Review r WHERE r.booking.id = :bookingId")
    Optional<Review> findByBookingId(Long bookingId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.providerService.id = :providerServiceId")
    Double calculateAverageRatingForService(Long providerServiceId);
    
    @Query("SELECT r FROM Review r WHERE r.providerService.serviceProvider.id = :serviceProviderId")
    List<Review> findByServiceProviderId(int serviceProviderId);
}
