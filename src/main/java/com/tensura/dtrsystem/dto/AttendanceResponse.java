package com.tensura.dtrsystem.dto;

import com.tensura.dtrsystem.entity.AttendanceEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class AttendanceResponse {
    private Long id;
    private Date date;
    private String timeIn;
    private String timeOut;
    private String timeInImage;
    private String timeOutImage;
    private Double totalHours;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private UserResponse user;

    public static AttendanceResponse fromEntity(AttendanceEntity entity) {
        if (entity == null) return null;

        AttendanceResponse dto = new AttendanceResponse();
        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setTimeIn(entity.getTimeIn());
        dto.setTimeOut(entity.getTimeOut());
        dto.setTimeInImage(entity.getTimeInImage());
        dto.setTimeOutImage(entity.getTimeOutImage());
        dto.setTotalHours(entity.getTotalHours());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());

        if (entity.getUser() != null) {
            dto.setUser(UserResponse.fromEntity(entity.getUser()));
        }
        return dto;
    }
}