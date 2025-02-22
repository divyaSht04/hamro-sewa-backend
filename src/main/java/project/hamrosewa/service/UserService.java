package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import project.hamrosewa.dto.UserDTO;
import project.hamrosewa.exceptions.UserValidationException;
import project.hamrosewa.model.Role;
import project.hamrosewa.model.User;
import project.hamrosewa.repository.RoleRepository;
import project.hamrosewa.repository.UserRepository;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ImageService imageStorageService;

    @Transactional
    public void registerUser(UserDTO userDTO) throws IOException {

        boolean usernameExists = userRepository.findByUsername(userDTO.getUsername()).isPresent();
        if (usernameExists) {
            throw new UserValidationException("Username already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setDateOfBirth(userDTO.getDateOfBirth());
        if (userDTO.getImage() != null && !userDTO.getImage().isEmpty()) {
            String fileName = imageStorageService.saveProfileImage(userDTO.getImage());
            user.setImage(fileName);
        }else {
            user.setImage(null);
        }
        Role userRole = roleRepository.findByName("ROLE_USER");

        user.getRoles().add(userRole);
        userRepository.save(user);
    }


    public User registerServiceProvider(UserDTO registrationDTO) {
        // Check if username or email already exists
//        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
//            throw new RuntimeException("Username already exists");
//        }
//        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
//            throw new RuntimeException("Email already exists");
//        }

        User serviceProvider = new User();
        serviceProvider.setUsername(registrationDTO.getUsername());
        serviceProvider.setEmail(registrationDTO.getEmail());
        serviceProvider.setPassword(registrationDTO.getPassword());
        serviceProvider.setDateOfBirth(registrationDTO.getDateOfBirth());
        serviceProvider.setPhoneNumber(registrationDTO.getPhoneNumber());
//        serviceProvider.setAddress(registrationDTO.getAddress());

        // Get or create SERVICE_PROVIDER role
        Role providerRole = roleRepository.findByName("ROLE_SERVICE_PROVIDER");
        serviceProvider.getRoles().add(providerRole);
        return userRepository.save(serviceProvider);
    }

    public byte[] getUserProfileImage(int userId) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getImage() == null) {
            throw new RuntimeException("User has no profile image");
        }

        return imageStorageService.getProfileImage(user.getImage());
    }
}
