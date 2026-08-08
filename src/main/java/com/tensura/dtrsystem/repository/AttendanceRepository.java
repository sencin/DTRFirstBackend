package com.tensura.dtrsystem.repository;

import com.tensura.dtrsystem.entity.AttendanceEntity;
import com.tensura.dtrsystem.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
    List<AttendanceEntity> findByUserId(Long userId);

    Optional<AttendanceEntity> findTopByUserIdOrderByDateTimeDesc(Long id);

    Optional<AttendanceEntity> findByUserAndDate(UserEntity user, Date todayDate);
}