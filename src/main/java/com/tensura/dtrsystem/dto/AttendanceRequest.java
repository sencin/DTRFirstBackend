package com.tensura.dtrsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class AttendanceRequest {
    private Date date;
    private String timeIn;
    private String timeOut;
    private int totalHours;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Long userId;
}
