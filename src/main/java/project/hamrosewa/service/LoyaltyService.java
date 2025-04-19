package project.hamrosewa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.hamrosewa.model.BookingStatus;
import project.hamrosewa.model.Customer;
import project.hamrosewa.model.LoyaltyTracker;
import project.hamrosewa.model.Notification.NotificationType;
import project.hamrosewa.model.ServiceBooking;
import project.hamrosewa.model.ServiceProvider;
import project.hamrosewa.model.UserType;
import project.hamrosewa.repository.LoyaltyTrackerRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class LoyaltyService {

    private static final int BOOKINGS_REQUIRED_FOR_DISCOUNT = 4;
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.20"); // 20% discount

    @Autowired
    private LoyaltyTrackerRepository loyaltyTrackerRepository;
    
    @Autowired
    private NotificationService notificationService;

    @Transactional
    public void processCompletedBooking(ServiceBooking booking) {
        System.out.println("Processing completed booking: " + booking.getId());
        
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            System.out.println("Booking status is not COMPLETED, skipping loyalty processing");
            return;
        }
        
        // Skip bookings that had a loyalty discount applied
        if (booking.getDiscountApplied() != null && booking.getDiscountApplied()) {
            System.out.println("Booking " + booking.getId() + " had loyalty discount applied, not counting toward next discount");
            return;
        }

        Customer customer = booking.getCustomer();
        ServiceProvider serviceProvider = booking.getProviderService().getServiceProvider();

        System.out.println("Processing loyalty for customer: " + customer.getId() + " and provider: " + serviceProvider.getId());

        LoyaltyTracker tracker = getOrCreateLoyaltyTracker(customer, serviceProvider);
        int oldCount = tracker.getCompletedBookingsCount();

        tracker.setCompletedBookingsCount(oldCount + 1);
        System.out.println("Incrementing loyalty counter from " + oldCount + " to " + tracker.getCompletedBookingsCount());
        
        // Save the updated tracker
        loyaltyTrackerRepository.save(tracker);
        System.out.println("Loyalty tracker saved successfully");
        
        // Check if the customer has now become eligible for a discount
        if (tracker.getCompletedBookingsCount() == BOOKINGS_REQUIRED_FOR_DISCOUNT) {
            // Send notification to the customer about the newly available discount
            notificationService.createNotification(
                "Congratulations! You're eligible for a 20% loyalty discount on your next booking with " + 
                serviceProvider.getBusinessName(),
                NotificationType.LOYALTY_DISCOUNT,
                "/customer/bookings/new",
                Long.valueOf(customer.getId()),
                UserType.CUSTOMER
            );
        }
    }

    public boolean shouldApplyDiscount(Customer customer, ServiceProvider serviceProvider) {
        LoyaltyTracker tracker = getOrCreateLoyaltyTracker(customer, serviceProvider);
        return tracker.getCompletedBookingsCount() >= BOOKINGS_REQUIRED_FOR_DISCOUNT;
    }

    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice) {
        if (originalPrice == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountAmount = originalPrice.multiply(DISCOUNT_PERCENTAGE);
        return originalPrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }

    public int getCompletedBookingsCount(Customer customer, ServiceProvider serviceProvider) {
        LoyaltyTracker tracker = getOrCreateLoyaltyTracker(customer, serviceProvider);
        return tracker.getCompletedBookingsCount();
    }

    private LoyaltyTracker getOrCreateLoyaltyTracker(Customer customer, ServiceProvider serviceProvider) {
        Optional<LoyaltyTracker> existingTracker = loyaltyTrackerRepository.findByCustomerIdAndServiceProviderId(
                customer.getId(), serviceProvider.getId());
        
        if (existingTracker.isPresent()) {
            return existingTracker.get();
        }
        
        LoyaltyTracker newTracker = new LoyaltyTracker();
        newTracker.setCustomer(customer);
        newTracker.setServiceProvider(serviceProvider);
        newTracker.setCompletedBookingsCount(0);
        return loyaltyTrackerRepository.save(newTracker);
    }

    @Transactional
    public void preserveLoyaltyDiscount(Customer customer, ServiceProvider serviceProvider) {
        LoyaltyTracker tracker = getOrCreateLoyaltyTracker(customer, serviceProvider);
        
        // If the tracker was reset when creating the booking, restore it
        if (tracker.getCompletedBookingsCount() < BOOKINGS_REQUIRED_FOR_DISCOUNT) {
            tracker.setCompletedBookingsCount(BOOKINGS_REQUIRED_FOR_DISCOUNT);
            loyaltyTrackerRepository.save(tracker);
        }
    }

    /**
     * Reset loyalty counter after applying discount
     *
     * @param customer the customer
     * @param serviceProvider the service provider
     */
    @Transactional
    public void resetLoyaltyCounter(Customer customer, ServiceProvider serviceProvider) {
        LoyaltyTracker tracker = getOrCreateLoyaltyTracker(customer, serviceProvider);
        tracker.setCompletedBookingsCount(0);
        loyaltyTrackerRepository.save(tracker);
    }
}
