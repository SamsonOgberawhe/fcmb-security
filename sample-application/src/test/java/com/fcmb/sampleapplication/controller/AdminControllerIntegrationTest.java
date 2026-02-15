package com.fcmb.sampleapplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fcmb.sampleapplication.entity.User;
import com.fcmb.sampleapplication.repository.UserRepository;
import com.fcmb.security.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Admin Controller Integration Tests")
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;
    private String adminToken;
    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        // Clean database
        userRepository.deleteAll();

        // Create regular user
        testUser = User.builder()
                .username("regularuser")
                .password(passwordEncoder.encode("userpass"))
                .email("user@example.com")
                .roles(Set.of("ROLE_USER"))
                .enabled(true)
                .build();
        testUser = userRepository.save(testUser);

        // Create admin user
        testAdmin = User.builder()
                .username("adminuser")
                .password(passwordEncoder.encode("adminpass"))
                .email("admin@example.com")
                .roles(Set.of("ROLE_USER", "ROLE_ADMIN"))
                .enabled(true)
                .build();
        testAdmin = userRepository.save(testAdmin);

        // Create additional users for list testing
        User user2 = User.builder()
                .username("user2")
                .password(passwordEncoder.encode("pass2"))
                .email("user2@example.com")
                .roles(Set.of("ROLE_USER"))
                .enabled(true)
                .build();
        userRepository.save(user2);

        User user3 = User.builder()
                .username("user3")
                .password(passwordEncoder.encode("pass3"))
                .email("user3@example.com")
                .roles(Set.of("ROLE_USER"))
                .enabled(false) // Disabled user
                .build();
        userRepository.save(user3);

        // Generate tokens
        userToken = jwtUtil.generateToken(
                testUser.getId(),
                testUser.getUsername(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        adminToken = jwtUtil.generateToken(
                testAdmin.getId(),
                testAdmin.getUsername(),
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );
    }

    @Test
    @DisplayName("Should get all users with admin token")
    void testGetAllUsers_WithAdminToken_ReturnsAllUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(4))) // 4 users created in setup
                .andExpect(jsonPath("$[*].username", hasItems("regularuser", "adminuser", "user2", "user3")));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to access admin endpoint")
    void testGetAllUsers_WithUserToken_Returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/admin/users"));
    }

    @Test
    @DisplayName("Should return 401 without authentication token")
    void testGetAllUsers_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Should return 401 with invalid token")
    void testGetAllUsers_WithInvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer invalid-token"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Should include all user fields in response")
    void testGetAllUsers_ResponseContainsAllFields() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].roles").exists())
                .andExpect(jsonPath("$[0].enabled").exists())
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    @DisplayName("Should not expose passwords in response")
    void testGetAllUsers_DoesNotExposePasswords() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].password").doesNotExist());
    }

    @Test
    @DisplayName("Should include both enabled and disabled users")
    void testGetAllUsers_IncludesDisabledUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'user3')].enabled").value(false))
                .andExpect(jsonPath("$[?(@.username == 'regularuser')].enabled").value(true));
    }

    @Test
    @DisplayName("Should return empty array when no users exist")
    void testGetAllUsers_WithNoUsers_ReturnsEmptyArray() throws Exception {
        // Clean all users
        userRepository.deleteAll();

        // Create only admin to make the request
        User admin = User.builder()
                .username("onlyadmin")
                .password(passwordEncoder.encode("pass"))
                .email("only@example.com")
                .roles(Set.of("ROLE_ADMIN"))
                .enabled(true)
                .build();
        admin = userRepository.save(admin);

        String token = jwtUtil.generateToken(
                admin.getId(),
                admin.getUsername(),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // Delete admin too
        userRepository.deleteAll();

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return 403 with token lacking ADMIN role")
    void testGetAllUsers_WithOnlyUserRole_Returns403() throws Exception {
        // Create user with only ROLE_USER
        User userOnly = User.builder()
                .username("useronly")
                .password(passwordEncoder.encode("pass"))
                .email("useronly@example.com")
                .roles(Set.of("ROLE_USER"))
                .enabled(true)
                .build();
        userOnly = userRepository.save(userOnly);

        String onlyUserToken = jwtUtil.generateToken(
                userOnly.getId(),
                userOnly.getUsername(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + onlyUserToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Should handle malformed Authorization header")
    void testGetAllUsers_WithMalformedHeader_Returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "InvalidPrefix " + adminToken))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should work with valid admin token regardless of order")
    void testGetAllUsers_MultipleRequests_AllSucceed() throws Exception {
        // Make multiple requests to ensure consistency
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/admin/users")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(4)));
        }
    }

    @Test
    @DisplayName("Should return users sorted or in consistent order")
    void testGetAllUsers_ReturnsConsistentOrder() throws Exception {
        // First request
        String response1 = mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Second request
        String response2 = mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Responses should be identical for consistency
        assert response1.equals(response2) : "Responses should be consistent";
    }
}
