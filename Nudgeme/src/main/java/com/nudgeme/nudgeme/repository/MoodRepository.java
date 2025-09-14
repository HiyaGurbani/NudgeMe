package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.Mood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoodRepository extends JpaRepository<Mood, Long> {
    Mood findByType(String type);
}
