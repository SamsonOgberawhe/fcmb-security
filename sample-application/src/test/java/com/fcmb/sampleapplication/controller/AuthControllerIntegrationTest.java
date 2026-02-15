package com.fcmb.sampleapplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fcmb.sampleapplication.dto.request.LoginRequest;
import com.fcmb.sampleapplication.entity.User;
import com.fcmb.sampleapplication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        userRepository.deleteAll();

        // Create test user
        User testUser = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("testpass"))
                .email("test@example.com")
                .roles(Set.of("ROLE_USER"))
                .enabled(true)
                .build();
        userRepository.save(testUser);

        // Create test admin
        User testAdmin = User.builder()
                .username("testadmin")
                .password(passwordEncoder.encode("adminpass"))
                .email("admin@example.com")
                .roles(Set.of("ROLE_USER", "ROLE_ADMIN"))
                .enabled(true)
                .build();
        userRepository.save(testAdmin);
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_WithValidCredentials_ReturnsToken() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("testpass")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles", hasItem("ROLE_USER")));
    }

    @Test
    @DisplayName("Should login admin with multiple roles")
    void testLogin_AsAdmin_ReturnsTokenWithMultipleRoles() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testadmin")
                .password("adminpass")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testadmin"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles", hasSize(2)))
                .andExpect(jsonPath("$.roles", hasItems("ROLE_USER", "ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Should return 401 with invalid password")
    void testLogin_WithInvalidPassword_Returns401() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    @DisplayName("Should return 401 with non-existent username")
    void testLogin_WithNonExistentUser_Returns401() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username("nonexistent")
                .password("password")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Should return 400 with missing username")
    void testLogin_WithMissingUsername_Returns400() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .password("testpass")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Should return 400 with missing password")
    void testLogin_WithMissingPassword_Returns400() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Should return 400 with empty request body")
    void testLogin_WithEmptyBody_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should successfully login disabled user returns 401")
    void testLogin_WithDisabledUser_Returns401() throws Exception {
        // Arrange - create disabled user
        User disabledUser = User.builder()
                .username("disabled")
                .password(passwordEncoder.encode("password"))
                .email("disabled@example.com")
                .roles(Set.of("ROLE_USER"))
                .enabled(false)
                .build();
        userRepository.save(disabledUser);

        LoginRequest loginRequest = LoginRequest.builder()
                .username("disabled")
                .password("password")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should generate valid JWT token structure")
    void testLogin_ReturnsValidJwtTokenStructure() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("testpass")
                .build();

        // Act
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        
        String token = objectMapper.readTree(response).get("token").asText();
        String[] tokenParts = token.split("\\.");
        
        assert tokenParts.length == 3 : "JWT should have 3 parts (header.payload.signature)";
    }
}
