package project.hamrosewa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.ServiceProviderDTO;
import project.hamrosewa.service.ServiceProviderService;

import java.io.IOException;

@RestController
@RequestMapping("/serviceProvider")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderService serviceProvider;

    @PutMapping("/edit-customer/{serviceProviderId}")
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

//        serviceProvider.updateCustomer(serviceProviderId, serviceProviderDTO);
        return new ResponseEntity<>("Profile updated successfully!", HttpStatus.OK);
    }

    @PostMapping("/upload-photo/{customerId}")
    public ResponseEntity<?> uploadPhoto(@PathVariable long customerId,
                                         @RequestParam("photo") MultipartFile photo)
            throws IOException {
//        serviceProvider.updateCustomerPhoto(customerId, photo);
        return ResponseEntity.ok("Photo updated successfully");
    }

}
