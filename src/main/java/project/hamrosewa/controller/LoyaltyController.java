package project.hamrosewa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.hamrosewa.model.Customer;
import project.hamrosewa.model.ServiceBooking;
import project.hamrosewa.model.ServiceProvider;
import project.hamrosewa.repository.CustomerRepository;
import project.hamrosewa.repository.ServiceBookingRepository;
import project.hamrosewa.repository.ServiceProviderRepository;
import project.hamrosewa.service.LoyaltyService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {

    @Autowired
    private LoyaltyService loyaltyService;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;
    
    @Autowired
    private ServiceBookingRepository bookingRepository;
    
    /**
     * Get a customer's loyalty progress with a specific service provider
     * 
     * @param customerId the customer ID
     * @param serviceProviderId the service provider ID
     * @return the loyalty progress information
     */
    @GetMapping("/progress/{customerId}/{serviceProviderId}")
    public ResponseEntity<?> getLoyaltyProgress(
            @PathVariable Integer customerId,
            @PathVariable Integer serviceProviderId) {
        
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            ServiceProvider serviceProvider = serviceProviderRepository.findById(Long.valueOf(serviceProviderId))
                    .orElseThrow(() -> new RuntimeException("Service provider not found"));
            
            int completedBookings = loyaltyService.getCompletedBookingsCount(customer, serviceProvider);
            boolean eligibleForDiscount = loyaltyService.shouldApplyDiscount(customer, serviceProvider);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerId", customerId);
            response.put("serviceProviderId", serviceProviderId);
            response.put("completedBookings", completedBookings);
            response.put("bookingsNeededForDiscount", completedBookings >= 4 ? 0 : 4 - completedBookings);
            response.put("eligibleForDiscount", eligibleForDiscount);
            response.put("discountEligible", eligibleForDiscount); // Add discountEligible flag for frontend compatibility
            response.put("discountPercentage", 20);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get a customer's loyalty progress across all service providers
     * 
     * @param customerId the customer ID
     * @return the loyalty progress information for all service providers
     */
    @GetMapping("/progress/customer/{customerId}")
    public ResponseEntity<?> getAllLoyaltyProgress(@PathVariable Integer customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
                    
            List<ServiceProvider> providers = serviceProviderRepository.findAll();
            List<Map<String, Object>> results = new ArrayList<>();
            
            for (ServiceProvider provider : providers) {
                int completedBookings = loyaltyService.getCompletedBookingsCount(customer, provider);
                boolean eligibleForDiscount = loyaltyService.shouldApplyDiscount(customer, provider);
                
                Map<String, Object> providerProgress = new HashMap<>();
                providerProgress.put("serviceProviderId", provider.getId());
                providerProgress.put("serviceProviderName", provider.getUsername());
                providerProgress.put("completedBookings", completedBookings);
                providerProgress.put("bookingsNeededForDiscount", eligibleForDiscount ? 0 : 4 - completedBookings);
                providerProgress.put("eligibleForDiscount", eligibleForDiscount);
                
                results.add(providerProgress);
            }
            
            return new ResponseEntity<>(results, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Debug endpoint to fix the loyalty tracker data in the system
     * This will recalculate the loyalty status based on completed bookings
     * 
     * @param customerId the customer ID
     * @return status of the fix operation
     */
    @PostMapping("/fix/{customerId}")
    public ResponseEntity<?> fixLoyaltyTracking(@PathVariable Integer customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            List<ServiceProvider> providers = serviceProviderRepository.findAll();
            Map<String, Object> results = new HashMap<>();
            
            for (ServiceProvider provider : providers) {
                loyaltyService.resetLoyaltyCounter(customer, provider);

                int fixedCount = 0;
                for (ServiceBooking booking : bookingRepository.findCompletedBookingsByCustomerAndProvider(
                        customer.getId(), provider.getId())) {
                    loyaltyService.processCompletedBooking(booking);
                    fixedCount++;
                }
                
                Map<String, Object> providerResult = new HashMap<>();
                providerResult.put("provider", provider.getUsername());
                providerResult.put("fixedBookings", fixedCount);
                providerResult.put("newStatus", loyaltyService.getCompletedBookingsCount(customer, provider));
                results.put("provider_" + provider.getId(), providerResult);
            }
            
            return new ResponseEntity<>(results, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
