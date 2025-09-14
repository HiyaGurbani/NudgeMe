package com.nudgeme.nudgeme.service;

import com.nudgeme.nudgeme.dto.UserProfileUpdateRequestDTO;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.UserProfile;
import com.nudgeme.nudgeme.repository.UserProfileRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;



    public UserProfile updateUserProfile(Long userId, UserProfileUpdateRequestDTO request) {
        // Try to find profile
        UserProfile profile = userProfileRepository.findByUser_Id(userId).orElse(null);

        if (profile == null) {
            // First time → create new
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            profile = new UserProfile();
            profile.setUser(user);
        }

        // Update fields safely (only overwrite if provided in request)
        profile.setLocation(request.getLocation());
        profile.setBio(request.getBio());
        profile.setImage(request.getImage());
        profile.setDob(request.getDob());

        return userProfileRepository.save(profile);
    }

}
