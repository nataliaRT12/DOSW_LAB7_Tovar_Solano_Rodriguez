package edu.eci.dosw.todo;

import edu.eci.dosw.todo.entity.TaskEntity;
import edu.eci.dosw.todo.entity.UserEntity;
import edu.eci.dosw.todo.entity.UserRole;
import edu.eci.dosw.todo.repository.TaskRepository;
import edu.eci.dosw.todo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    private static final String PASSWORD = "ContrasenaDemo123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void register(String name, String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());
    }

    private String login(String email) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private String registerAndLogin(String name, String email) throws Exception {
        register(name, email);
        return login(email);
    }

    private long createTask(String token, String title) throws Exception {
        String body = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void pt01_pt03_registerCreatesUserWithHashedPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"Ana@Example.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        UserEntity saved = userRepository.findByEmail("ana@example.com").orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo(PASSWORD).startsWith("$2");
    }

    @Test
    void pt02_duplicatedEmailReturns409WithoutDuplicates() throws Exception {
        register("Ana", "ana@example.com");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Otra Ana\",\"email\":\"ANA@example.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isConflict());

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void registerIgnoresAdminRoleSentByClient() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Eva\",\"email\":\"eva@example.com\",\"password\":\"" + PASSWORD + "\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void pt04_loginReturnsSignedToken() throws Exception {
        register("Ana", "ana@example.com");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.expiresIn").value(1800));
    }

    @Test
    void pt05_wrongPasswordReturns401WithoutToken() throws Exception {
        register("Ana", "ana@example.com");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"password\":\"otraClave123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void pt06_tasksWithoutTokenReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void pt07_tasksWithValidTokenReturnOnlyOwnTasks() throws Exception {
        String ana = registerAndLogin("Ana", "ana@example.com");
        String luis = registerAndLogin("Luis", "luis@example.com");
        createTask(ana, "Ana 1");
        createTask(ana, "Ana 2");
        createTask(luis, "Luis 1");

        String body = mockMvc.perform(get("/api/v1/tasks").header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode tasks = objectMapper.readTree(body);
        assertThat(tasks).hasSize(2);
        assertThat(tasks.findValuesAsString("title")).containsExactlyInAnyOrder("Ana 1", "Ana 2");
    }

    @Test
    void pt08_tamperedTokenReturns401() throws Exception {
        String token = registerAndLogin("Ana", "ana@example.com");
        String[] parts = token.split("\\.");
        char changed = parts[1].charAt(10) == 'A' ? 'B' : 'A';
        String tampered = parts[0] + "." + parts[1].substring(0, 10) + changed + parts[1].substring(11) + "." + parts[2];

        mockMvc.perform(get("/api/v1/tasks").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void pt09_expiredTokenReturns401() throws Exception {
        register("Ana", "ana@example.com");
        UserEntity ana = userRepository.findByEmail("ana@example.com").orElseThrow();
        Instant issuedAt = Instant.now().minus(Duration.ofMinutes(40));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("todo-api")
                .subject(String.valueOf(ana.getId()))
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(Duration.ofMinutes(30)))
                .claim("roles", List.of("USER"))
                .build();
        String expired = jwtEncoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        mockMvc.perform(get("/api/v1/tasks").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void pt10_getAnotherUserTaskReturns404() throws Exception {
        String ana = registerAndLogin("Ana", "ana@example.com");
        String luis = registerAndLogin("Luis", "luis@example.com");
        long anaTask = createTask(ana, "Privada de Ana");

        mockMvc.perform(get("/api/v1/tasks/" + anaTask).header("Authorization", "Bearer " + luis))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").doesNotExist());
    }

    @Test
    void pt11_updateAnotherUserTaskReturns404AndKeepsData() throws Exception {
        String ana = registerAndLogin("Ana", "ana@example.com");
        String luis = registerAndLogin("Luis", "luis@example.com");
        long anaTask = createTask(ana, "Privada de Ana");

        mockMvc.perform(put("/api/v1/tasks/" + anaTask)
                        .header("Authorization", "Bearer " + luis)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Cambiada por Luis\",\"status\":\"COMPLETED\",\"priority\":\"LOW\"}"))
                .andExpect(status().isNotFound());

        TaskEntity stored = taskRepository.findById(anaTask).orElseThrow();
        assertThat(stored.getTitle()).isEqualTo("Privada de Ana");
        assertThat(stored.getStatus().name()).isEqualTo("PENDING");
    }

    @Test
    void pt12_deleteAnotherUserTaskReturns404AndKeepsData() throws Exception {
        String ana = registerAndLogin("Ana", "ana@example.com");
        String luis = registerAndLogin("Luis", "luis@example.com");
        long anaTask = createTask(ana, "Privada de Ana");

        mockMvc.perform(delete("/api/v1/tasks/" + anaTask).header("Authorization", "Bearer " + luis))
                .andExpect(status().isNotFound());

        assertThat(taskRepository.existsById(anaTask)).isTrue();
    }

    @Test
    void pt13_createdTaskBelongsToTokenUser() throws Exception {
        String ana = registerAndLogin("Ana", "ana@example.com");
        Long anaId = userRepository.findByEmail("ana@example.com").orElseThrow().getId();

        long taskId = createTask(ana, "Nueva tarea");

        assertThat(taskRepository.findByIdAndOwnerId(taskId, anaId)).isPresent();
    }

    @Test
    void profileReturnsAuthenticatedUser() throws Exception {
        String ana = registerAndLogin("Ana", "ana@example.com");

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void pt14_userCannotAccessAdminSummary() throws Exception {
        String ana = registerAndLogin("Ana", "ana@example.com");

        mockMvc.perform(get("/api/v1/admin/summary").header("Authorization", "Bearer " + ana))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void pt15_adminCanAccessAdminSummary() throws Exception {
        String ana = registerAndLogin("Ana", "ana@example.com");
        createTask(ana, "Ana 1");
        register("Admin", "admin@example.com");
        UserEntity admin = userRepository.findByEmail("admin@example.com").orElseThrow();
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);
        String adminToken = login("admin@example.com");

        mockMvc.perform(get("/api/v1/admin/summary").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(2))
                .andExpect(jsonPath("$.totalTasks").value(1))
                .andExpect(jsonPath("$.tasksByStatus.PENDING").value(1));
    }
}
