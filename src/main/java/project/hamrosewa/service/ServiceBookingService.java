package project.hamrosewa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.hamrosewa.dto.ServiceBookingDTO;
import project.hamrosewa.exceptions.BookingNotFoundException;
import project.hamrosewa.exceptions.InvalidBookingStatusException;
import project.hamrosewa.exceptions.ProviderServiceException;
import project.hamrosewa.exceptions.UserValidationException;
import project.hamrosewa.model.*;
import project.hamrosewa.repository.CustomerRepository;
import project.hamrosewa.repository.ProviderServiceRepository;
import project.hamrosewa.repository.ServiceBookingRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceBookingService {

    @Autowired
    private ServiceBookingRepository bookingRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProviderServiceRepository providerServiceRepository;
    
    public ServiceBooking createBooking(ServiceBookingDTO bookingDTO) {
        // Validate customer exists
        Customer customer = customerRepository.findById(bookingDTO.getCustomerId())
                .orElseThrow(() -> new UserValidationException("Customer not found with id: " + bookingDTO.getCustomerId()));
        
        // Validate service exists
        ProviderService providerService = providerServiceRepository.findById(bookingDTO.getProviderServiceId())
                .orElseThrow(() -> new ProviderServiceException("Service not found with id: " + bookingDTO.getProviderServiceId()));
        
        // Validate service is approved
        if (providerService.getStatus() != ServiceStatus.APPROVED) {
            throw new IllegalArgumentException("Cannot book a service that is not approved");
        }

        ServiceBooking booking = new ServiceBooking();
        booking.setCustomer(customer);
        booking.setProviderService(providerService);
        booking.setBookingDateTime(bookingDTO.getBookingDateTime());
        booking.setBookingNotes(bookingDTO.getBookingNotes());
        booking.setStatus(BookingStatus.PENDING);
        
        return bookingRepository.save(booking);
    }
    
    public ServiceBooking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }
    
    public List<ServiceBooking> getBookingsByCustomerId(int customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }
    
    public List<ServiceBooking> getBookingsByProviderServiceId(Long providerServiceId) {
        return bookingRepository.findByProviderServiceId(providerServiceId);
    }
    
    public List<ServiceBooking> getBookingsByServiceProviderId(int serviceProviderId) {
        return bookingRepository.findByServiceProviderId(serviceProviderId);
    }
    
    public ServiceBooking updateBookingStatus(Long id, BookingStatus newStatus) {
        ServiceBooking booking = getBookingById(id);
        BookingStatus currentStatus = booking.getStatus();

        validateStatusTransition(currentStatus, newStatus);
        
        booking.setStatus(newStatus);
        booking.setUpdatedAt(LocalDateTime.now());
        
        return bookingRepository.save(booking);
    }
    
    public void cancelBooking(Long id) {
        ServiceBooking booking = getBookingById(id);

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStatusException("Only pending or confirmed bookings can be cancelled");
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        
        bookingRepository.save(booking);
    }
    
    private void validateStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                if (newStatus != BookingStatus.CONFIRMED && 
                    newStatus != BookingStatus.REJECTED && 
                    newStatus != BookingStatus.CANCELLED) {
                    throw new InvalidBookingStatusException(currentStatus, newStatus);
                }
                break;
            case CONFIRMED:
                if (newStatus != BookingStatus.COMPLETED && 
                    newStatus != BookingStatus.CANCELLED) {
                    throw new InvalidBookingStatusException(currentStatus, newStatus);
                }
                break;
            case COMPLETED:
            case CANCELLED:
            case REJECTED:
                throw new InvalidBookingStatusException("Cannot change status from " + currentStatus + " as it is a terminal state");
            default:
                throw new InvalidBookingStatusException("Unknown current status: " + currentStatus);
        }
    }
}
