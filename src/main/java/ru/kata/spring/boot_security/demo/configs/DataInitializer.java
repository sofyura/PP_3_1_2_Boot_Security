package ru.kata.spring.boot_security.demo.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public DataInitializer(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public void run(String... args) {
        Role adminRole = roleService.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            adminRole = new Role("ROLE_ADMIN");
            roleService.saveRole(adminRole);
        }

        Role userRole = roleService.findByName("ROLE_USER");
        if (userRole == null) {
            userRole = new Role("ROLE_USER");
            roleService.saveRole(userRole);
        }

        if (userService.findByUsername("admin@mail.com") == null) {
            User admin = new User("Admin", "Adminov", "admin@mail.com", "admin", Set.of(adminRole, userRole));
            userService.saveUser(admin);
        }

        if (userService.findByUsername("user@mail.com") == null) {
            User user = new User("User", "Userov", "user@mail.com", "user", Set.of(userRole));
            userService.saveUser(user);
        }
    }
}
