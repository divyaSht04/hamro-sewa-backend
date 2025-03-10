package project.hamrosewa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.ProviderServiceDTO;
import project.hamrosewa.model.ProviderService;
import project.hamrosewa.model.ServiceStatus;
import project.hamrosewa.service.ProviderServiceService;
import project.hamrosewa.service.ServiceProviderService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/provider-services")
public class ProviderServiceController {

    @Autowired
    private ProviderServiceService providerServiceService;

    @Autowired
    private ServiceProviderService serviceProviderService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createService(
            @RequestParam("serviceProviderId") Long serviceProviderId,
            @RequestParam("serviceName") String serviceName,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("category") String category,
            @RequestPart(value = "pdf", required = false) MultipartFile pdf,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            ProviderServiceDTO serviceDTO = new ProviderServiceDTO();
            serviceDTO.setServiceName(serviceName);
            serviceDTO.setDescription(description);
            serviceDTO.setPrice(price);
            serviceDTO.setServiceProviderId(serviceProviderId);
            serviceDTO.setCategory(category);
            serviceDTO.setPdf(pdf);
            serviceDTO.setImage(image);

            ProviderService createdService = providerServiceService.createService(serviceDTO);
            return new ResponseEntity<>(createdService, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to create service: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/{serviceId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateService(
            @PathVariable Long serviceId,
            @RequestParam("serviceProviderId") Long serviceProviderId,
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) BigDecimal price,
            @RequestParam(value = "category", required = false) String category,
            @RequestPart(value = "pdf", required = false) MultipartFile pdf,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            ProviderServiceDTO serviceDTO = new ProviderServiceDTO();
            serviceDTO.setServiceName(serviceName);
            serviceDTO.setDescription(description);
            serviceDTO.setPrice(price);
            serviceDTO.setServiceProviderId(serviceProviderId);
            serviceDTO.setCategory(category);
            serviceDTO.setPdf(pdf);
            serviceDTO.setImage(image);

            ProviderService updatedService = providerServiceService.updateService(serviceId, serviceDTO);
            return new ResponseEntity<>(updatedService, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to update service: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<?> deleteService(@PathVariable Long serviceId) {
        try {
            providerServiceService.deleteService(serviceId);
            return new ResponseEntity<>("Service deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete service: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getProviderServices(@PathVariable Long providerId) {
        try {
            List<ProviderService> services = serviceProviderService.getProviderServices(providerId);
            return new ResponseEntity<>(services, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch services: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/{serviceId}")
    public ResponseEntity<?> getServiceById(@PathVariable Long serviceId) {
        try {
            ProviderService service = providerServiceService.getServiceById(serviceId);
            return new ResponseEntity<>(service, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch service: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/image/{serviceId}")
    public ResponseEntity<?> getServiceImage(@PathVariable Long serviceId) {
        try {
            ProviderService service = providerServiceService.getServiceById(serviceId);
            if (service.getImagePath() == null || service.getImagePath().isEmpty()) {
                return new ResponseEntity<>("No image found for this service", HttpStatus.NOT_FOUND);
            }
            
            byte[] imageData = providerServiceService.getServiceImage(service.getImagePath());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageData);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch service image: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/pdf/{serviceId}")
    public ResponseEntity<?> getServicePdf(@PathVariable Long serviceId) {
        try {
            ProviderService service = providerServiceService.getServiceById(serviceId);
            if (service.getPdfPath() == null || service.getPdfPath().isEmpty()) {
                return new ResponseEntity<>("No PDF found for this service", HttpStatus.NOT_FOUND);
            }
            
            byte[] pdfData = providerServiceService.getServicePdf(service.getPdfPath());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "inline; filename=\"" + service.getServiceName().replaceAll("\\s+", "_") + ".pdf\"")
                    .body(pdfData);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch service PDF: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Endpoint for admin to approve a service
     */
    @PutMapping("/approve/{serviceId}")
    public ResponseEntity<?> approveService(
            @PathVariable Long serviceId,
            @RequestParam(value = "feedback", required = false) String feedback
    ) {
        try {
            ProviderService service = providerServiceService.approveService(serviceId, feedback);
            return new ResponseEntity<>(service, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to approve service: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Endpoint for admin to reject a service
     */
    @PutMapping("/reject/{serviceId}")
    public ResponseEntity<?> rejectService(
            @PathVariable Long serviceId,
            @RequestParam(value = "feedback", required = true) String feedback
    ) {
        try {
            if (feedback == null || feedback.trim().isEmpty()) {
                return new ResponseEntity<>("Feedback is required when rejecting a service", HttpStatus.BAD_REQUEST);
            }
            
            ProviderService service = providerServiceService.rejectService(serviceId, feedback);
            return new ResponseEntity<>(service, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to reject service: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Endpoint to get all services with a specific status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getServicesByStatus(@PathVariable ServiceStatus status) {
        try {
            List<ProviderService> services = providerServiceService.getServicesByStatus(status);
            return new ResponseEntity<>(services, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch services: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Endpoint to get all services (for admin)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllServices() {
        try {
            List<ProviderService> services = providerServiceService.getAllServices();
            return new ResponseEntity<>(services, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch services: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
