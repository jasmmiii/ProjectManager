package com.scsb.pm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pm_db")
@Data
@Getter
@Setter
public class Member {
    @Id
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

}
