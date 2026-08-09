package com.tensura.dtrsystem.service;

import com.tensura.dtrsystem.dto.AttendanceRequest;
import com.tensura.dtrsystem.dto.AttendanceResponse;
import com.tensura.dtrsystem.dto.MonthlyAttendanceResponse;
import com.tensura.dtrsystem.entity.AttendanceEntity;
import com.tensura.dtrsystem.entity.UserEntity;
import com.tensura.dtrsystem.repository.AttendanceRepository;
import java.io.IOException;

import com.tensura.dtrsystem.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

            // 1. Calculate raw duration assuming same UTC day
            Duration duration = Duration.between(in, out);

            // 2. Since it's UTC time strings without dates:
            // If out is before in, it means the shift crossed the UTC midnight boundary.
            if (duration.isNegative()) {
                duration = duration.plusDays(1); // Adds 24 hours to fix overnight shifts
            }

            // 3. Convert total minutes to a precise double fraction (e.g., 515 mins / 60.0 = 8.5833)
            double preciseHours = duration.toMinutes() / 60.0;

            // 4. (Optional) Round to 2 decimal places so it doesn't store trailing numbers like 8.58333333
            preciseHours = Math.round(preciseHours * 100.0) / 100.0;

            attendance.setTotalHours(preciseHours);
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
    public List<AttendanceResponse> getRecentAttendancesByUserId(Long userId) {
        enforceAdminOrSelf(userId);
        List<AttendanceEntity> entities = attendanceRepository.findByUserIdOrderByIdDesc(userId);
        return entities.stream().map(AttendanceResponse::fromEntity).collect(Collectors.toList());
    }


    private String uploadAttendancePicture(UserEntity user, MultipartFile picture) throws IOException {
        String sanitizedName = (user.getFirstName() + "_" + user.getLastName()).toLowerCase().replaceAll("\\s+", "_");
        String userFolder = "dtrsystem/attendance/" + sanitizedName + "_" + user.getId();
        return fileStorageService.uploadFile(picture, userFolder);
    }

    private void enforceAdminOrSelf(Long requestedId) {
        UserEntity loggedUser = getCurrentAuthenticatedUser();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(loggedUser.getRole());
        boolean isSelf = loggedUser.getId().equals(requestedId);

        if (!isAdmin && !isSelf) {
            throw new RuntimeException("Access Denied: You do not have permission to access this user's information.");
        }
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

        // Sum up the precise Double values and safeguard against NullPointerExceptions
        double totalHoursInMonth = monthlyLogs.stream()
                .map(AttendanceEntity::getTotalHours)
                .filter(Objects::nonNull) // Ignores days where totalHours is not yet calculated
                .mapToDouble(Double::doubleValue)
                .sum();

        List<AttendanceResponse> safeRecords = monthlyLogs.stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());

        // Pass the double directly to your MonthlyAttendanceResponse constructor
        return new MonthlyAttendanceResponse(safeRecords, totalHoursInMonth);
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
        entity.setTotalHours((double) dto.getTotalHours());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        UserEntity user = new UserEntity();
        user.setId(dto.getUserId());
        entity.setUser(user);
    }
}
