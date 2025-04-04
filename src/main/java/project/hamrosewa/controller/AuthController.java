package project.hamrosewa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.AdminDTO;
import project.hamrosewa.dto.CustomerDTO;
import project.hamrosewa.dto.ServiceProviderDTO;
import project.hamrosewa.dto.UserDTO;
import project.hamrosewa.model.User;
import project.hamrosewa.repository.UserRepository;
import project.hamrosewa.service.AdminService;
import project.hamrosewa.service.CustomerService;
import project.hamrosewa.service.ServiceProviderService;
import project.hamrosewa.service.TokenBlackListService;
import project.hamrosewa.util.JWTUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private ServiceProviderService serviceProviderService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenBlackListService tokenBlackListService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO user){
        try{
            if (user.getEmail() == null || user.getPassword() == null) {
                return ResponseEntity.badRequest()
                    .body("Email and password are required");
            }

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
                );

                if (authentication.isAuthenticated()){
                    String token = jwtUtil.generateToken(user.getEmail());
                    org.springframework.security.core.userdetails.UserDetails userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

                    // Get the user from repository to get the ID
                    User userEntity = userRepository.findByEmail(user.getEmail())
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                    Map<String, Object> response = new HashMap<>();
                    response.put("token", token);
                    response.put("user", Map.of(
                        "id", userEntity.getId(),
                        "email", user.getEmail(),
                        "username", userEntity.getUsername(),
                        "roles", userDetails.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                    ));

                    return ResponseEntity.ok(response);
                }
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
                
            } catch (BadCredentialsException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("The email and password combination is incorrect. Please try again.");
            } catch (UsernameNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("The email address is not registered. Please check your email address and try again.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred during login: " + e.getMessage());
        }
    }

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

            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        ServiceProviderDTO serviceProviderDTO = new ServiceProviderDTO();
        serviceProviderDTO.setUsername(username);
        serviceProviderDTO.setEmail(email);
        serviceProviderDTO.setPassword(password);
        serviceProviderDTO.setPhoneNumber(phoneNumber);
        serviceProviderDTO.setAddress(address);
        serviceProviderDTO.setBusinessName(businessName);
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlackListService.blacklistToken(token);
            return ResponseEntity.ok("Logged out successfully");
        }
        return ResponseEntity.badRequest().body("Invalid token format");
    }
}
