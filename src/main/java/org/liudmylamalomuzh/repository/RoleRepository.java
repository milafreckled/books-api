package org.liudmylamalomuzh.repository;

import org.liudmylamalomuzh.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findFirstByName(String name);
}

