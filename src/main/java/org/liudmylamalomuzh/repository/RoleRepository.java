package org.liudmylamalomuzh.repository;

import org.liudmylamalomuzh.entity.Book;
import org.liudmylamalomuzh.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String roleAdmin);
}

