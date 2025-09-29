package com.example.bankcards.controller;

import com.example.bankcards.BankcardsApplication;
import com.example.bankcards.dto.CardCreationRequest;
import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.repository.CardRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(
        classes = BankcardsApplication.class,
        properties = {
                "security.jwt.secret=B4nkC4rdS3cr3tK3yF0rJvT7890!@#$AbCdEfGhIjKlMnOpQrStUvWxYz",
                "security.jwt.expiration=86400000",
                "security.jwt.header=Authorization",
                "security.jwt.prefix=Bearer"
        })
@AutoConfigureMockMvc
public class UserControllerIntegrationTcTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("bank_db")
            .withUsername("postgres")
            .withPassword("admin");

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/migration/master.yaml");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardRepository cardRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String obtainJwtToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        return objectMapper.readValue(content, Map.class).get("token").toString();
    }

    @Test
    void userEndpoint_NoToken_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/user/cards"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userEndpoint_WithValidUserToken_ReturnsOk() throws Exception {
        String userToken = obtainJwtToken("user", "user");
        String adminToken = obtainJwtToken("admin", "admin");

        CardCreationRequest requestBody = new CardCreationRequest(101L, "1111222233334445", "2028-12-31");

        mockMvc.perform(post("/api/v1/admin/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andReturn();

        mockMvc.perform(get("/api/v1/user/cards")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 4445"))
                .andExpect(jsonPath("$.content[0].expiryDate").value("2028-12-31"))
                .andExpect(jsonPath("$.content[0].ownerId").value(101))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.size").value(20));
    }
}
