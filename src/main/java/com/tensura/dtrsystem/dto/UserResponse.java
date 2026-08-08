package com.tensura.dtrsystem.dto;

import com.tensura.dtrsystem.entity.UserEntity;
import lombok.Data;

@Data
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String role;
    private String profilePictureUrl;

    public static UserResponse fromEntity(UserEntity entity) {
        UserResponse dto = new UserResponse();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setMiddleName(entity.getMiddleName());
        dto.setEmail(entity.getEmail());
        dto.setRole(entity.getRole());
        dto.setProfilePictureUrl(entity.getProfilePictureUrl());
        return dto;
    }
}
