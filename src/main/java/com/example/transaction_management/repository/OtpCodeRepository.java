package com.example.transaction_management.repository;

import com.example.transaction_management.common.enums.OtpPurpose;
import com.example.transaction_management.entity.OtpCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCodeEntity, Integer> {

    Optional<OtpCodeEntity> findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String userId, OtpPurpose purpose);

    Optional<OtpCodeEntity> findFirstByUserIdAndPurposeOrderByCreatedAtDesc(
            String userId, OtpPurpose purpose);
}
