package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.CustomerDTO;
import project.hamrosewa.exceptions.UserValidationException;
import project.hamrosewa.model.Customer;
import project.hamrosewa.model.Role;
import project.hamrosewa.model.User;
import project.hamrosewa.repository.CustomerRepository;
import project.hamrosewa.repository.RoleRepository;
import project.hamrosewa.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Transactional
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
    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public void registerCustomer(CustomerDTO customerDTO) throws IOException {
        boolean usernameExists = userRepository.findByUsername(customerDTO.getUsername()).isPresent();
        if (usernameExists) {
            throw new UserValidationException("Username already exists");
        }

        boolean emailExist = userRepository.findByEmail(customerDTO.getEmail()).isPresent();
        if (emailExist) {
            throw new UserValidationException("Email already exists");
        }

        boolean numberExists = userRepository.findByPhoneNumber(customerDTO.getPhoneNumber()).isPresent();
        if (numberExists) {
            throw new UserValidationException("Phone Number already exists");
        }

        Customer customer = new Customer();
        customer.setUsername(customerDTO.getUsername());
        customer.setEmail(customerDTO.getEmail());
        customer.setPhoneNumber(customerDTO.getPhoneNumber());
        customer.setPassword(passwordEncoder.encode(customerDTO.getPassword()));
        if (customerDTO.getImage() != null && !customerDTO.getImage().isEmpty()) {
            String fileName = imageStorageService.saveImage(customerDTO.getImage());
            customer.setImage(fileName);
        } else {
            customer.setImage(null);
        }
        customer.setDateOfBirth(customerDTO.getDateOfBirth());
        customer.setAddress(customerDTO.getAddress());
        Role userRole = roleRepository.findByName("ROLE_CUSTOMER");
        customer.setRole(userRole);
        customer.setFullName(customerDTO.getFullName());
        userRepository.save(customer);
    }

    public List<Customer> getCustomerInfo(int customerId) {
        Optional<Customer> customer = customerRepository.findAllById(customerId);
        if (customer.isEmpty()) {
            throw new RuntimeException("Customer not found");
        }
        return List.of(customer.get());
    }


    public byte[] getCustomerProfileImage(int userId) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getImage() == null) {
            throw new RuntimeException("User has no profile image");
        }

        return imageStorageService.getImage(user.getImage());
    }

    @Transactional
    public void updateCustomer(long customerId, CustomerDTO customerDTO) throws IOException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (customerDTO.getUsername() != null && !customerDTO.getUsername().isEmpty() &&
                !customerDTO.getUsername().equals(customer.getUsername())) {
            Optional<User> userByUsername = userRepository.findByUsername(customerDTO.getUsername());
            if (userByUsername.isPresent() && userByUsername.get().getId() != customer.getId()) {
                throw new UserValidationException("Username already exists");
            }
            customer.setUsername(customerDTO.getUsername());
        }

        if (customerDTO.getEmail() != null && !customerDTO.getEmail().isEmpty() &&
                !customerDTO.getEmail().equals(customer.getEmail())) {
            Optional<User> userByEmail = userRepository.findByEmail(customerDTO.getEmail());
            if (userByEmail.isPresent() && userByEmail.get().getId() != customer.getId()) {
                throw new UserValidationException("Email already exists");
            }
            customer.setEmail(customerDTO.getEmail());
        }

        if (customerDTO.getPhoneNumber() != null && !customerDTO.getPhoneNumber().isEmpty() &&
                !customerDTO.getPhoneNumber().equals(customer.getPhoneNumber())) {
            Optional<User> userByPhone = userRepository.findByPhoneNumber(customerDTO.getPhoneNumber());
            if (userByPhone.isPresent() && userByPhone.get().getId() != customer.getId()) {
                throw new UserValidationException("Phone number already exists");
            }
            customer.setPhoneNumber(customerDTO.getPhoneNumber());
        }

        if (customerDTO.getPassword() != null && !customerDTO.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customerDTO.getPassword()));
        }

        if (customerDTO.getAddress() != null && !customerDTO.getAddress().isEmpty()) {
            customer.setAddress(customerDTO.getAddress());
        }
        if (customerDTO.getFullName() != null && !customerDTO.getFullName().isEmpty()) {
            customer.setFullName(customerDTO.getFullName());
        }
        if (customerDTO.getImage() != null && !customerDTO.getImage().isEmpty()) {
            if (customer.getImage() != null) {
                imageStorageService.deleteImage(customer.getImage());
            }
            String fileName = imageStorageService.saveImage(customerDTO.getImage());
            customer.setImage(fileName);
        } else if (customer.getImage() != null) {
            imageStorageService.deleteImage(customer.getImage());
            customer.setImage(null);
        }
        if (customerDTO.getDateOfBirth() != null) {
            customer.setDateOfBirth(customerDTO.getDateOfBirth());
        }

        customerRepository.save(customer);
    }

    @Transactional
    public void updateCustomerPhoto(long customerId, MultipartFile photo) throws IOException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        if (customer.getImage() != null) {
            imageStorageService.deleteImage(customer.getImage());
        }
        String fileName = imageStorageService.saveImage(photo);
        customer.setImage(fileName);
        customerRepository.save(customer);
    }
}
