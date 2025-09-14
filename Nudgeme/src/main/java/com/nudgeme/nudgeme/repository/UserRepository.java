package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUuid(String uuid);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
//    Optional<User> getFullName();// ✅ New method for UUID
}
