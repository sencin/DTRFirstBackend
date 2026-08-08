package com.tensura.dtrsystem.service;
import com.tensura.dtrsystem.dto.FaceRegistrationRequest;
import com.tensura.dtrsystem.entity.UserEntity;
import com.tensura.dtrsystem.entity.UserFaceRecordEntity;
import com.tensura.dtrsystem.repository.FaceRecordRepository;
import com.tensura.dtrsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class FaceService {

    private final UserRepository userRepository;
    private final FaceRecordRepository faceRecordRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public void registerFace(FaceRegistrationRequest request, MultipartFile image) {
        try {
            // 1. Retrieve the user
            UserEntity user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

            String imageUrl = fileStorageService.uploadFile(image, "biometrics");

            String embeddingString = request.getEmbedding().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            // 3. Fetch or Create the Face Record using the Repository
            // Since UserEntity has no reference, we query by User ID
            UserFaceRecordEntity faceRecord = faceRecordRepository.findByUserId(user.getId())
                    .orElse(UserFaceRecordEntity.builder().user(user).build());

            // 4. Update fields
            faceRecord.setEmbedding(embeddingString);
            faceRecord.setBiometricImagePath(imageUrl);
            user.setProfilePictureUrl(imageUrl);

// Uncomment to support image saving
            // 5. Save both
            faceRecordRepository.save(faceRecord);
            userRepository.save(user);

            log.info("Successfully registered face for user: {}", user.getId());

        } catch (Exception e) {
            log.error("Face registration failed for user {}", request.getUserId(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    public String getStoredEmbedding() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserFaceRecordEntity faceRecord = faceRecordRepository.findByUserId(user.getId())
                .orElse(null);

        if (faceRecord == null) {
            return null;
        }

        return faceRecord.getEmbedding();
    }

    public boolean hasFaceRegistered(Long userId) {
        return faceRecordRepository.existsByUserId(userId);
    }

}