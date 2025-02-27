package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.AdminDTO;
import project.hamrosewa.exceptions.UserValidationException;
import project.hamrosewa.model.Admin;
import project.hamrosewa.model.Role;
import project.hamrosewa.model.User;
import project.hamrosewa.repository.RoleRepository;
import project.hamrosewa.repository.UserRepository;

import java.io.IOException;

@Transactional
@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ImageService imageStorageService;

    public void registerAdmin(AdminDTO adminDTO) throws IOException {
        boolean usernameExists = userRepository.findByUsername(adminDTO.getUsername()).isPresent();
        if (usernameExists) {
            throw new UserValidationException("Username already exists");
        }

        boolean emailExist = userRepository.findByEmail(adminDTO.getEmail()).isPresent();
        if (emailExist) {
            throw new UserValidationException("Email already exists");
        }

        boolean numberExists = userRepository.findByPhoneNumber(adminDTO.getPhoneNumber()).isPresent();
        if (numberExists) {
            throw new UserValidationException("Number already exists");
        }

        Admin admin = new Admin();
        admin.setUsername(adminDTO.getUsername());
        admin.setEmail(adminDTO.getEmail());
        admin.setPhoneNumber(adminDTO.getPhoneNumber());
        admin.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
        if (adminDTO.getImage() != null && !adminDTO.getImage().isEmpty()) {
            String fileName = imageStorageService.saveImage(adminDTO.getImage());
            admin.setImage(fileName);
        } else {
            admin.setImage(null);
        }
        admin.setAddress(adminDTO.getAddress());
        Role userRole = roleRepository.findByName("ROLE_ADMIN");
        admin.setDateOfBirth(adminDTO.getDateOfBirth());
        admin.setRole(userRole);
        admin.setFullName(adminDTO.getFullName());
        admin.setDepartment(adminDTO.getDepartment());
        userRepository.save(admin);
    }

    @Transactional
    public void updateAdmin(int adminId, AdminDTO adminDTO) throws IOException {
        Admin admin = userRepository.findById(adminId)
                .filter(user -> user instanceof Admin)
                .map(user -> (Admin) user)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (adminDTO.getUsername() != null && !adminDTO.getUsername().equals(admin.getUsername())) {
            boolean usernameExists = userRepository.findByUsername(adminDTO.getUsername())
                    .filter(user -> !(user.getId() == adminId))
                    .isPresent();
            if (usernameExists) {
                throw new UserValidationException("Username already exists");
            }
            admin.setUsername(adminDTO.getUsername());
        }

        if (adminDTO.getEmail() != null && !adminDTO.getEmail().equals(admin.getEmail())) {
            boolean emailExists = userRepository.findByEmail(adminDTO.getEmail())
                    .filter(user -> !(user.getId() == (adminId)))
                    .isPresent();
            if (emailExists) {
                throw new UserValidationException("Email already exists");
            }
            admin.setEmail(adminDTO.getEmail());
        }

        if (adminDTO.getPhoneNumber() != null && !adminDTO.getPhoneNumber().equals(admin.getPhoneNumber())) {
            boolean phoneExists = userRepository.findByPhoneNumber(adminDTO.getPhoneNumber())
                    .filter(user -> !(user.getId() == (adminId)))
                    .isPresent();
            if (phoneExists) {
                throw new UserValidationException("Phone number already exists");
            }
            admin.setPhoneNumber(adminDTO.getPhoneNumber());
        }

        if (adminDTO.getPassword() != null && !adminDTO.getPassword().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
        }

        if (adminDTO.getFullName() != null) {
            admin.setFullName(adminDTO.getFullName());
        }

        if (adminDTO.getAddress() != null) {
            admin.setAddress(adminDTO.getAddress());
        }

        if (adminDTO.getDepartment() != null) {
            admin.setDepartment(adminDTO.getDepartment());
        }

        if (adminDTO.getDateOfBirth() != null) {
            admin.setDateOfBirth(adminDTO.getDateOfBirth());
        }

        if (adminDTO.getImage() != null && !adminDTO.getImage().isEmpty()) {
            if (admin.getImage() != null) {
                imageStorageService.deleteImage(admin.getImage());
            }
            String fileName = imageStorageService.saveImage(adminDTO.getImage());
            admin.setImage(fileName);
        } else if (admin.getImage() != null) {
            imageStorageService.deleteImage(admin.getImage());
            admin.setImage(null);
        }

        userRepository.save(admin);
    }

    @Transactional
    public void updateAdminPhoto(int adminId, MultipartFile photo) throws IOException {
        Admin admin = userRepository.findById(adminId)
                .filter(user -> user instanceof Admin)
                .map(user -> (Admin) user)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getImage() != null) {
            imageStorageService.deleteImage(admin.getImage());
        }
        String fileName = imageStorageService.saveImage(photo);
        admin.setImage(fileName);
        userRepository.save(admin);
    }

    public byte[] getAdminProfileImage(int userId) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getImage() == null) {
            throw new RuntimeException("User has no profile image");
        }
        return imageStorageService.getImage(user.getImage());
    }
}
