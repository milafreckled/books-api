package org.liudmylamalomuzh.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

@Table(name = "privileges")
@Entity
@Getter
@Setter
public class Privilege {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Use IDENTITY for PostgreSQL
    private Long id;

    @Setter
    @Getter
    private String name;

    @ManyToMany(mappedBy = "privileges")
    private Collection<Role> roles = new ArrayList<>();

    public Privilege(String name) {
        this.name = name;
    }

    public Privilege() {
    }

}
