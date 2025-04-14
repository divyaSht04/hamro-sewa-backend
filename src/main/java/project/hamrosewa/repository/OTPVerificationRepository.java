package project.hamrosewa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.hamrosewa.model.OTPVerification;

import java.util.Optional;

@Repository
public interface OTPVerificationRepository extends JpaRepository<OTPVerification, Long> {
    Optional<OTPVerification> findByEmailAndVerifiedFalse(String email);
    Optional<OTPVerification> findByEmailAndOtpAndVerifiedFalse(String email, String otp);
}
