package project.hamrosewa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ServiceBookingService {

    @Autowired
    private ServiceBookingRepository bookingRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProviderServiceRepository providerServiceRepository;
    
    @Autowired
    private LoyaltyService loyaltyService;
    
    @Autowired
    private EmailService emailService;
    
    @Transactional
    public ServiceBooking createBooking(ServiceBookingDTO bookingDTO) {
        Customer customer = customerRepository.findById(Math.toIntExact(bookingDTO.getCustomerId()))
                .orElseThrow(() -> new UserValidationException("Customer not found with id: " + bookingDTO.getCustomerId()));

        ProviderService providerService = providerServiceRepository.findById(bookingDTO.getProviderServiceId())
                .orElseThrow(() -> new ProviderServiceException("Service not found with id: " + bookingDTO.getProviderServiceId()));

        if (providerService.getStatus() != ServiceStatus.APPROVED) {
            throw new IllegalArgumentException("Cannot book a service that is not approved");
        }

        ServiceBooking booking = new ServiceBooking();
        booking.setCustomer(customer);
        booking.setProviderService(providerService);
        booking.setBookingDateTime(bookingDTO.getBookingDateTime());
        booking.setBookingNotes(bookingDTO.getBookingNotes());
        booking.setStatus(BookingStatus.PENDING);
        
        // Check if the customer is eligible for a loyalty discount
        boolean shouldApplyDiscount = loyaltyService.shouldApplyDiscount(
                customer, providerService.getServiceProvider());
        
        if (shouldApplyDiscount) {
            booking.setDiscountApplied(true);
            booking.setDiscountPercentage(new java.math.BigDecimal("0.20")); // 20% discount
            booking.setOriginalPrice(providerService.getPrice());
            booking.setDiscountedPrice(
                    loyaltyService.calculateDiscountedPrice(providerService.getPrice()));
            
            // Reset the loyalty counter after applying the discount
            // This needs to happen at booking creation, not when the booking is completed
            loyaltyService.resetLoyaltyCounter(customer, providerService.getServiceProvider());
        } else {
            booking.setDiscountApplied(false);
            booking.setOriginalPrice(providerService.getPrice());
            booking.setDiscountedPrice(providerService.getPrice());
        }
        
        // Save the booking first
        ServiceBooking savedBooking = bookingRepository.save(booking);
        
        // Send booking confirmation email
        try {
            String formattedDateTime = booking.getBookingDateTime().format(
                DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm"));
            String formattedPrice = booking.getDiscountApplied() ? 
                booking.getDiscountedPrice().toString() + " (with 20% loyalty discount)" : 
                booking.getOriginalPrice().toString();
                
            emailService.sendBookingConfirmationEmail(
                customer.getEmail(),
                customer.getUsername(),
                savedBooking.getId(),
                providerService.getServiceName(),
                formattedDateTime,
                formattedPrice,
                providerService.getServiceProvider().getBusinessName()
            );
        } catch (Exception e) {
            // Log the error but don't fail the booking creation
            System.out.println("Failed to send booking confirmation email: " + e.getMessage());
        }
        
        return savedBooking;
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
    
    @Transactional
    public ServiceBooking updateBookingStatus(Long id, BookingStatus newStatus, String comment, boolean preserveLoyalty) {
        System.out.println("Updating booking " + id + " status to " + newStatus + ", preserveLoyalty=" + preserveLoyalty);
        
        ServiceBooking booking = getBookingById(id);
        BookingStatus currentStatus = booking.getStatus();
        System.out.println("Current status: " + currentStatus);

        validateStatusTransition(currentStatus, newStatus);

        booking.setStatus(newStatus);
        booking.setStatusComment(comment);
        booking.setUpdatedAt(LocalDateTime.now());
        
        // If cancelling a booking with loyalty discount
        if (newStatus == BookingStatus.CANCELLED && booking.getDiscountApplied() && preserveLoyalty) {
            System.out.println("Preserving loyalty discount for booking: " + id);
            Customer customer = booking.getCustomer();
            ServiceProvider provider = booking.getProviderService().getServiceProvider();
            loyaltyService.preserveLoyaltyDiscount(customer, provider);
        }
        
        // Save the status change
        ServiceBooking updatedBooking = bookingRepository.save(booking);
        
        // Process completed bookings for loyalty program - must happen after the status is saved
        if (newStatus == BookingStatus.COMPLETED && currentStatus != BookingStatus.COMPLETED) {
            System.out.println("Processing completed booking for loyalty program: " + id);
            loyaltyService.processCompletedBooking(updatedBooking);
            
            // Send booking completion email
            try {
                Customer customer = updatedBooking.getCustomer();
                ProviderService service = updatedBooking.getProviderService();
                String completionTime = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm"));
                
                emailService.sendBookingCompletionEmail(
                    customer.getEmail(),
                    customer.getUsername(),
                    updatedBooking.getId(),
                    service.getServiceName(),
                    completionTime
                );
                
                System.out.println("Booking completion email sent to " + customer.getEmail());
            } catch (Exception e) {
                // Log the error but don't fail the booking status update
                System.out.println("Failed to send booking completion email: " + e.getMessage());
            }
        }
        
        return updatedBooking;
    }
    
    // This method is now deprecated - use updateBookingStatus with CANCELLED status instead
    @Deprecated
    public void cancelBooking(Long id) {
        updateBookingStatus(id, BookingStatus.CANCELLED, "Cancelled by system", false);
    }
    
    private void validateStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        if (currentStatus == BookingStatus.CANCELLED || currentStatus == BookingStatus.COMPLETED) {
            throw new InvalidBookingStatusException(
                    "Cannot change status of a " + currentStatus.toString().toLowerCase() + " booking"
            );
        }

        if (currentStatus == BookingStatus.PENDING && newStatus == BookingStatus.COMPLETED) {
            throw new InvalidBookingStatusException(
                    "Cannot mark a pending booking as completed. It must be confirmed first"
            );
        }
    }
}
