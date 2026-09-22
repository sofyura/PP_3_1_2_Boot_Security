package ru.kata.spring.boot_security.demo.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.dto.RoleResponse;
import ru.kata.spring.boot_security.demo.dto.UserRequest;
import ru.kata.spring.boot_security.demo.dto.UserResponse;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<UserResponse> getUsers() {
        return userService.getAllUsers().stream().map(UserResponse::new).collect(Collectors.toList());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(new UserResponse(userService.getUserById(id)));
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        User user = new User();
        fillUser(user, request);
        userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponse(userService.getUserById(user.getId())));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        User user = new User();
        user.setId(id);
        fillUser(user, request);
        userService.updateUser(user);

        User updatedUser = userService.getUserById(id);

        // If the currently authenticated user was edited, refresh the
        // SecurityContext so a changed email/username is used immediately.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) { userService.deleteUser(id); }

    @GetMapping("/roles")
    public List<RoleResponse> getRoles() {
        return roleService.getAllRoles().stream().map(RoleResponse::new).collect(Collectors.toList());
    }

    @GetMapping("/user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        User user = null;

        // The authenticated principal is our User entity, so use its ID.
        // This keeps /api/user working even after an admin changes the user's email.
        if (principal instanceof User) {
            // The User entity is the authenticated principal. Use it directly
            // as a safe fallback; updateUser refreshes it when the email changes.
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
        if (roleIds == null || roleIds.isEmpty()) return Collections.emptySet();
        return roleIds.stream().map(roleService::getRoleById).collect(Collectors.toSet());
    }
}
