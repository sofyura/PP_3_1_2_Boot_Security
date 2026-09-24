package ru.kata.spring.boot_security.demo.controller.api;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.kata.spring.boot_security.demo.dto.RoleResponse;
import ru.kata.spring.boot_security.demo.dto.UserRequest;
import ru.kata.spring.boot_security.demo.dto.UserResponse;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

@RestController
@RequestMapping("/api")
public class UserRestController {

    private final UserService userService;
    private final RoleService roleService;

    public UserRestController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> users = userService.getAllUsers()
                .stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new UserResponse(user));
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        User user = new User();
        fillUser(user, request);
        userService.saveUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UserResponse(userService.getUserById(user.getId())));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserRequest request) {

        User user = new User();
        user.setId(id);
        fillUser(user, request);
        userService.updateUser(user);

        User updatedUser = userService.getUserById(id);

        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof User
                && id.equals(((User) authentication.getPrincipal()).getId())) {

            UsernamePasswordAuthenticationToken refreshedAuthentication =
                    new UsernamePasswordAuthenticationToken(
                            updatedUser,
                            authentication.getCredentials(),
                            updatedUser.getAuthorities());

            refreshedAuthentication.setDetails(authentication.getDetails());
            SecurityContextHolder.getContext().setAuthentication(refreshedAuthentication);
        }

        return ResponseEntity.ok(new UserResponse(updatedUser));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        List<RoleResponse> roles = roleService.getAllRoles()
                .stream()
                .map(RoleResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roles);
    }

    @GetMapping("/user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        User user = null;

        if (principal instanceof User) {
            user = (User) principal;
        } else if (principal instanceof UserDetails) {
            user = userService.findByUsername(((UserDetails) principal).getUsername());
        }

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new UserResponse(user));
    }

    private void fillUser(User user, UserRequest request) {
        user.setName(request.getName());
        user.setLastName(request.getLastName());
        user.setAge(request.getAge());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRoles(resolveRoles(request.getRoleIds()));
    }

    private Set<Role> resolveRoles(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptySet();
        }

        return roleIds.stream()
                .map(roleService::getRoleById)
                .collect(Collectors.toSet());
    }
}
