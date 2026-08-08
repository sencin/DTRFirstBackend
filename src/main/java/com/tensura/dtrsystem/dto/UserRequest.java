package com.tensura.dtrsystem.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String password;
    private String role;
    private String profilePictureUrl;
}