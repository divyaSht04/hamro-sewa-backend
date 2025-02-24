package project.hamrosewa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.hamrosewa.dto.CustomerDTO;
import project.hamrosewa.model.Customer;
import project.hamrosewa.service.CustomerService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/info/{id}")
    public ResponseEntity<?> getCustomerInfo(@PathVariable int id){
        List<Customer> customerInfo = customerService.getCustomerInfo(id);
        return new ResponseEntity<>(customerInfo, HttpStatus.OK);
    }

    @PutMapping("/edit-customer/{customerId}")
    public ResponseEntity<?> editCustomer(@PathVariable long customerId, @RequestBody CustomerDTO customer) throws IOException {
        customerService.updateCustomer(customerId ,customer);
        return new ResponseEntity<>("Edited Successfully!",HttpStatus.OK);
    }
}
