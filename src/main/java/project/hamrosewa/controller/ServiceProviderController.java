package project.hamrosewa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.ServiceProviderDashboardMetricsDTO;
import project.hamrosewa.dto.PerformanceMetricsDTO;
import project.hamrosewa.dto.ServiceProviderDTO;
import project.hamrosewa.model.ServiceProvider;
import project.hamrosewa.service.ServiceProviderService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/service-providers")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderService serviceProviderService;

    @PutMapping("/edit-serviceProvider/{serviceProviderId}")
    public ResponseEntity<?> editServiceProvider(
            @PathVariable long serviceProviderId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String businessName,
            @RequestPart(required = false) MultipartFile image
    ) throws IOException {
        ServiceProviderDTO serviceProviderDTO = new ServiceProviderDTO();
        serviceProviderDTO.setUsername(username);
        serviceProviderDTO.setEmail(email);
        serviceProviderDTO.setPhoneNumber(phoneNumber);
        serviceProviderDTO.setAddress(address);
        serviceProviderDTO.setBusinessName(businessName);
        serviceProviderDTO.setImage(image);

        serviceProviderService.updateServiceProvider(serviceProviderId, serviceProviderDTO);
        return new ResponseEntity<>("Profile updated successfully!", HttpStatus.OK);
    }

    @PostMapping("/upload-photo/{customerId}")
    public ResponseEntity<?> uploadPhoto(@PathVariable long customerId,
                                         @RequestParam("photo") MultipartFile photo)
            throws IOException {
        serviceProviderService.updateServiceProviderPhoto(customerId, photo);
        return ResponseEntity.ok("Photo updated successfully");
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<?> getServiceProvider(@PathVariable long id) throws IOException {
        List<ServiceProvider> serviceProvider = serviceProviderService.getServiceProviderInfo(id);
        return new ResponseEntity<>(serviceProvider, HttpStatus.OK);
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<?> getServiceProviderImage(@PathVariable long id) throws IOException {
        try {
            byte[] image = serviceProviderService.getServiceProviderImage(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(image);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Dashboard metrics endpoints
     */
    
    @GetMapping("/dashboard-metrics/{providerId}")
    public ResponseEntity<ServiceProviderDashboardMetricsDTO> getDashboardMetrics(
            @PathVariable long providerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // If dates not provided, default to current month
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        ServiceProviderDashboardMetricsDTO metrics = serviceProviderService.getDashboardMetrics(providerId, startDate, endDate);
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/performance-metrics/{providerId}")
    public ResponseEntity<PerformanceMetricsDTO> getPerformanceMetrics(
            @PathVariable long providerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // If dates not provided, default to all-time
        if (startDate == null) {
            startDate = LocalDate.of(2000, 1, 1); // A date far in the past
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        PerformanceMetricsDTO metrics = serviceProviderService.getPerformanceMetrics(providerId, startDate, endDate);
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/monthly-earnings/{providerId}")
    public ResponseEntity<Map<String, Double>> getMonthlyEarnings(
            @PathVariable long providerId,
            @RequestParam(required = false) Integer year) {
        
        // If year not provided, default to current year
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        Map<String, Double> monthlyEarnings = serviceProviderService.getMonthlyEarnings(providerId, year);
        return ResponseEntity.ok(monthlyEarnings);
    }
    
    @GetMapping("/client-growth/{providerId}")
    public ResponseEntity<Map<String, Integer>> getClientGrowth(
            @PathVariable long providerId,
            @RequestParam(required = false) Integer year) {
        
        // If year not provided, default to current year
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        Map<String, Integer> clientGrowth = serviceProviderService.getClientGrowthByMonth(providerId, year);
        return ResponseEntity.ok(clientGrowth);
    }
    
    @GetMapping("/service-popularity/{providerId}")
    public ResponseEntity<Map<String, Integer>> getServicePopularity(@PathVariable long providerId) {
        Map<String, Integer> servicePopularity = serviceProviderService.getServicePopularity(providerId);
        return ResponseEntity.ok(servicePopularity);
    }
}
