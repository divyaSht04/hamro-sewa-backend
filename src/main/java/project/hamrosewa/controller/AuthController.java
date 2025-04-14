package project.hamrosewa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.*;
import project.hamrosewa.exceptions.OTPVerificationException;
import project.hamrosewa.model.User;
import project.hamrosewa.repository.UserRepository;
import project.hamrosewa.service.*;
import project.hamrosewa.util.JWTUtil;
import project.hamrosewa.util.ValidationUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

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

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OTPVerificationService otpVerificationService;

    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;

    @Autowired
    public AuthController(UserRepository userRepository,
                          CustomerService customerService,
                          ServiceProviderService serviceProviderService,
                          AdminService adminService,
                          AuthenticationManager authenticationManager,
                          TokenBlackListService tokenBlackListService,
                          EmailService emailService,
                          OTPVerificationService otpVerificationService,
                          ObjectMapper objectMapper,
                          HttpServletRequest request) {
        this.userRepository = userRepository;
        this.customerService = customerService;
        this.serviceProviderService = serviceProviderService;
        this.adminService = adminService;
        this.authenticationManager = authenticationManager;
        this.tokenBlackListService = tokenBlackListService;
        this.emailService = emailService;
        this.otpVerificationService = otpVerificationService;
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO user) {
        try {
            if (user.getEmail() == null || user.getPassword() == null) {
                return ResponseEntity.badRequest()
                        .body("Email and password are required");
            }

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
                );

                if (authentication.isAuthenticated()) {
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

                    // Send login success email notification
                    try {
                        String loginTime = java.time.LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss"));
                        emailService.sendLoginSuccessEmail(
                                userEntity.getEmail(),
                                userEntity.getUsername(),
                                loginTime
                        );
                    } catch (Exception e) {
                        // Log error but don't block login process if email fails
                        System.out.println("Failed to send login email notification: " + e.getMessage());
                    }

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

    @PostMapping("/initiate-customer-registration")
    public ResponseEntity<?> initiateCustomerRegistration(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam("dateOfBirth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        ValidationUtil.validateCustomer(username, email, password, phoneNumber, address, fullName);

        boolean emailExists = userRepository.findByEmail(email).isPresent();
        boolean usernameExists = userRepository.findByUsername(username).isPresent();

        if (emailExists) {
            return new ResponseEntity<>("Email already registered", HttpStatus.CONFLICT);
        }

        if (usernameExists) {
            return new ResponseEntity<>("Username already taken", HttpStatus.CONFLICT);
        }

        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setUsername(username);
        customerDTO.setEmail(email);
        customerDTO.setPassword(password);
        customerDTO.setPhoneNumber(phoneNumber);
        customerDTO.setDateOfBirth(dateOfBirth);
        customerDTO.setAddress(address);
        customerDTO.setFullName(fullName);

        if (image != null && !image.isEmpty()) {
            customerDTO.setImage(image);
        }

        try {
            otpVerificationService.sendOTPForCustomerRegistration(customerDTO);
            return new ResponseEntity<>(
                    "Verification OTP sent to your email. Please verify to complete registration",
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Failed to send verification OTP: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @PostMapping(value = "/verify-customer-registration")
    public ResponseEntity<?> verifyCustomerRegistration(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "otp", required = false) String otp,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        OTPVerificationDTO verificationDTO = new OTPVerificationDTO();

        try {
            if (email != null && otp != null) {
                verificationDTO.setEmail(email);
                verificationDTO.setOtp(otp);
            } else {
                try {
                    verificationDTO = objectMapper.readValue(request.getInputStream(), OTPVerificationDTO.class);
                } catch (IOException ex) {
                    return new ResponseEntity<>("Missing verification data: email and OTP are required", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return new ResponseEntity<>("Invalid verification data format: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
        try {
            CustomerDTO customerDTO = otpVerificationService.verifyCustomerOTP(
                    verificationDTO.getEmail(),
                    verificationDTO.getOtp()
            );

            if (image != null && !image.isEmpty()) {
                customerDTO.setImage(image);
            }

            customerService.registerCustomer(customerDTO);

            // Send registration confirmation email
            try {
                String registrationDate = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
                emailService.sendCustomerRegistrationEmail(
                        customerDTO.getEmail(),
                        customerDTO.getUsername(),
                        registrationDate
                );
            } catch (Exception e) {
                // Log error but continue with registration confirmation
                System.out.println("Failed to send customer registration email: " + e.getMessage());
            }

            return new ResponseEntity<>("Customer registered successfully!", HttpStatus.CREATED);
        } catch (OTPVerificationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return new ResponseEntity<>(
                    "Registration failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
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

    /**
     * Step 1: Initiate service provider registration and send OTP
     */
    @PostMapping("/initiate-provider-registration")
    public ResponseEntity<?> initiateServiceProviderRegistration(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam("businessName") String businessName,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        // Validate service provider input
        ValidationUtil.validateServiceProvider(username, email, password, phoneNumber, address, businessName);

        // Check if email or username is already taken
        boolean emailExists = userRepository.findByEmail(email).isPresent();
        boolean usernameExists = userRepository.findByUsername(username).isPresent();

        if (emailExists) {
            return new ResponseEntity<>("Email already registered", HttpStatus.CONFLICT);
        }

        if (usernameExists) {
            return new ResponseEntity<>("Username already taken", HttpStatus.CONFLICT);
        }

        // If validation passes, create the service provider DTO
        ServiceProviderDTO serviceProviderDTO = new ServiceProviderDTO();
        serviceProviderDTO.setUsername(username);
        serviceProviderDTO.setEmail(email);
        serviceProviderDTO.setPassword(password);
        serviceProviderDTO.setPhoneNumber(phoneNumber);
        serviceProviderDTO.setAddress(address);
        serviceProviderDTO.setBusinessName(businessName);

        // Handle image if present
        if (image != null && !image.isEmpty()) {
            serviceProviderDTO.setImage(image);
        }

        // Generate and send OTP
        try {
            otpVerificationService.sendOTPForServiceProviderRegistration(serviceProviderDTO);
            return new ResponseEntity<>(
                    "Verification OTP sent to your email. Please verify to complete registration",
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Failed to send verification OTP: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Step 2: Verify OTP and complete service provider registration
     * <p>
     * Note: Frontend needs to re-upload the image during verification as it cannot be stored with OTP
     */
    @PostMapping(value = "/verify-provider-registration")
    public ResponseEntity<?> verifyServiceProviderRegistration(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "otp", required = false) String otp,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        OTPVerificationDTO verificationDTO = new OTPVerificationDTO();
        try {
            // If the parameters are directly in the form
            if (email != null && otp != null) {
                verificationDTO.setEmail(email);
                verificationDTO.setOtp(otp);
            } else {
                // Fallback to try reading the body as JSON
                try {
                    verificationDTO = objectMapper.readValue(request.getInputStream(), OTPVerificationDTO.class);
                } catch (IOException ex) {
                    return new ResponseEntity<>("Missing verification data: email and OTP are required", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return new ResponseEntity<>("Invalid verification data format: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
        try {
            // Verify OTP and get stored service provider data
            ServiceProviderDTO serviceProviderDTO = otpVerificationService.verifyServiceProviderOTP(
                    verificationDTO.getEmail(),
                    verificationDTO.getOtp()
            );

            // Re-attach the image if provided in this request
            if (image != null && !image.isEmpty()) {
                serviceProviderDTO.setImage(image);
            }

            // Register the service provider
            serviceProviderService.registerServiceProvider(serviceProviderDTO);

            // Send registration confirmation email
            try {
                String registrationDate = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
                emailService.sendProviderRegistrationEmail(
                        serviceProviderDTO.getEmail(),
                        serviceProviderDTO.getUsername(),
                        serviceProviderDTO.getBusinessName(),
                        registrationDate
                );
            } catch (Exception e) {
                // Log error but continue with registration confirmation
                System.out.println("Failed to send service provider registration email: " + e.getMessage());
            }

            return new ResponseEntity<>("Service Provider registered successfully!", HttpStatus.CREATED);
        } catch (OTPVerificationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return new ResponseEntity<>(
                    "Registration failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
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
