package project.hamrosewa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.UserDTO;
import project.hamrosewa.service.UserService;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("dateOfBirth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        userDTO.setEmail(email);
        userDTO.setPassword(password);
        userDTO.setPhoneNumber(phoneNumber);
        userDTO.setDateOfBirth(dateOfBirth);
        userDTO.setImage(image);

        userService.registerUser(userDTO);
        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/profile-image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable int userId) {
        try {
            byte[] imageData = userService.getUserProfileImage(userId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageData);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
