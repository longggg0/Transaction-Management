package com.example.transaction_management.repository;

import com.example.transaction_management.common.enums.OtpPurpose;
import com.example.transaction_management.entity.OtpCodeEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCodeEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpCodeEntity> findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String userId, OtpPurpose purpose);

    Optional<OtpCodeEntity> findFirstByUserIdAndPurposeOrderByCreatedAtDesc(
            String userId, OtpPurpose purpose);
}
