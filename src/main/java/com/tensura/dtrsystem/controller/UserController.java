package com.tensura.dtrsystem.controller;

import com.tensura.dtrsystem.dto.FaceRegistrationRequest;
import com.tensura.dtrsystem.dto.UserRequest;
import com.tensura.dtrsystem.dto.UserResponse;
import com.tensura.dtrsystem.repository.UserRepository;
import com.tensura.dtrsystem.service.FaceService;
import com.tensura.dtrsystem.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    final private FaceService faceService;
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest dto) {
        return new ResponseEntity<>(userService.createUser(dto), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequest dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @PostMapping(value = "/register-face", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerFace(@RequestPart("request") FaceRegistrationRequest request, @RequestPart("image") MultipartFile image) {
        faceService.registerFace(request, image);
        return ResponseEntity.ok("Face registered successfully");
    }

    @GetMapping("/stored")
    public ResponseEntity<?> getStoredEmbedding() {
        String embedding = faceService.getStoredEmbedding();

        if(embedding == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No face biometric data registered for this account.");
        }

        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("embedding", embedding);

        return ResponseEntity.ok(successResponse);
    }
}
