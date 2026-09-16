package ru.kata.spring.boot_security.demo.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.annotation.PostConstruct;
import java.util.Set;

@Component
public class DataInitializer {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public DataInitializer(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @PostConstruct
    public void init() {
        Role adminRole = new Role("ROLE_ADMIN");
        Role userRole = new Role("ROLE_USER");

        roleService.saveRole(adminRole);
        roleService.saveRole(userRole);

        User admin = new User();
        admin.setName("Admin");
        admin.setLastName("Admin");
        admin.setAge(35);
        admin.setEmail("admin");
        admin.setPassword("admin");
        admin.setRoles(Set.of(adminRole, userRole));
        userService.saveUser(admin);

        // Пользователь с логином "user" и паролем "user"
        User user = new User();
        user.setName("User");
        user.setLastName("User");
        user.setAge(25);
        user.setEmail("user");
        user.setPassword("user");
        user.setRoles(Set.of(userRole));
        userService.saveUser(user);
    }
}