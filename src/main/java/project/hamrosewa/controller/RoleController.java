package project.hamrosewa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import project.hamrosewa.model.Role;
import project.hamrosewa.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping("/addRole")
    public void createRole(@RequestBody Role role) {
        roleService.createRole(role);
    }

    @GetMapping("/getAllRoles")
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }
}
