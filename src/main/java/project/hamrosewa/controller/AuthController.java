package project.hamrosewa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.AdminDTO;
import project.hamrosewa.dto.CustomerDTO;
import project.hamrosewa.dto.ServiceProviderDTO;
import project.hamrosewa.service.AdminService;
import project.hamrosewa.service.CustomerService;
import project.hamrosewa.service.ServiceProviderService;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ServiceProviderService serviceProviderService;

    @Autowired
    private AdminService adminService;

    @PostMapping("/register-customer")
    public ResponseEntity<?> registerCustomer(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam("dateOfBirth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setUsername(username);
        customerDTO.setEmail(email);
        customerDTO.setPassword(password);
        customerDTO.setPhoneNumber(phoneNumber);
        customerDTO.setDateOfBirth(dateOfBirth);
        customerDTO.setImage(image);
        customerDTO.setAddress(address);
        customerDTO.setFullName(fullName);

        customerService.registerCustomer(customerDTO);
        return new ResponseEntity<>("Customer registered successfully!", HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/profile-image")
    public ResponseEntity<byte[]> getCustomerProfileImage(@PathVariable int userId) {
        try {
            byte[] imageData = customerService.getCustomerProfileImage(userId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/register-service-provider")
    public ResponseEntity<?> registerServiceProvider(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam("businessName") String businessName,
            @RequestParam("serviceCategory") String serviceCategory,
            @RequestParam("description") String description,
            @RequestParam("hourlyRate") Double hourlyRate,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        ServiceProviderDTO serviceProviderDTO = new ServiceProviderDTO();
        serviceProviderDTO.setUsername(username);
        serviceProviderDTO.setEmail(email);
        serviceProviderDTO.setPassword(password);
        serviceProviderDTO.setPhoneNumber(phoneNumber);
        serviceProviderDTO.setAddress(address);
        serviceProviderDTO.setBusinessName(businessName);
        serviceProviderDTO.setServiceCategory(serviceCategory);
        serviceProviderDTO.setDescription(description);
        serviceProviderDTO.setHourlyRate(hourlyRate);
        serviceProviderDTO.setImage(image);

        serviceProviderService.registerServiceProvider(serviceProviderDTO);
        return new ResponseEntity<>("Service Provider registered successfully!", HttpStatus.CREATED);
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam("dateOfBirth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam("fullName") String fullName,
            @RequestParam("department") String department,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setUsername(username);
        adminDTO.setEmail(email);
        adminDTO.setPassword(password);
        adminDTO.setPhoneNumber(phoneNumber);
        adminDTO.setDateOfBirth(dateOfBirth);
        adminDTO.setImage(image);
        adminDTO.setAddress(address);
        adminDTO.setFullName(fullName);


        adminService.registerAdmin(adminDTO);
        return new ResponseEntity<>("Admin registered successfully!", HttpStatus.CREATED);
    }
}
