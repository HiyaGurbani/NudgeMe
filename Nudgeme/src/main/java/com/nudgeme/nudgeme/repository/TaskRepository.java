package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.Goal;
import com.nudgeme.nudgeme.model.Task;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByGoalId(Long goalId);
    @Query("SELECT t FROM Task t WHERE t.goal.user = :user AND t.dueDate = :date")
    List<Task> findByUserAndDueDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.goal.user = :user AND t.dueDate = :date AND t.status = :status")
    List<Task> findByUserAndDueDateAndStatus(@Param("user") User user,
                                                  @Param("date") LocalDate date,
                                                  @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.goal.user.uuid = :userUuid AND t.status = 'COMPLETED' ORDER BY t.completedOn DESC")
    List<Task> findCompletedTasksByUser(@Param("userUuid") String userUuid);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.goal = :goal AND t.status <> :completed")
    long countIncompleteTasksByGoal(@Param("goal") Goal goal,
                                    @Param("completed") TaskStatus completed);


}
