package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.hamrosewa.dto.CustomerDTO;
import project.hamrosewa.dto.ServiceProviderDTO;
import project.hamrosewa.exceptions.UserValidationException;
import project.hamrosewa.model.Customer;
import project.hamrosewa.model.Role;
import project.hamrosewa.model.ServiceProvider;
import project.hamrosewa.model.User;
import project.hamrosewa.repository.RoleRepository;
import project.hamrosewa.repository.ServiceProviderRepository;
import project.hamrosewa.repository.UserRepository;

import java.io.IOException;
import java.time.LocalDate;

@Service
public class ServiceProviderService {
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ImageService imageStorageService;


    @Transactional
    public void registerServiceProvider(ServiceProviderDTO serviceProviderDTO) throws IOException {
        boolean usernameExists = userRepository.findByUsername(serviceProviderDTO.getUsername()).isPresent();
        if (usernameExists) {
            throw new UserValidationException("Username already exists");
        }

        boolean emailExist = userRepository.findByEmail(serviceProviderDTO.getUsername()).isPresent();
        if (emailExist) {
            throw new UserValidationException("Username already exists");
        }

        boolean numberExists = userRepository.findByPhoneNumber(serviceProviderDTO.getUsername()).isPresent();
        if (numberExists) {
            throw new UserValidationException("Username already exists");
        }

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setUsername(serviceProviderDTO.getUsername());
        serviceProvider.setEmail(serviceProviderDTO.getEmail());
        serviceProvider.setPhoneNumber(serviceProviderDTO.getPhoneNumber());
        serviceProvider.setPassword(passwordEncoder.encode(serviceProviderDTO.getPassword()));
        serviceProvider.setServiceCategory(serviceProviderDTO.getServiceCategory());
        serviceProvider.setBusinessName(serviceProviderDTO.getBusinessName());
        serviceProvider.setAddress(serviceProviderDTO.getAddress());
        serviceProvider.setDescription(serviceProviderDTO.getDescription());
        serviceProvider.setDate(LocalDate.now());
        serviceProvider.setVerified(false);
        serviceProvider.setHourlyRate(serviceProviderDTO.getHourlyRate());

        if (serviceProviderDTO.getImage() != null && !serviceProviderDTO.getImage().isEmpty()) {
            String fileName = imageStorageService.saveProfileImage(serviceProviderDTO.getImage());
            serviceProvider.setImage(fileName);
        }else {
            serviceProvider.setImage(null);
        }

        Role userRole = roleRepository.findByName("ROLE_SERVICE_PROVIDER");
        serviceProvider.setRole(userRole);
        userRepository.save(serviceProvider);
    }



    public byte[] getCustomerProfileImage(int userId) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getImage() == null) {
            throw new RuntimeException("User has no profile image");
        }

        return imageStorageService.getProfileImage(user.getImage());
    }
}
