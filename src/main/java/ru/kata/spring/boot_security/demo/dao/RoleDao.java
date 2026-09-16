package ru.kata.spring.boot_security.demo.dao;

import ru.kata.spring.boot_security.demo.model.Role;
import java.util.List;
import java.util.Set;

public interface RoleDao {
    List<Role> getAllRoles();
    Role findByName(String name);
    Role getRoleById(Long id);
    Set<Role> getRolesByIds(Set<Long> ids);
    void saveRole(Role role);
}