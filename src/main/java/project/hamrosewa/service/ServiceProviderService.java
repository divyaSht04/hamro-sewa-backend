package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
import java.util.Optional;

@Transactional
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
            String fileName = imageStorageService.saveImage(serviceProviderDTO.getImage());
            serviceProvider.setImage(fileName);
        } else {
            serviceProvider.setImage(null);
        }

        Role userRole = roleRepository.findByName("ROLE_SERVICE_PROVIDER");
        serviceProvider.setRole(userRole);
        userRepository.save(serviceProvider);
    }


    @Transactional
        public void updateServiceProvider(Long id, ServiceProviderDTO serviceProviderDTO) throws IOException {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service provider not found"));

        if (serviceProviderDTO.getUsername() != null && !serviceProviderDTO.getUsername().equals(serviceProvider.getUsername())) {
            boolean usernameExists = userRepository.findByUsername(serviceProviderDTO.getUsername()).isPresent();
            if (usernameExists) {
                throw new UserValidationException("Username already exists");
            }
            serviceProvider.setUsername(serviceProviderDTO.getUsername());
        }

        if (serviceProviderDTO.getEmail() != null && !serviceProviderDTO.getEmail().equals(serviceProvider.getEmail())) {
            boolean emailExists = userRepository.findByEmail(serviceProviderDTO.getEmail()).isPresent();
            if (emailExists) {
                throw new UserValidationException("Email already exists");
            }
            serviceProvider.setEmail(serviceProviderDTO.getEmail());
        }

        if (serviceProviderDTO.getPhoneNumber() != null && !serviceProviderDTO.getPhoneNumber().equals(serviceProvider.getPhoneNumber())) {
            boolean numberExists = userRepository.findByPhoneNumber(serviceProviderDTO.getPhoneNumber()).isPresent();
            if (numberExists) {
                throw new UserValidationException("Phone number already exists");
            }
            serviceProvider.setPhoneNumber(serviceProviderDTO.getPhoneNumber());
        }

        if (serviceProviderDTO.getPassword() != null && !serviceProviderDTO.getPassword().isEmpty()) {
            serviceProvider.setPassword(passwordEncoder.encode(serviceProviderDTO.getPassword()));
        }

        if (serviceProviderDTO.getBusinessName() != null) {
            serviceProvider.setBusinessName(serviceProviderDTO.getBusinessName());
        }

        if (serviceProviderDTO.getAddress() != null) {
            serviceProvider.setAddress(serviceProviderDTO.getAddress());
        }

        if (serviceProviderDTO.getImage() != null && !serviceProviderDTO.getImage().isEmpty()) {
            if (serviceProvider.getImage() != null) {
                imageStorageService.deleteImage(serviceProvider.getImage());
            }
            String fileName = imageStorageService.saveImage(serviceProviderDTO.getImage());
            serviceProvider.setImage(fileName);
        } else if (serviceProvider.getImage() != null) {
            imageStorageService.deleteImage(serviceProvider.getImage());
            serviceProvider.setImage(null);
        }

        userRepository.save(serviceProvider);
    }

    public byte[] getCustomerProfileImage(int userId) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getImage() == null) {
            throw new RuntimeException("User has no profile image");
        }

        return imageStorageService.getImage(user.getImage());
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

    public void updateServiceProviderPhoto(long customerId, MultipartFile photo) throws IOException {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        if (serviceProvider.getImage() != null) {
            imageStorageService.deleteImage(serviceProvider.getImage());
        }
        String fileName = imageStorageService.saveImage(photo);
        serviceProvider.setImage(fileName);
        serviceProviderRepository.save(serviceProvider);
    }

    public List<ServiceProvider> getServiceProviderInfo(long id) {
        Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findById(id);
        if (serviceProvider.isPresent()) {
            return serviceProvider.stream().toList();
        }
        return null;
    }

    public byte[] getServiceProviderImage(long id) throws IOException {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new UserValidationException("Service provider not found with id: " + id));

        if (serviceProvider.getImage() == null) {
            throw new UserValidationException("Service provider has no image");
        }

        return imageStorageService.getImage(serviceProvider.getImage());
    }
}
