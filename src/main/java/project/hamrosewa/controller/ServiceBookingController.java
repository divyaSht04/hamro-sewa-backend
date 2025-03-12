package project.hamrosewa.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.hamrosewa.dto.ServiceBookingDTO;
import project.hamrosewa.model.BookingStatus;
import project.hamrosewa.model.ServiceBooking;
import project.hamrosewa.service.ServiceBookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class ServiceBookingController {

    @Autowired
    private ServiceBookingService bookingService;
    
    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody ServiceBookingDTO bookingDTO) {
        try {
            ServiceBooking booking = bookingService.createBooking(bookingDTO);
            return new ResponseEntity<>(booking, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        try {
            ServiceBooking booking = bookingService.getBookingById(id);
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getBookingsByCustomerId(@PathVariable int customerId) {
        List<ServiceBooking> bookings = bookingService.getBookingsByCustomerId(customerId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }
    
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<?> getBookingsByServiceId(@PathVariable Long serviceId) {
        List<ServiceBooking> bookings = bookingService.getBookingsByProviderServiceId(serviceId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }
    
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getBookingsByProviderId(@PathVariable int providerId) {
        List<ServiceBooking> bookings = bookingService.getBookingsByServiceProviderId(providerId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @RequestParam BookingStatus status) {
        try {
            ServiceBooking booking = bookingService.updateBookingStatus(id, status);
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            bookingService.cancelBooking(id);
            return new ResponseEntity<>("Booking cancelled successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
