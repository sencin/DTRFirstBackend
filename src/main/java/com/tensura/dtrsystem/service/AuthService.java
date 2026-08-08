package com.tensura.dtrsystem.service;

import com.tensura.dtrsystem.dto.RegisterRequest;
import com.tensura.dtrsystem.dto.User;
import com.tensura.dtrsystem.entity.UserEntity;
import com.tensura.dtrsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        UserEntity user = new UserEntity();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        System.out.println(user.getPassword());
        user.setRole("Employee");
        user.setStatus("Pending");
        UserEntity saved = userRepository.save(user);

        return User.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .status(saved.getStatus())
                .profilePictureUrl(saved.getProfilePictureUrl())
                .build();
    }
}