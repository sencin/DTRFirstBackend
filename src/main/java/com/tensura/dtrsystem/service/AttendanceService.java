package com.tensura.dtrsystem.service;

import com.tensura.dtrsystem.dto.AttendanceRequest;
import com.tensura.dtrsystem.dto.MonthlyAttendanceResponse;
import com.tensura.dtrsystem.entity.AttendanceEntity;
import com.tensura.dtrsystem.entity.UserEntity;
import com.tensura.dtrsystem.repository.AttendanceRepository;
import java.io.IOException;

import com.tensura.dtrsystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public AttendanceEntity createAttendance(BigDecimal latitude, BigDecimal longitude, MultipartFile picture) throws IOException {
        UserEntity user = getCurrentAuthenticatedUser();

        ZoneId localZone = ZoneId.of("Asia/Manila");
        LocalDate localToday = LocalDate.now(localZone);
        Date todayDate = Date.from(localToday.atStartOfDay(localZone).toInstant());

        Optional<AttendanceEntity> existingAttendanceOpt = attendanceRepository.findByUserAndDate(user, todayDate);

        if (existingAttendanceOpt.isPresent()) {
            AttendanceEntity existingRecord = existingAttendanceOpt.get();
            boolean hasTimeIn = existingRecord.getTimeIn() != null && !existingRecord.getTimeIn().isBlank();
            boolean hasTimeOut = existingRecord.getTimeOut() != null && !existingRecord.getTimeOut().isBlank();

            if (hasTimeIn && hasTimeOut) {
                throw new RuntimeException("You already completed your attendance today");
            }
        }

        String imageUrl = uploadAttendancePicture(user, picture);

        LocalTime utcTimeNow = LocalTime.now(ZoneOffset.UTC);
        String utcTimeString = utcTimeNow.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        AttendanceEntity attendance = attendanceRepository.findByUserAndDate(user, todayDate)
                .orElseGet(() -> {
                    AttendanceEntity newRecord = new AttendanceEntity();
                    newRecord.setUser(user);
                    newRecord.setDate(todayDate);
                    newRecord.setLatitude(latitude);
                    newRecord.setLongitude(longitude);
                    return newRecord;
                });

        if (attendance.getTimeIn() == null || attendance.getTimeIn().isBlank()) {
            attendance.setTimeIn(utcTimeString);
            attendance.setTimeInImage(imageUrl);
            System.out.println("Saved TIME IN (UTC time string): " + utcTimeString);
        } else if (attendance.getTimeOut() == null || attendance.getTimeOut().isBlank()) {
            attendance.setTimeOut(utcTimeString);
            attendance.setTimeOutImage(imageUrl);
            System.out.println("Saved TIME OUT (UTC time string): " + utcTimeString);
            calculateTotalHours(attendance);
        } else {
            throw new RuntimeException("Punch Failed: You have already completed your Attendance for today.");
        }

        return attendanceRepository.save(attendance);
    }

    private void calculateTotalHours(AttendanceEntity attendance) {
        try {
            LocalTime in = LocalTime.parse(attendance.getTimeIn());
            LocalTime out = LocalTime.parse(attendance.getTimeOut());
            long hours = Duration.between(in, out).toHours();
            if (out.isBefore(in)) {
                hours = Duration.between(in, out.plus(Duration.ofDays(1))).toHours();
            }
            attendance.setTotalHours((int) hours);
        } catch (Exception e) {
            System.out.println("Could not compute duration automatically: " + e.getMessage());
        }
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

    private String uploadAttendancePicture(UserEntity user, MultipartFile picture) throws IOException {
        String sanitizedName = (user.getFirstName() + "_" + user.getLastName()).toLowerCase().replaceAll("\\s+", "_");
        String userFolder = "dtrsystem/attendance/" + sanitizedName + "_" + user.getId();
        return fileStorageService.uploadFile(picture, userFolder);
    }

    private UserEntity getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Map<String, String> determinePunchType() {
        UserEntity loggedUser = getCurrentAuthenticatedUser();
        ZoneId localZone = ZoneId.of("Asia/Manila");
        LocalDate localToday = LocalDate.now(localZone);
        Date todayDate = Date.from(localToday.atStartOfDay(localZone).toInstant());
        Optional<AttendanceEntity> todayAttendanceOpt = attendanceRepository.findByUserAndDate(loggedUser, todayDate);

        Map<String, String> response = new HashMap<>();

        if (todayAttendanceOpt.isPresent()) {
            AttendanceEntity attendance = todayAttendanceOpt.get();
            if (attendance.getTimeIn() == null || attendance.getTimeIn().isBlank()) {
                response.put("punchType", "Time IN");
                return response;
            }
            if (attendance.getTimeOut() == null || attendance.getTimeOut().isBlank()) {
                response.put("punchType", "Time OUT");
                return response;
            }
            throw new RuntimeException("You have already completed your Attendance for today.");
        }

        response.put("punchType", "Time IN");
        return response;
    }

    public MonthlyAttendanceResponse getMonthlyAttendanceForUser(Long userId, int year, int month) {
        ZoneId localZone = ZoneId.of("Asia/Manila");
        LocalDate startLocalDate = LocalDate.of(year, month, 1);
        Date startDate = Date.from(startLocalDate.atStartOfDay(localZone).toInstant());
        LocalDate endLocalDate = startLocalDate.withDayOfMonth(startLocalDate.lengthOfMonth());
        Date endDate = Date.from(endLocalDate.atTime(23, 59, 59, 999).atZone(localZone).toInstant());
        List<AttendanceEntity> monthlyLogs = attendanceRepository.findMonthlyAttendance(userId, startDate, endDate);
        int totalHoursInMonth = monthlyLogs.stream()
                .mapToInt(AttendanceEntity::getTotalHours)
                .sum();
        return new MonthlyAttendanceResponse(monthlyLogs, totalHoursInMonth);
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
