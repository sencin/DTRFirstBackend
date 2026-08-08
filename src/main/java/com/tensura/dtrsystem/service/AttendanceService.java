package com.tensura.dtrsystem.service;

import com.tensura.dtrsystem.dto.AttendanceRequest;
import com.tensura.dtrsystem.entity.AttendanceEntity;
import com.tensura.dtrsystem.entity.UserEntity;
import com.tensura.dtrsystem.repository.AttendanceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceEntity createAttendance(AttendanceRequest dto) {
        AttendanceEntity attendance = new AttendanceEntity();
        mapDtoToEntity(dto, attendance);
        return attendanceRepository.save(attendance);
    }

    public List<AttendanceEntity> getAllAttendances() {
        return attendanceRepository.findAll();
    }

    public AttendanceEntity getAttendanceById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attendance record not found with id: " + id));
    }

    public List<AttendanceEntity> getAttendancesByUserId(Long userId) {
        return attendanceRepository.findByUserId(userId);
    }

    public AttendanceEntity updateAttendance(Long id, AttendanceRequest dto) {
        AttendanceEntity attendance = getAttendanceById(id);
        mapDtoToEntity(dto, attendance);
        return attendanceRepository.save(attendance);
    }

    public void deleteAttendance(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new EntityNotFoundException("Attendance record not found with id: " + id);
        }
        attendanceRepository.deleteById(id);
    }

    private void mapDtoToEntity(AttendanceRequest dto, AttendanceEntity entity) {
        entity.setDate(dto.getDate());
        entity.setTimeIn(dto.getTimeIn());
        entity.setTimeOut(dto.getTimeOut());
        entity.setTotalHours(dto.getTotalHours());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        UserEntity user = new UserEntity();
        user.setId(dto.getUserId());
        entity.setUser(user);
    }
}
