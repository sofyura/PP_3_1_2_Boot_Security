package ru.kata.spring.boot_security.demo.dto;

import ru.kata.spring.boot_security.demo.model.User;
import java.util.List;
import java.util.stream.Collectors;

public class UserResponse {
    private Long id;
    private String name;
    private String lastName;
    private Integer age;
    private String email;
    private List<RoleResponse> roles;
    public UserResponse() {}
    public UserResponse(User user) {
        this.id = user.getId(); this.name = user.getName(); this.lastName = user.getLastName();
        this.age = user.getAge(); this.email = user.getEmail();
        this.roles = user.getRoles().stream().map(RoleResponse::new).collect(Collectors.toList());
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLastName() { return lastName; }
    public Integer getAge() { return age; }
    public String getEmail() { return email; }
    public List<RoleResponse> getRoles() { return roles; }
}
