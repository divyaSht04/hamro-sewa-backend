package project.hamrosewa.exceptions;

public class OTPVerificationException extends RuntimeException {
    
    public OTPVerificationException(String message) {
        super(message);
    }
    
    public OTPVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
