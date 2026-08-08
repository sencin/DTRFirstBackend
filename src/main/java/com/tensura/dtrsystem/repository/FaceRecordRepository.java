package com.tensura.dtrsystem.repository;

import com.tensura.dtrsystem.entity.UserFaceRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FaceRecordRepository extends JpaRepository<UserFaceRecordEntity, Long> {
    Optional<UserFaceRecordEntity> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}