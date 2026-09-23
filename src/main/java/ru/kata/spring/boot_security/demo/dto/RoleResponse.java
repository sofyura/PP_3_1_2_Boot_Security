package ru.kata.spring.boot_security.demo.dto;

import ru.kata.spring.boot_security.demo.model.Role;

public class RoleResponse {
    private Long id;
    private String name;
    public RoleResponse() {}
    public RoleResponse(Role role) { this.id = role.getId(); this.name = role.toString(); }
    public Long getId() { return id; }
    public String getName() { return name; }
}
