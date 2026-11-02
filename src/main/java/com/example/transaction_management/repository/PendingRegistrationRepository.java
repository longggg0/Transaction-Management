package com.example.transaction_management.repository;

import com.example.transaction_management.entity.PendingRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistrationEntity, Integer> {
    Optional<PendingRegistrationEntity> findByEmail(String email);
}
