package com.tensura.dtrsystem.controller;

import com.tensura.dtrsystem.dto.LoginRequest;
import com.tensura.dtrsystem.dto.RegisterRequest;
import com.tensura.dtrsystem.entity.UserEntity;
import com.tensura.dtrsystem.repository.UserRepository;
import com.tensura.dtrsystem.service.AuthService;
import com.tensura.dtrsystem.service.FaceService;
import com.tensura.dtrsystem.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final FaceService faceService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        UserEntity user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        // 2. Handle invalid password mismatch
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        // 3. Handle account status locks/pending
        if (user.getStatus().equalsIgnoreCase("Pending")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Account is still pending approval."));
        }

        String token = jwtService.generateToken(user);

        boolean hasFaceRegistered = faceService.hasFaceRegistered(user.getId());

        return ResponseEntity.ok(
                Map.of(
                        "id", user.getId(),
                        "token", token,
                        "email", user.getEmail(),
                        "role", user.getRole(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "hasFaceRegistered", hasFaceRegistered,
                        "status", user.getStatus()
                )
        );
    }
}