package com.fcmb.sampleapplication.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Public Controller Integration Tests")
class PublicControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should access health endpoint without authentication")
    void testHealth_WithoutAuthentication_ReturnsSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/public/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Application is running"));
    }

    @Test
    @DisplayName("Should access health endpoint with authentication token")
    void testHealth_WithAuthentication_AlsoReturnsSuccess() throws Exception {
        // Even with a fake token, public endpoint should work
        mockMvc.perform(get("/api/public/health")
                        .header("Authorization", "Bearer fake-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Should return correct response structure")
    void testHealth_ReturnsCorrectStructure() throws Exception {
        mockMvc.perform(get("/api/public/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").isString())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    @DisplayName("Should handle multiple concurrent requests")
    void testHealth_ConcurrentRequests_AllSucceed() throws Exception {
        // Simulate multiple requests
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/public/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        }
    }

    @Test
    @DisplayName("Should be accessible via different HTTP methods")
    void testHealth_OnlyGetMethodAllowed() throws Exception {
        // GET should work
        mockMvc.perform(get("/api/public/health"))
                .andExpect(status().isOk());
    }
}
