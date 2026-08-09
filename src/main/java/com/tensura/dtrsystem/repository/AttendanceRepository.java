package com.tensura.dtrsystem.repository;

import com.tensura.dtrsystem.entity.AttendanceEntity;
import com.tensura.dtrsystem.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
    List<AttendanceEntity> findByUserId(Long userId);

    List<AttendanceEntity> findByUserIdOrderByIdDesc(Long userId);
    Optional<AttendanceEntity> findTopByUserIdOrderByDateTimeDesc(Long id);

    Optional<AttendanceEntity> findByUserAndDate(UserEntity user, Date todayDate);

    @Query("SELECT a FROM AttendanceEntity a WHERE a.user.id = :userId AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date ASC")
    List<AttendanceEntity> findMonthlyAttendance(
            @Param("userId") Long userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
}