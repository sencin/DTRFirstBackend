package com.tensura.dtrsystem.dto;

import com.tensura.dtrsystem.entity.AttendanceEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAttendanceResponse {
    private List<AttendanceResponse> records;
    private double totalHoursRenderedInMonth;
}
