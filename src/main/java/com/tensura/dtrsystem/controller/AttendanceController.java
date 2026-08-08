package com.tensura.dtrsystem.controller;

import com.tensura.dtrsystem.dto.AttendanceRequest;
import com.tensura.dtrsystem.entity.AttendanceEntity;
import com.tensura.dtrsystem.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<AttendanceEntity> createAttendance(@RequestBody AttendanceRequest dto) {
        return new ResponseEntity<>(attendanceService.createAttendance(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AttendanceEntity>> getAllAttendances() {
        return ResponseEntity.ok(attendanceService.getAllAttendances());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceEntity> getAttendanceById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendanceById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttendanceEntity>> getAttendancesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(attendanceService.getAttendancesByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceEntity> updateAttendance(
            @PathVariable Long id,
            @RequestBody AttendanceRequest dto) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }
}
