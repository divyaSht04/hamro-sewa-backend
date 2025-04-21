package project.hamrosewa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.hamrosewa.model.BookingStatus;
import project.hamrosewa.model.ServiceBooking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, Long> {
    List<ServiceBooking> findByCustomerId(int customerId);
    
    List<ServiceBooking> findByProviderServiceId(Long providerServiceId);
    
    List<ServiceBooking> findByStatus(BookingStatus status);
    
    List<ServiceBooking> findByBookingDateTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT b FROM ServiceBooking b WHERE b.providerService.serviceProvider.id = :serviceProviderId")
    List<ServiceBooking> findByServiceProviderId(int serviceProviderId);
    
    @Query("SELECT b FROM ServiceBooking b WHERE b.status = project.hamrosewa.model.BookingStatus.COMPLETED")
    List<ServiceBooking> findAllCompletedBookings();
    

    @Query("SELECT b FROM ServiceBooking b WHERE b.customer.id = :customerId AND b.providerService.serviceProvider.id = :serviceProviderId AND b.status = project.hamrosewa.model.BookingStatus.COMPLETED")
    List<ServiceBooking> findCompletedBookingsByCustomerAndProvider(int customerId, int serviceProviderId);
}
