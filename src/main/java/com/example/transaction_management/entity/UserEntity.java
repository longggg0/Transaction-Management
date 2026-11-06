package com.example.transaction_management.entity;

import com.example.transaction_management.common.enums.Role;
import com.example.transaction_management.common.id.PrefixedId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "users")
@DynamicUpdate
public class UserEntity {

    @Id
    @PrefixedId(prefix = "USER_")
    @Column(length = 30)
    private String id;

    @Column(name = "username", nullable = false, length = 200, unique = true)
    private String username;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "email", nullable = false, length = 200, unique = true)
    private String email;

    @Column(name = "password", nullable = false, length = 200)
    private String password;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

}
