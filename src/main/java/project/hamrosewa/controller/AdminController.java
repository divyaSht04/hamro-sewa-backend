package project.hamrosewa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.AdminDTO;
import project.hamrosewa.dto.CustomerDTO;
import project.hamrosewa.model.Admin;
import project.hamrosewa.model.BookingStatus;
import project.hamrosewa.model.ProviderService;
import project.hamrosewa.model.ServiceStatus;
import project.hamrosewa.repository.AdminRepository;
import project.hamrosewa.repository.UserRepository;
import project.hamrosewa.service.AdminService;
import project.hamrosewa.service.ProviderServiceService;
import project.hamrosewa.service.ServiceBookingService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private final AdminService adminService;
    
    @Autowired
    private final ProviderServiceService providerService;

    @Autowired
    private final ServiceBookingService bookingService;

    @GetMapping("/info/{adminId}")
    public ResponseEntity<?> getAdminInfo(@PathVariable long adminId) {
        try {
            Map<String, Object> adminInfo = adminService.getAdminInfo(adminId);
            return new ResponseEntity<>(adminInfo, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/edit-admin/{adminId}")
    public ResponseEntity<?> editAdmin(
            @PathVariable int adminId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestPart(required = false) MultipartFile image
    ) throws IOException {
        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setUsername(username);
        adminDTO.setEmail(email);
        adminDTO.setAddress(address);
        adminDTO.setPhoneNumber(phoneNumber);
        adminDTO.setFullName(fullName);
        adminDTO.setDepartment(department);
        adminDTO.setDateOfBirth(dateOfBirth);
        adminDTO.setImage(image);

        adminService.updateAdmin(adminId, adminDTO);
        return new ResponseEntity<>("Admin Updated Successfully!",HttpStatus.OK);
    }

    @GetMapping("/earnings")
    public ResponseEntity<?> getAdminEarnings() {
        try {
            BigDecimal earningsInfo = adminService.calculateAdminEarnings();
            return new ResponseEntity<>(earningsInfo, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to calculate admin earnings");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
