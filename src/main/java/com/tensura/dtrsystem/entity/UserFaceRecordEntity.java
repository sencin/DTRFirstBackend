package com.tensura.dtrsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_faces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFaceRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserEntity user;

    @Column(name = "biometric_image_path", nullable = false)
    private String biometricImagePath;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String embedding;
}
