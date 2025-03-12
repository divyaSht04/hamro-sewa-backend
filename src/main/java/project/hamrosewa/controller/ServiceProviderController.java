package project.hamrosewa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.ServiceProviderDTO;
import project.hamrosewa.model.ServiceProvider;
import project.hamrosewa.service.ServiceProviderService;

import java.io.IOException;
import java.util.List;

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
}
