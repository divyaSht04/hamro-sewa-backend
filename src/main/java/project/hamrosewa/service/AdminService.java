package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.hamrosewa.dto.AdminDTO;
import project.hamrosewa.exceptions.UserValidationException;
import project.hamrosewa.model.Admin;
import project.hamrosewa.model.Role;
import project.hamrosewa.model.User;
import project.hamrosewa.repository.RoleRepository;
import project.hamrosewa.repository.UserRepository;

import java.io.IOException;

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

    @Transactional
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
            String fileName = imageStorageService.saveProfileImage(adminDTO.getImage());
            admin.setImage(fileName);
        }else {
            admin.setImage(null);
        }
        admin.setAddress(adminDTO.getAddress());
        Role userRole = roleRepository.findByName("ROLE_ADMIN");
        admin.setRole(userRole);
        admin.setFullName(adminDTO.getFullName());
        admin.setDepartment(adminDTO.getDepartment());
        userRepository.save(admin);
    }



    public byte[] getAdminProfileImage(int userId) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getImage() == null) {
            throw new RuntimeException("User has no profile image");
        }
        return imageStorageService.getProfileImage(user.getImage());
    }
}
