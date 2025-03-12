package project.hamrosewa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.CustomerDTO;
import project.hamrosewa.model.Customer;
import project.hamrosewa.service.CustomerService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/info/{id}")
    public ResponseEntity<?> getCustomerInfo(@PathVariable int id) {
        List<Customer> customerInfo = customerService.getCustomerInfo(id);
        return new ResponseEntity<>(customerInfo, HttpStatus.OK);
    }

    @PutMapping("/edit-customer/{customerId}")
    public ResponseEntity<?> editCustomer(
            @PathVariable long customerId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestPart(required = false) MultipartFile image
    ) throws IOException {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setUsername(username);
        customerDTO.setEmail(email);
        customerDTO.setPhoneNumber(phoneNumber);
        customerDTO.setAddress(address);
        customerDTO.setFullName(fullName);
        customerDTO.setDateOfBirth(dateOfBirth);
        customerDTO.setImage(image);

        customerService.updateCustomer(customerId, customerDTO);
        return new ResponseEntity<>("Profile updated successfully!", HttpStatus.OK);
    }

    @PostMapping("/upload-photo/{customerId}")
    public ResponseEntity<?> uploadPhoto(@PathVariable long customerId, @RequestParam("photo") MultipartFile photo) throws IOException {
        customerService.updateCustomerPhoto(customerId, photo);
        return ResponseEntity.ok("Photo updated successfully");
    }
}
