package project.hamrosewa.service;

import org.springframework.stereotype.Service;

/**
 * Interface for email notification service
 */
public interface EmailService {
    

    
    /**
     * Send an HTML formatted email
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param htmlContent email body as HTML
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);
    
    /**
     * Send login success notification email
     * 
     * @param to recipient email address
     * @param username user's name
     * @param loginTime time of login
     */
    void sendLoginSuccessEmail(String to, String username, String loginTime);
    
    /**
     * Send booking confirmation email
     * 
     * @param to recipient email address
     * @param username recipient name
     * @param bookingId booking identifier
     * @param serviceName name of the booked service
     * @param bookingDateTime date and time of the booking
     * @param price price of the service
     * @param providerName name of the service provider
     */
    void sendBookingConfirmationEmail(String to, String username, Long bookingId, String serviceName, 
                                     String bookingDateTime, String price, String providerName);
    
    /**
     * Send booking completion notification email
     * 
     * @param to recipient email address
     * @param username recipient name
     * @param bookingId booking identifier
     * @param serviceName name of the service
     * @param completionTime time of completion
     */
    void sendBookingCompletionEmail(String to, String username, Long bookingId, String serviceName, String completionTime);
    
    /**
     * Send registration success email to a new customer
     * 
     * @param to customer's email address
     * @param username customer's username
     * @param registrationDate date of registration
     */
    void sendCustomerRegistrationEmail(String to, String username, String registrationDate);
    
    /**
     * Send registration success email to a new service provider
     * 
     * @param to service provider's email address
     * @param username service provider's username
     * @param businessName service provider's business name
     * @param registrationDate date of registration
     */
    void sendProviderRegistrationEmail(String to, String username, String businessName, String registrationDate);
    
    /**
     * Send email verification OTP
     * 
     * @param to recipient email address
     * @param username recipient's username
     * @param otp the one-time password for verification
     * @param expiryMinutes minutes until the OTP expires
     */
    void sendOTPVerificationEmail(String to, String username, String otp, int expiryMinutes);
    
    /**
     * Send notification to service provider when their service is deleted by an admin
     * 
     * @param to service provider's email address
     * @param username service provider's username
     * @param serviceName name of the deleted service
     * @param reason reason for deletion (optional)
     */
    void sendServiceDeletionEmail(String to, String username, String serviceName, String reason);
}
