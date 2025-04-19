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
import project.hamrosewa.model.Notification.NotificationType;
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
    
    @Autowired
    private NotificationService notificationService;
    
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
        
        // Check if the customer is eligible for a loyalty discount and wants to apply it
        boolean customerEligibleForDiscount = loyaltyService.shouldApplyDiscount(
                customer, providerService.getServiceProvider());
        
        // Only apply the discount if the customer is eligible AND has chosen to apply it
        boolean shouldApplyDiscount = customerEligibleForDiscount && 
                (bookingDTO.getApplyLoyaltyDiscount() != null && bookingDTO.getApplyLoyaltyDiscount());
        
        if (shouldApplyDiscount) {
            booking.setDiscountApplied(true);
            booking.setDiscountPercentage(new java.math.BigDecimal("0.20")); // 20% discount
            booking.setOriginalPrice(providerService.getPrice());
            booking.setDiscountedPrice(
                    loyaltyService.calculateDiscountedPrice(providerService.getPrice()));

            // Send notification about applied loyalty discount to customer
            notificationService.createNotification(
                "20% loyalty discount applied to your booking for " + providerService.getServiceName() + 
                ". You saved $" + booking.getOriginalPrice().subtract(booking.getDiscountedPrice()),
                NotificationType.LOYALTY_DISCOUNT,
                "/customer/bookings",
                Long.valueOf(customer.getId()),
                UserType.CUSTOMER
            );
            
            // Send notification to service provider about a booking with loyalty discount
            notificationService.createNotification(
                "New booking with 20% loyalty discount from " + customer.getFullName() + 
                " for your service " + providerService.getServiceName(),
                NotificationType.BOOKING_CREATED,
                "/provider/bookings",
                Long.valueOf(providerService.getServiceProvider().getId()),
                UserType.SERVICE_PROVIDER
            );
            
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
            
            // Send real-time notification to the customer
            notificationService.createNotification(
                "Your booking for " + providerService.getServiceName() + " has been created",
                NotificationType.BOOKING_CREATED,
                "/customer/bookings",
                Long.valueOf(customer.getId()),
                UserType.CUSTOMER
            );
            
            // Send real-time notification to the service provider
            notificationService.createNotification(
                "New booking request for " + providerService.getServiceName(),
                NotificationType.BOOKING_CREATED,
                "/service-provider/bookings",
                Long.valueOf(providerService.getServiceProvider().getId()),
                UserType.SERVICE_PROVIDER
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
    public ServiceBooking updateBookingStatus(Long bookingId, BookingStatus newStatus, String comment, boolean preserveLoyalty) {
        System.out.println("Updating booking " + bookingId + " status to " + newStatus + ", preserveLoyalty=" + preserveLoyalty);
        
        ServiceBooking booking = getBookingById(bookingId);
        BookingStatus currentStatus = booking.getStatus();
        System.out.println("Current status: " + currentStatus);

        validateStatusTransition(currentStatus, newStatus);

        booking.setStatus(newStatus);
        booking.setStatusComment(comment);
        booking.setUpdatedAt(LocalDateTime.now());

        // If completing the booking, update the customer's service count for loyalty program
        if (newStatus == BookingStatus.COMPLETED) {
            // Increment the loyalty counter for completed service
            loyaltyService.processCompletedBooking(booking);
            
            // Add notification for service completion
            notificationService.createNotification(
                "Your booking for " + booking.getProviderService().getServiceName() + " has been completed",
                NotificationType.BOOKING_COMPLETED,
                "/customer/bookings",
                Long.valueOf(booking.getCustomer().getId()),
                UserType.CUSTOMER
            );
            
            // Return the updated booking
            ServiceBooking updatedBooking = bookingRepository.save(booking);
            return updatedBooking;
        } else if (newStatus == BookingStatus.CONFIRMED) {
            // Send notification to customer that booking is confirmed
            notificationService.createNotification(
                "Your booking for " + booking.getProviderService().getServiceName() + " has been confirmed",
                NotificationType.BOOKING_CONFIRMED,
                "/customer/bookings",
                Long.valueOf(booking.getCustomer().getId()),
                UserType.CUSTOMER
            );
        } else if (newStatus == BookingStatus.CANCELLED) {
            if (booking.getDiscountApplied() != null && booking.getDiscountApplied() && preserveLoyalty) {
                System.out.println("Preserving loyalty discount for booking: " + bookingId);
                Customer customer = booking.getCustomer();
                ServiceProvider provider = booking.getProviderService().getServiceProvider();
                try {
                    loyaltyService.preserveLoyaltyDiscount(customer, provider);
                } catch (Exception e) {
                    System.out.println("Error preserving loyalty discount: " + e.getMessage());
                    // Continue with the status update even if loyalty preservation fails
                }
            }
            
            // Send notification about cancellation to both parties
            notificationService.createNotification(
                "Your booking for " + booking.getProviderService().getServiceName() + " has been cancelled",
                NotificationType.BOOKING_CANCELLED,
                "/customer/bookings",
                Long.valueOf(booking.getCustomer().getId()),
                UserType.CUSTOMER
            );
            
            notificationService.createNotification(
                "Booking for " + booking.getProviderService().getServiceName() + " has been cancelled",
                NotificationType.BOOKING_CANCELLED,
                "/provider/bookings",
                Long.valueOf(booking.getProviderService().getServiceProvider().getId()),
                UserType.SERVICE_PROVIDER
            );
        }
        
        // Save the status change
        ServiceBooking updatedBooking = bookingRepository.save(booking);

        if (newStatus == BookingStatus.COMPLETED) {
            try {
                loyaltyService.processCompletedBooking(updatedBooking);
            } catch (Exception e) {
                System.out.println("Error processing completed booking for loyalty: " + e.getMessage());
            }

            try {
                Customer customer = updatedBooking.getCustomer();
                if (customer != null && customer.getEmail() != null) {
                    ProviderService service = updatedBooking.getProviderService();
                    if (service != null && service.getServiceName() != null) {
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
                    } else {
                        System.out.println("Cannot send email: Service or service name is null");
                    }
                } else {
                    System.out.println("Cannot send email: Customer or customer email is null");
                }
            } catch (Exception e) {
                // Log the error but don't fail the booking status update
                System.out.println("Failed to send booking completion email: " + e.getMessage());
            }
        }
        
        return updatedBooking;
    }

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
