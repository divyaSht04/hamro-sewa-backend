package project.hamrosewa.util;

import project.hamrosewa.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for validating user input
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+977|0)?9[6-9]\\d{8}$");

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._]{4,20}$");

    public static void validateEmail(String email, List<String> errors) {
        if (email == null || email.isEmpty()) {
            errors.add("Email is required");
            return;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Invalid email format");
        }
    }

    public static void validatePassword(String password, List<String> errors) {
        if (password == null || password.isEmpty()) {
            errors.add("Password is required");
            return;
        }
        
        if (password.length() < 8) {
            errors.add("Password must be at least 8 characters long");
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            errors.add("Password must contain at least one digit, one lowercase, one uppercase letter, and one special character");
        }
    }
    

    public static void validatePhoneNumber(String phoneNumber, List<String> errors) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            errors.add("Phone number is required");
            return;
        }
        
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            errors.add("Invalid phone number format. Must be a valid Nepali phone number (e.g., 9801234567 or +9779801234567)");
        }
    }

    public static void validateUsername(String username, List<String> errors) {
        if (username == null || username.isEmpty()) {
            errors.add("Username is required");
            return;
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            errors.add("Username must be 4-20 characters and can only contain letters, numbers, periods, and underscores");
        }
    }
    

    public static void validateNotEmpty(String value, String fieldName, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(fieldName + " is required");
        }
    }
    public static void validateCustomer(String username, String email, String password, String phoneNumber, String address, String fullName) throws ValidationException {
        List<String> errors = new ArrayList<>();
        
        validateUsername(username, errors);
        validateEmail(email, errors);
        validatePassword(password, errors);
        validatePhoneNumber(phoneNumber, errors);
        validateNotEmpty(address, "Address", errors);
        validateNotEmpty(fullName, "Full name", errors);
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public static void validateServiceProvider(String username, String email, String password, String phoneNumber, String address, String businessName) throws ValidationException {
        List<String> errors = new ArrayList<>();
        
        validateUsername(username, errors);
        validateEmail(email, errors);
        validatePassword(password, errors);
        validatePhoneNumber(phoneNumber, errors);
        validateNotEmpty(address, "Address", errors);
        validateNotEmpty(businessName, "Business name", errors);
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
