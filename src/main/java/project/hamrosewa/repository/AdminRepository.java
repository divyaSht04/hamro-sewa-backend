package project.hamrosewa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.hamrosewa.model.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
}
