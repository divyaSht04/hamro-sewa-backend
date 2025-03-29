package project.hamrosewa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.hamrosewa.dto.ReviewDTO;
import project.hamrosewa.exceptions.BookingNotFoundException;
import project.hamrosewa.exceptions.ReviewValidationException;
import project.hamrosewa.model.*;
import project.hamrosewa.repository.CustomerRepository;
import project.hamrosewa.repository.ProviderServiceRepository;
import project.hamrosewa.repository.ReviewRepository;
import project.hamrosewa.repository.ServiceBookingRepository;

import java.time.LocalDateTime;
import java.util.List;

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
        
        return savedReview;
    }
    
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + id));
    }
    
    public List<Review> getReviewsByCustomerId(int customerId) {
        return reviewRepository.findByCustomerId(customerId);
    }
    
    public List<Review> getReviewsByProviderServiceId(Long providerServiceId) {
        return reviewRepository.findByProviderServiceId(providerServiceId);
    }
    
    public List<Review> getReviewsByServiceProviderId(int serviceProviderId) {
        return reviewRepository.findByServiceProviderId(serviceProviderId);
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

        existingReview.setRating(reviewDTO.getRating());
        existingReview.setComment(reviewDTO.getComment());
        
        return reviewRepository.save(existingReview);
    }
    
    public void deleteReview(Long reviewId, Long customerId) {
        // Validate review exists and is owned by the customer
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + reviewId));

        if (existingReview.getCustomer().getId() != customerId.intValue()) {
            throw new ReviewValidationException("Customer does not own this review");
        }

        ServiceBooking booking = existingReview.getBooking();
        if (booking != null) {
            booking.setReview(null);
        }

        reviewRepository.deleteById(reviewId);
    }

    public Boolean existsByBookingId(Long bookingId) {
        // First verify that the booking exists
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        // Check if a review exists for this booking
        return reviewRepository.existsByBookingId(bookingId);
    }
}
