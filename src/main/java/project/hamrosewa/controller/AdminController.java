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
import project.hamrosewa.service.AdminService;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private final AdminService adminService;

    @PutMapping("/edit-admin/{adminId}")
    public ResponseEntity<?> editAdmin(
            @PathVariable int adminId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestPart(required = false) MultipartFile image
    ) throws IOException {
        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setUsername(username);
        adminDTO.setEmail(email);
        adminDTO.setAddress(address);
        adminDTO.setPhoneNumber(phoneNumber);
        adminDTO.setFullName(fullName);
        adminDTO.setDateOfBirth(dateOfBirth);
        adminDTO.setImage(image);

        adminService.updateAdmin(adminId, adminDTO);
        return new ResponseEntity<>("Admin Updated Successfully!",HttpStatus.OK);
    }
}
