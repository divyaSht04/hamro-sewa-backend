package project.hamrosewa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.hamrosewa.dto.CustomerDTO;
import project.hamrosewa.dto.ServiceProviderDTO;
import project.hamrosewa.exceptions.OTPVerificationException;
import project.hamrosewa.model.OTPVerification;
import project.hamrosewa.model.OTPVerification.UserType;
import project.hamrosewa.repository.OTPVerificationRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OTPVerificationService {

    @Autowired
    private OTPVerificationRepository otpRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    

    @Transactional
    public void sendOTPForCustomerRegistration(CustomerDTO customerDTO) {
        try {
            deleteExistingOTP(customerDTO.getEmail());

            String otp = generateOTP();

            CustomerDTO dtoCopy = new CustomerDTO();
            dtoCopy.setUsername(customerDTO.getUsername());
            dtoCopy.setEmail(customerDTO.getEmail());
            dtoCopy.setPassword(customerDTO.getPassword());
            dtoCopy.setPhoneNumber(customerDTO.getPhoneNumber());
            dtoCopy.setAddress(customerDTO.getAddress());
            dtoCopy.setDateOfBirth(customerDTO.getDateOfBirth());
            dtoCopy.setFullName(customerDTO.getFullName());

            OTPVerification otpVerification = new OTPVerification();
            otpVerification.setEmail(customerDTO.getEmail());
            otpVerification.setOtp(otp);
            otpVerification.setCreatedAt(LocalDateTime.now());
            otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
            otpVerification.setVerified(false);
            otpVerification.setUserType(UserType.CUSTOMER);

            otpVerification.setRegistrationData(objectMapper.writeValueAsString(dtoCopy));

            otpRepository.save(otpVerification);

            sendOTPEmail(customerDTO.getEmail(), customerDTO.getUsername(), otp);
            
        } catch (Exception e) {
            e.printStackTrace(); // Add this for debugging
            throw new OTPVerificationException("Failed to generate and send OTP: " + e.getMessage());
        }
    }

    @Transactional
    public void sendOTPForServiceProviderRegistration(ServiceProviderDTO providerDTO) {
        try {
            deleteExistingOTP(providerDTO.getEmail());

            String otp = generateOTP();

            ServiceProviderDTO dtoCopy = new ServiceProviderDTO();
            dtoCopy.setUsername(providerDTO.getUsername());
            dtoCopy.setEmail(providerDTO.getEmail());
            dtoCopy.setPassword(providerDTO.getPassword());
            dtoCopy.setPhoneNumber(providerDTO.getPhoneNumber());
            dtoCopy.setAddress(providerDTO.getAddress());
            dtoCopy.setBusinessName(providerDTO.getBusinessName());

            OTPVerification otpVerification = new OTPVerification();
            otpVerification.setEmail(providerDTO.getEmail());
            otpVerification.setOtp(otp);
            otpVerification.setCreatedAt(LocalDateTime.now());
            otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
            otpVerification.setVerified(false);
            otpVerification.setUserType(UserType.SERVICE_PROVIDER);

            otpVerification.setRegistrationData(objectMapper.writeValueAsString(dtoCopy));
            
            // Save the OTP verification record
            otpRepository.save(otpVerification);

            sendOTPEmail(providerDTO.getEmail(), providerDTO.getUsername(), otp);
            
        } catch (Exception e) {
            e.printStackTrace(); // Add this for debugging
            throw new OTPVerificationException("Failed to generate and send OTP: " + e.getMessage());
        }
    }

    @Transactional
    public CustomerDTO verifyCustomerOTP(String email, String otp) {
        try {
            OTPVerification verification = verifyOTP(email, otp, UserType.CUSTOMER);
            return objectMapper.readValue(verification.getRegistrationData(), CustomerDTO.class);
            
        } catch (Exception e) {
            e.printStackTrace(); // Add for debugging
            throw new OTPVerificationException("OTP verification failed: " + e.getMessage());
        }
    }

    @Transactional
    public ServiceProviderDTO verifyServiceProviderOTP(String email, String otp) {
        try {
            OTPVerification verification = verifyOTP(email, otp, UserType.SERVICE_PROVIDER);

            return objectMapper.readValue(verification.getRegistrationData(), ServiceProviderDTO.class);
            
        } catch (Exception e) {
            e.printStackTrace(); // Add for debugging
            throw new OTPVerificationException("OTP verification failed: " + e.getMessage());
        }
    }

    private OTPVerification verifyOTP(String email, String otp, UserType userType) {
        Optional<OTPVerification> verificationOpt = otpRepository.findByEmailAndOtpAndVerifiedFalse(email, otp);
        
        if (verificationOpt.isEmpty()) {
            throw new OTPVerificationException("Invalid OTP");
        }
        
        OTPVerification verification = verificationOpt.get();

        if (verification.isExpired()) {
            throw new OTPVerificationException("OTP has expired");
        }

        if (verification.getUserType() != userType) {
            throw new OTPVerificationException("Invalid user type for OTP");
        }

        verification.setVerified(true);
        otpRepository.save(verification);
        
        return verification;
    }
    

    private String generateOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }

    private void deleteExistingOTP(String email) {
        Optional<OTPVerification> existingOTP = otpRepository.findByEmailAndVerifiedFalse(email);
        existingOTP.ifPresent(otpRepository::delete);
    }

    private void sendOTPEmail(String email, String username, String otp) {
        try {
            emailService.sendOTPVerificationEmail(email, username, otp, OTP_EXPIRY_MINUTES);
        } catch (Exception e) {
            throw new OTPVerificationException("Failed to send OTP email: " + e.getMessage());
        }
    }
}
