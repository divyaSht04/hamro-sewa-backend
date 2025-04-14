package project.hamrosewa.dto;

import lombok.Data;

@Data
public class OTPVerificationDTO {
    private String email;
    private String otp;
}
