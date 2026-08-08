package com.tensura.dtrsystem.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String middleName;
    @Column(unique = true)
    private String email;
    private String password;
    private String role;
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
}