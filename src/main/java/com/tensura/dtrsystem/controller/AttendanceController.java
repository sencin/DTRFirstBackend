package com.tensura.dtrsystem.controller;

import com.tensura.dtrsystem.dto.AttendanceRequest;
import com.tensura.dtrsystem.dto.MonthlyAttendanceResponse;
import com.tensura.dtrsystem.entity.AttendanceEntity;
import com.tensura.dtrsystem.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<AttendanceEntity> createAttendance(@RequestParam BigDecimal latitude, @RequestParam BigDecimal longitude, @RequestParam MultipartFile picture) throws IOException {
        return new ResponseEntity<>(attendanceService.createAttendance(latitude,longitude,picture), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AttendanceEntity>> getAllAttendances() {
        return ResponseEntity.ok(attendanceService.getAllAttendances());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceEntity> getAttendanceById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendanceById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttendanceEntity>> getAttendancesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(attendanceService.getAttendancesByUserId(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AttendanceEntity> updateAttendance(
            @PathVariable Long id,
            @RequestBody AttendanceRequest dto) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, dto));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/punch-type")
    public ResponseEntity<Map<String, String>> getPunchType() {
        return ResponseEntity.ok(attendanceService.determinePunchType());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}/monthly")
    public ResponseEntity<MonthlyAttendanceResponse> getMonthlyAttendance(@PathVariable Long userId, @RequestParam int year, @RequestParam int month) {
        MonthlyAttendanceResponse summary = attendanceService.getMonthlyAttendanceForUser(userId, year, month);
        return ResponseEntity.ok(summary);
    }
}
