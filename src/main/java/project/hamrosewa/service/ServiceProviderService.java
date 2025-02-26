package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.hamrosewa.dto.CustomerDTO;
import project.hamrosewa.dto.ServiceProviderDTO;
import project.hamrosewa.exceptions.UserValidationException;
import project.hamrosewa.model.*;
import project.hamrosewa.repository.RoleRepository;
import project.hamrosewa.repository.ServiceProviderRepository;
import project.hamrosewa.repository.UserRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

        boolean emailExist = userRepository.findByEmail(serviceProviderDTO.getEmail()).isPresent();
        if (emailExist) {
            throw new UserValidationException("Email already exists");
        }

        boolean numberExists = userRepository.findByPhoneNumber(serviceProviderDTO.getPhoneNumber()).isPresent();
        if (numberExists) {
            throw new UserValidationException("Number already exists");
        }

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setUsername(serviceProviderDTO.getUsername());
        serviceProvider.setEmail(serviceProviderDTO.getEmail());
        serviceProvider.setPhoneNumber(serviceProviderDTO.getPhoneNumber());
        serviceProvider.setPassword(passwordEncoder.encode(serviceProviderDTO.getPassword()));
        serviceProvider.setBusinessName(serviceProviderDTO.getBusinessName());
        serviceProvider.setAddress(serviceProviderDTO.getAddress());

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

    public ServiceProvider getServiceProviderById(Long id) {
        return serviceProviderRepository.findById(id).orElse(null);
    }

    public List<ProviderService> getProviderServices(Long providerId) {
        ServiceProvider serviceProvider = getServiceProviderById(providerId);
        if (serviceProvider == null) {
            throw new RuntimeException("Service provider not found");
        }
        return serviceProvider.getServices();
    }
}
