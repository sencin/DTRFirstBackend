package com.tensura.dtrsystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceRegistrationRequest {
    private Long userId;
    private List<Double> embedding;
}
