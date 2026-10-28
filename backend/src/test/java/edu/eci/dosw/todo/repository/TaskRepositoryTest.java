package edu.eci.dosw.todo.repository;

import edu.eci.dosw.todo.entity.TaskEntity;
import edu.eci.dosw.todo.entity.TaskPriority;
import edu.eci.dosw.todo.entity.TaskStatus;
import edu.eci.dosw.todo.entity.UserEntity;
import edu.eci.dosw.todo.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity ana;
    private UserEntity luis;
    private TaskEntity anaTask;
    private TaskEntity luisTask;

    @BeforeEach
    void setUp() {
        ana = userRepository.save(user("Ana Pérez", "ana@example.com"));
        luis = userRepository.save(user("Luis Gómez", "luis@example.com"));
        anaTask = taskRepository.save(task("Entregar laboratorio", ana));
        taskRepository.save(task("Organizar apuntes", ana));
        luisTask = taskRepository.save(task("Comprar mercado", luis));
    }

    @Test
    void findAllByOwnerIdReturnsOnlyTheOwnerTasks() {
        List<TaskEntity> tasks = taskRepository.findAllByOwnerId(ana.getId());

        assertThat(tasks).hasSize(2)
                .extracting(TaskEntity::getTitle)
                .containsExactlyInAnyOrder("Entregar laboratorio", "Organizar apuntes");
    }

    @Test
    void findAllByOwnerIdDoesNotMixUsers() {
        List<TaskEntity> tasks = taskRepository.findAllByOwnerId(luis.getId());

        assertThat(tasks).extracting(TaskEntity::getTitle).containsExactly("Comprar mercado");
    }

    @Test
    void findByIdAndOwnerIdFindsOwnTask() {
        assertThat(taskRepository.findByIdAndOwnerId(anaTask.getId(), ana.getId())).isPresent();
    }

    @Test
    void findByIdAndOwnerIdDoesNotReturnAnotherUserTask() {
        assertThat(taskRepository.findByIdAndOwnerId(luisTask.getId(), ana.getId())).isEmpty();
    }

    @Test
    void findByEmailFindsRegisteredUser() {
        assertThat(userRepository.findByEmail("ana@example.com"))
                .get()
                .extracting(UserEntity::getName)
                .isEqualTo("Ana Pérez");
        assertThat(userRepository.existsByEmail("nadie@example.com")).isFalse();
    }

    private UserEntity user(String name, String email) {
        UserEntity user = new UserEntity();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(UserRole.USER);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private TaskEntity task(String title, UserEntity owner) {
        TaskEntity task = new TaskEntity();
        task.setTitle(title);
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(TaskPriority.MEDIUM);
        task.setCreatedAt(LocalDateTime.now());
        task.setOwner(owner);
        return task;
    }
}
