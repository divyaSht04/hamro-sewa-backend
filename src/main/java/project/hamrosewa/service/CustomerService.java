package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.hamrosewa.dto.CustomerDTO;
import project.hamrosewa.exceptions.UserValidationException;
import project.hamrosewa.model.Customer;
import project.hamrosewa.model.Role;
import project.hamrosewa.model.User;
import project.hamrosewa.repository.RoleRepository;
import project.hamrosewa.repository.UserRepository;

import java.io.IOException;

@Service
public class CustomerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ImageService imageStorageService;

    @Transactional
    public void registerCustomer(CustomerDTO customerDTO) throws IOException {
        boolean usernameExists = userRepository.findByUsername(customerDTO.getUsername()).isPresent();
        if (usernameExists) {
            throw new UserValidationException("Username already exists");
        }

        boolean emailExist = userRepository.findByEmail(customerDTO.getUsername()).isPresent();
        if (emailExist) {
            throw new UserValidationException("Username already exists");
        }

        boolean numberExists = userRepository.findByPhoneNumber(customerDTO.getUsername()).isPresent();
        if (numberExists) {
            throw new UserValidationException("Username already exists");
        }

        Customer customer = new Customer();
        customer.setUsername(customerDTO.getUsername());
        customer.setEmail(customerDTO.getEmail());
        customer.setPhoneNumber(customerDTO.getPhoneNumber());
        customer.setPassword(passwordEncoder.encode(customerDTO.getPassword()));
        if (customerDTO.getImage() != null && !customerDTO.getImage().isEmpty()) {
            String fileName = imageStorageService.saveProfileImage(customerDTO.getImage());
            customer.setImage(fileName);
        }else {
            customer.setImage(null);
        }
        customer.setAddress(customerDTO.getAddress());
        Role userRole = roleRepository.findByName("ROLE_USER");
        customer.setRole(userRole);
        customer.setFullName(customerDTO.getFullName());
        userRepository.save(customer);
    }



    public byte[] getCustomerProfileImage(int userId) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getImage() == null) {
            throw new RuntimeException("User has no profile image");
        }

        return imageStorageService.getProfileImage(user.getImage());
    }
}
