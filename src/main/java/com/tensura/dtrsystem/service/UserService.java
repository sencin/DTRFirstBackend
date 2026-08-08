package com.tensura.dtrsystem.service;

import com.tensura.dtrsystem.dto.UserRequest;
import com.tensura.dtrsystem.dto.UserResponse;
import com.tensura.dtrsystem.entity.UserEntity;
import com.tensura.dtrsystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(UserRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email address is already in use: " + dto.getEmail());
        }
        UserEntity user = new UserEntity();
        mapDtoToEntity(dto, user);
        user.setPassword(dto.getPassword());

        return UserResponse.fromEntity(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        enforceAdminOrSelf(id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateUser(Long id, UserRequest dto) {
        enforceAdminOrSelf(id);
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email address is already in use: " + dto.getEmail());
        }

        mapDtoToEntity(dto, user);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(dto.getPassword());
        }

        return UserResponse.fromEntity(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        enforceAdminOrSelf(id);
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }


    private UserEntity getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void enforceAdminOrSelf(Long requestedId) {
        UserEntity loggedUser = getCurrentAuthenticatedUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(loggedUser.getRole());
        boolean isSelf = loggedUser.getId().equals(requestedId);

        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException("Access Denied: You do not have permission to access this user's information.");
        }
    }

    private void mapDtoToEntity(UserRequest dto, UserEntity entity) {
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setMiddleName(dto.getMiddleName());
        entity.setEmail(dto.getEmail());
        entity.setRole(dto.getRole());
        entity.setProfilePictureUrl(dto.getProfilePictureUrl());
    }
}
