package project.hamrosewa.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({OTPVerificationException.class})
    public ResponseEntity<Map<String, Object>> handleOTPVerificationException(OTPVerificationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({UserValidationException.class})
    public ResponseEntity<?> handleUserValidationException(UserValidationException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler({RoleNotFoundException.class})
    public ResponseEntity<?> handleRoleException(RoleNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({JwtTokenException.class})
    public ResponseEntity<String> handleJWTTokenException(JwtTokenException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler({ProviderServiceException.class})
    public ResponseEntity<String> handleProviderServiceException(ProviderServiceException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    
    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Validation failed");
        response.put("errors", ex.getErrors());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
