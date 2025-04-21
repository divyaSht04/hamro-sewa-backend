package project.hamrosewa.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.hamrosewa.dto.ReviewDTO;
import project.hamrosewa.exceptions.ReviewValidationException;
import project.hamrosewa.model.Review;
import project.hamrosewa.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;    
    
    @PostMapping
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            Review review = reviewService.createReview(reviewDTO);
            return new ResponseEntity<>(review, HttpStatus.CREATED);
        } catch (ReviewValidationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        try {
            Review review = reviewService.getReviewById(id);
            return new ResponseEntity<>(review, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getReviewsByCustomerId(@PathVariable int customerId) {
        List<Review> reviews = reviewService.getReviewsByCustomerId(customerId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
    
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<?> getReviewsByServiceId(@PathVariable Long serviceId) {
        List<Review> reviews = reviewService.getReviewsByProviderServiceId(serviceId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
    
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getReviewsByProviderId(@PathVariable int providerId) {
        List<Review> reviews = reviewService.getReviewsByServiceProviderId(providerId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
    
    @GetMapping("/service/{serviceId}/rating")
    public ResponseEntity<?> getAverageRatingForService(@PathVariable Long serviceId) {
        Double averageRating = reviewService.calculateAverageRating(serviceId);
        if (averageRating == null) {
            return new ResponseEntity<>("No ratings available for this service", HttpStatus.OK);
        }
        return new ResponseEntity<>(averageRating, HttpStatus.OK);
    }
    
    @GetMapping("/booking/{bookingId}/exists")
    public ResponseEntity<Boolean> checkReviewExistsForBooking(@PathVariable Long bookingId) {
        boolean exists = reviewService.existsByBookingId(bookingId);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }
    
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getReviewByBookingId(@PathVariable Long bookingId) {
        try {
            Review review = reviewService.getReviewByBookingId(bookingId);
            return new ResponseEntity<>(review, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewDTO reviewDTO) {
        try {
            Review updatedReview = reviewService.updateReview(id, reviewDTO);
            return new ResponseEntity<>(updatedReview, HttpStatus.OK);
        } catch (ReviewValidationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, @RequestParam Long customerId) {
        try {
            reviewService.deleteReview(id, customerId);
            return new ResponseEntity<>("Review deleted successfully", HttpStatus.OK);
        } catch (ReviewValidationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
