package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.hamrosewa.exceptions.RoleNotFoundException;
import project.hamrosewa.model.Role;
import project.hamrosewa.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public void createRole(Role role) {
       Role check = roleRepository.findByName(role.getName());
       if (check == null){
           roleRepository.save(role);
       } else throw new RoleNotFoundException("Role " + role.getName() + " already exists");
    }

    public Role findRole(String roleName) {
         Role role = roleRepository.findByName(roleName);
         if (role == null) throw new RoleNotFoundException("Role " + roleName + " not found");
         return role;
    }

    public void updateRole(Role role) {
        if (roleRepository.existsById(role.getId())) {
            roleRepository.save(role);
        } else throw new RoleNotFoundException("Role with id " + role.getId() + " not found");
    }

    public List<Role> getAllRoles(){
        return roleRepository.findAll();
    }
}
