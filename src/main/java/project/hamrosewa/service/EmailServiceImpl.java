package project.hamrosewa.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of email service for sending various notifications
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm");

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.sender.name}")
    private String senderName;

    @Value("${app.email.sender.address}")
    private String senderAddress;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(String.format("%s <%s>", senderName, senderAddress));
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            logger.info("Simple email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send simple email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(senderAddress, senderName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("HTML email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send HTML email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendLoginSuccessEmail(String to, String name, String loginTime) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("name", name);
            templateModel.put("loginTime", loginTime);
            templateModel.put("currentYear", LocalDateTime.now().getYear());
            
            String htmlContent = processTemplate("login-success", templateModel);
            sendHtmlEmail(to, "Successful Login to HamroSewa", htmlContent);
            
            logger.info("Login success email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send login success email: {}", e.getMessage());
        }
    }

    @Override
    @Async
    public void sendBookingConfirmationEmail(String to, String name, Long bookingId, String serviceName, 
                                           String bookingDateTime, String price, String providerName) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("name", name);
            templateModel.put("bookingId", bookingId);
            templateModel.put("serviceName", serviceName);
            templateModel.put("bookingDateTime", bookingDateTime);
            templateModel.put("price", price);
            templateModel.put("providerName", providerName);
            templateModel.put("currentYear", LocalDateTime.now().getYear());
            
            String htmlContent = processTemplate("booking-confirmation", templateModel);
            sendHtmlEmail(to, "Booking Confirmation - Hamro Sewa", htmlContent);
            
            logger.info("Booking confirmation email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send booking confirmation email: {}", e.getMessage());
        }
    }

    @Override
    @Async
    public void sendBookingCompletionEmail(String to, String username, Long bookingId, String serviceName, String completionTime) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("username", username);
            templateModel.put("bookingId", bookingId);
            templateModel.put("serviceName", serviceName);
            templateModel.put("completionTime", completionTime);
            templateModel.put("currentYear", LocalDateTime.now().getYear());
            
            String htmlContent = processTemplate("booking-completion", templateModel);
            sendHtmlEmail(to, "Your Service Has Been Completed", htmlContent);
            
            logger.info("Booking completion email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send booking completion email: {}", e.getMessage());
            throw new RuntimeException("Failed to send booking completion email", e);
        }
    }
    
    @Async
    @Override
    public void sendCustomerRegistrationEmail(String to, String username, String registrationDate) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("username", username);
            templateModel.put("email", to);
            templateModel.put("registrationDate", registrationDate);
            templateModel.put("currentYear", LocalDateTime.now().getYear());
            
            String htmlContent = processTemplate("registration-success-customer", templateModel);
            sendHtmlEmail(to, "Welcome to HamroSewa!", htmlContent);
            
            logger.info("Customer registration success email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send customer registration email: {}", e.getMessage());
            throw new RuntimeException("Failed to send customer registration email", e);
        }
    }
    
    @Async
    @Override
    public void sendProviderRegistrationEmail(String to, String username, String businessName, String registrationDate) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("username", username);
            templateModel.put("email", to);
            templateModel.put("businessName", businessName);
            templateModel.put("registrationDate", registrationDate);
            templateModel.put("currentYear", LocalDateTime.now().getYear());
            
            String htmlContent = processTemplate("registration-success-provider", templateModel);
            sendHtmlEmail(to, "Welcome to HamroSewa as a Service Provider!", htmlContent);
            
            logger.info("Service provider registration success email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send service provider registration email: {}", e.getMessage());
            throw new RuntimeException("Failed to send service provider registration email", e);
        }
    }

    /**
     * Process a Thymeleaf template with the provided model
     * 
     * @param templateName name of the template (without extension)
     * @param model variables to use in the template
     * @return processed HTML string
     */
    private String processTemplate(String templateName, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return templateEngine.process("emails/" + templateName, context);
    }
    
    @Override
    @Async
    public void sendOTPVerificationEmail(String to, String username, String otp, int expiryMinutes) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("username", username);
            templateModel.put("otp", otp);
            templateModel.put("expiryMinutes", expiryMinutes);
            templateModel.put("currentYear", LocalDateTime.now().getYear());
            
            String htmlContent = processTemplate("email-verification", templateModel);
            sendHtmlEmail(to, "Email Verification - HamroSewa", htmlContent);
            
            logger.info("OTP verification email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send OTP verification email: {}", e.getMessage());
            // No exception re-throw to prevent stack overflow in @Async method
        }
    }
}
