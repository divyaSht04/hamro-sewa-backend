package project.hamrosewa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.hamrosewa.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>{

}
