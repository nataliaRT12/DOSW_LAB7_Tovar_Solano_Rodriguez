package edu.eci.dosw.todo.repository;
 
import edu.eci.dosw.todo.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    List<TaskEntity> findAllByOwnerId(Long ownerId);

    Optional<TaskEntity> findByIdAndOwnerId(Long id, Long ownerId);
}