package project.hamrosewa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.hamrosewa.dto.ReviewDTO;
import project.hamrosewa.exceptions.BookingNotFoundException;
import project.hamrosewa.exceptions.ReviewValidationException;
import project.hamrosewa.model.*;
import project.hamrosewa.model.Notification.NotificationType;
import project.hamrosewa.repository.CustomerRepository;
import project.hamrosewa.repository.ProviderServiceRepository;
import project.hamrosewa.repository.ReviewRepository;
import project.hamrosewa.repository.ServiceBookingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ServiceBookingRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProviderServiceRepository providerServiceRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ProviderServiceService providerServiceService;

    @Autowired
    private ServiceProviderService serviceProviderService;


    public Review createReview(ReviewDTO reviewDTO) {
        // Validate booking exists
        ServiceBooking booking = bookingRepository.findById(reviewDTO.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(reviewDTO.getBookingId()));

        // Validate customer exists and matches the booking
        int customerIdInt = reviewDTO.getCustomerId().intValue();
        Customer customer = customerRepository.findById(customerIdInt)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + reviewDTO.getCustomerId()));

        if (booking.getCustomer().getId() != customer.getId()) {
            throw new ReviewValidationException("Customer does not have the booking!");
        }

        // Validate service exists and matches the booking
        ProviderService providerService = providerServiceRepository.findById(reviewDTO.getProviderServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + reviewDTO.getProviderServiceId()));

        if (!booking.getProviderService().getId().equals(providerService.getId())) {
            throw new ReviewValidationException("Service does not match the booking's service");
        }

        // Validate booking is completed
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ReviewValidationException("Cannot review a service that hasn't been completed");
        }

        // Check if a review already exists for this booking
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new ReviewValidationException("A review already exists for this booking");
        }

        // Create review
        Review review = new Review();
        review.setCustomer(customer);
        review.setProviderService(providerService);
        review.setBooking(booking);
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        booking.setReview(savedReview);
        bookingRepository.save(booking);

        // Send notification to the service provider about the new review
        notificationService.createNotification(
            "New review for your service '" + providerService.getServiceName() + "' - Rating: " + reviewDTO.getRating() + " stars",
            NotificationType.NEW_REVIEW,
            "/service-provider/reviews",
            Long.valueOf(providerService.getServiceProvider().getId()),
            UserType.SERVICE_PROVIDER
        );

        return savedReview;
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + id));
    }

    public List<Review> getReviewsByCustomerId(int customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        if(customer.isPresent()){
            return customer.get().getReviews();
        }
        return null;
    }

    public List<Review> getReviewsByProviderServiceId(Long providerServiceId) {
        return reviewRepository.findByProviderServiceId(providerServiceId);
    }

    public List<Review> getReviewsByServiceProviderId(long serviceProviderId) {
        ServiceProvider provider = serviceProviderService.getServiceProviderById(serviceProviderId);

        List<ProviderService> services = provider.getServices();

        List<Review> reviews = services.stream()
                .flatMap(ps -> ps.getReviews().stream()) // flatten the nested lists
                .toList(); // if using Java 16+, else use .collect(Collectors.toList())

        return reviews;
    }

    public Double calculateAverageRating(Long providerServiceId) {
        return reviewRepository.calculateAverageRatingForService(providerServiceId);
    }

    public Review updateReview(Long reviewId, ReviewDTO reviewDTO) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + reviewId));

        if (existingReview.getCustomer().getId() != reviewDTO.getCustomerId()) {
            throw new ReviewValidationException("Customer does not own this review");
        }
        
        // Get provider service and service provider for notification
        ProviderService providerService = existingReview.getProviderService();
        int oldRating = existingReview.getRating();
        
        existingReview.setRating(reviewDTO.getRating());
        existingReview.setComment(reviewDTO.getComment());

        Review updatedReview = reviewRepository.save(existingReview);
        
        // Ensure service provider ID is valid
        Long serviceProviderId = null;
        try {
            serviceProviderId = Long.valueOf(providerService.getServiceProvider().getId());
        } catch (Exception e) {
            System.err.println("Error getting service provider ID: " + e.getMessage());
            // Still return the updated review, but don't send notification
            return updatedReview;
        }

        // Send notification to the service provider about the review update
        try {
            notificationService.createNotification(
                "A review for your service '" + 
                (providerService.getServiceName() != null ? providerService.getServiceName() : "Unknown Service") + 
                "' has been updated - Rating changed from " + oldRating + " to " + reviewDTO.getRating() + " stars",
                NotificationType.REVIEW_UPDATED,
                "/service-provider/reviews",
                serviceProviderId,
                UserType.SERVICE_PROVIDER
            );
        } catch (Exception e) {
            System.err.println("Failed to create update notification: " + e.getMessage());
            // Continue execution - notification failure shouldn't prevent review update
        }
        
        return updatedReview;
    }

    public void deleteReview(Long reviewId, Long customerId) {
        // Validate review exists and is owned by the customer
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + reviewId));

        if (existingReview.getCustomer().getId() != customerId.intValue()) {
            throw new ReviewValidationException("Customer does not own this review");
        }
        
        // Get provider service and service provider for notification
        ProviderService providerService = existingReview.getProviderService();
        Customer customer = existingReview.getCustomer();
        int rating = existingReview.getRating();

        // Remove the reference from the booking
        ServiceBooking booking = existingReview.getBooking();
        if (booking != null) {
            booking.setReview(null);
            bookingRepository.save(booking);
        }

        reviewRepository.delete(existingReview);
        
        // Get customer name safely
        String customerName = "a customer";
        try {
            if (customer != null) {
                // Try to get full name or use username as fallback
                if (customer.getFullName() != null && !customer.getFullName().isEmpty()) {
                    customerName = customer.getFullName();
                } else if (customer.getUsername() != null) {
                    customerName = customer.getUsername();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting customer name: " + e.getMessage());
            // Fall back to generic name
        }
        
        // Ensure service provider ID is valid
        Long serviceProviderId = null;
        try {
            serviceProviderId = Long.valueOf(providerService.getServiceProvider().getId());
        } catch (Exception e) {
            System.err.println("Error getting service provider ID: " + e.getMessage());
            return; // Can't send notification without valid recipient
        }
        
        // Send notification to the service provider about the review deletion
        try {
            notificationService.createNotification(
                "A review (" + rating + " stars) for your service '" + 
                (providerService.getServiceName() != null ? providerService.getServiceName() : "Unknown Service") + 
                "' has been deleted by " + customerName,
                NotificationType.REVIEW_DELETED,
                "/service-provider/reviews",
                serviceProviderId,
                UserType.SERVICE_PROVIDER
            );
        } catch (Exception e) {
            System.err.println("Failed to create deletion notification: " + e.getMessage());
            // Continue execution - notification failure shouldn't prevent review deletion
        }
    }

    public Boolean existsByBookingId(Long bookingId) {
        // First verify that the booking exists
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        // Check if a review exists for this booking
        return reviewRepository.existsByBookingId(bookingId);
    }
    
    public Review getReviewByBookingId(Long bookingId) {
        // First verify that the booking exists
        ServiceBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        // Find the review for this booking
        return reviewRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found for booking with id: " + bookingId));
    }
}
