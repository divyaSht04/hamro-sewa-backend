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
        Customer customer = customerRepository.findById(reviewDTO.getCustomerId())
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
        
        return reviewRepository.save(review);
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
}
