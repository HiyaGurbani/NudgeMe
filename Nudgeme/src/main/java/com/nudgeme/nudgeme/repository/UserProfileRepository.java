package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser(User user);
    Optional<UserProfile> findByUser_Id(Long user_id);

}
