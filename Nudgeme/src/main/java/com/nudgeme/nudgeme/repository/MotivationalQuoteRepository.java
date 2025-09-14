package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.MotivationalQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MotivationalQuoteRepository extends JpaRepository<MotivationalQuote, Long> {
    boolean existsByText(String text); // to avoid duplicates
}
