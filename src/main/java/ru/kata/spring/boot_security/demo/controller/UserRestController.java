package ru.kata.spring.boot_security.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserRestController {

    private final UserService userService;
    private final RoleService roleService;

    public UserRestController(UserService userService,
                              RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    // GET ALL USERS
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        List<UserResponse> users = userService.getAllUsers()
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    // GET USER BY ID
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        User user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                UserResponse.fromUser(user)
        );
    }

    // GET CURRENT USER
    @GetMapping("/user/current")
    public ResponseEntity<UserResponse> getCurrentUser(
            Principal principal) {

        User user = userService.findByUsername(principal.getName());

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                UserResponse.fromUser(user)
        );
    }

    // GET ALL ROLES
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {

        List<RoleResponse> roles = roleService.getAllRoles()
                .stream()
                .map(RoleResponse::fromRole)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roles);
    }

    // CREATE USER
    @PostMapping("/users")
    public ResponseEntity<UserResponse> addUser(
            @RequestBody User user) {

        userService.saveUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.fromUser(user));
    }

    //UPDATE USER
    @PutMapping("/users")
    public ResponseEntity<UserResponse> updateUser(
            @RequestBody User user) {

        userService.updateUser(user);

        User updatedUser =
                userService.getUserById(user.getId());

        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                UserResponse.fromUser(updatedUser)
        );
    }

    // DELETE USER
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        User user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    // USER RESPONSE
    public static class UserResponse {

        private Long id;
        private String name;
        private String lastName;
        private Integer age;
        private String email;
        private Set<RoleResponse> roles;

        public UserResponse() {
        }

        public static UserResponse fromUser(User user) {

            UserResponse response =
                    new UserResponse();

            response.id = user.getId();
            response.name = user.getName();
            response.lastName = user.getLastName();
            response.age = user.getAge();
            response.email = user.getEmail();

            if (user.getRoles() != null) {
                response.roles = user.getRoles()
                        .stream()
                        .map(RoleResponse::fromRole)
                        .collect(Collectors.toSet());
            }

            return response;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLastName() {
            return lastName;
        }

        public Integer getAge() {
            return age;
        }

        public String getEmail() {
            return email;
        }

        public Set<RoleResponse> getRoles() {
            return roles;
        }
    }

    // ROLE RESPONSE
    public static class RoleResponse {

        private Long id;
        private String name;

        public RoleResponse() {
        }

        public static RoleResponse fromRole(Role role) {

            RoleResponse response =
                    new RoleResponse();

            response.id = role.getId();
            response.name = role.getName();

            return response;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}