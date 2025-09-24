package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.bankcards.BankcardsApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BankcardsApplication.class,
properties = {
        "security.jwt.secret=B4nkC4rdS3cr3tK3yF0rJvT7890!@#$AbCdEfGhIjKlMnOpQrStUvWxYz",
        "security.jwt.expiration=86400000",
        "security.jwt.header=Authorization",
        "security.jwt.prefix=Bearer"
        })
@AutoConfigureMockMvc
@Transactional
public class CardControllerIntegrationTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        @Primary
        public PasswordEncoder passwordEncoder() {
            return NoOpPasswordEncoder.getInstance();
        }
    }

    @Autowired
    private MockMvc mockMvc;

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

        return content.substring(content.indexOf("token")+8, content.indexOf("\"}}"));
    }


    @Test
    void userEndpoint_NoToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/user/cards"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userEndpoint_WithValidUserToken_ReturnsOk() throws Exception {
        String userToken = obtainJwtToken("user", "user");

        mockMvc.perform(get("/api/v1/user/cards")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }


    @Test
    void adminEndpoint_WithValidAdminToken_ReturnsCreated() throws Exception {
        String adminToken = obtainJwtToken("admin", "admin");

        String requestBody = "{\"ownerId\": 101, \"cardNumber\": \"1111222233334444\", \"expiryDate\": \"2028-12-31\"}";

        mockMvc.perform(post("/api/v1/admin/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void adminEndpoint_WithUserToken_ReturnsForbidden() throws Exception {
        String userToken = obtainJwtToken("user", "user");

        String requestBody = "{\"ownerId\": 101, \"cardNumber\": \"1111222233334444\", \"expiryDate\": \"2028-12-31\"}";

        mockMvc.perform(post("/api/v1/admin/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_DeleteCard_ReturnsNoContent() throws Exception {
        String adminToken = obtainJwtToken("admin", "admin");


        mockMvc.perform(delete("/api/v1/admin/cards/101")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
