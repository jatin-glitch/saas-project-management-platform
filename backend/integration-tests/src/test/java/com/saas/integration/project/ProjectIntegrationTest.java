package com.saas.integration.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.saas.project.ProjectServiceApplication.class)
@AutoConfigureWebMvc
public class ProjectIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> validProjectRequest;

    @BeforeEach
    void setUp() {
        validProjectRequest = new HashMap<>();
        validProjectRequest.put("name", "Test Project");
        validProjectRequest.put("description", "A test project for integration testing");
        validProjectRequest.put("code", "TEST-001");
        validProjectRequest.put("status", "PLANNING");
        validProjectRequest.put("priority", "MEDIUM");
        validProjectRequest.put("isPublic", false);
        validProjectRequest.put("tags", new String[]{"test", "integration"});
    }

    @Test
    @WithMockUser(roles = {"PROJECT_MANAGER"})
    void whenCreateProject_thenReturnCreatedProject() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .header("X-Tenant-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProjectRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.code").value("TEST-001"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("Test Project"));
        assertTrue(response.contains("TEST-001"));
    }

    @Test
    void whenCreateProjectWithoutAuth_thenReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .header("X-Tenant-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProjectRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void whenCreateProjectWithInsufficientRole_thenReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .header("X-Tenant-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProjectRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"PROJECT_MANAGER"})
    void whenGetAllProjects_thenReturnProjectList() throws Exception {
        // First create a project
        mockMvc.perform(post("/api/projects")
                        .header("X-Tenant-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProjectRequest)))
                .andExpect(status().isCreated());

        // Then get all projects
        mockMvc.perform(get("/api/projects")
                        .header("X-Tenant-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.size").isNumber());
    }

    @Test
    @WithMockUser(roles = {"PROJECT_MANAGER"})
    void whenGetProjectById_thenReturnProject() throws Exception {
        // First create a project
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .header("X-Tenant-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProjectRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        String projectId = objectMapper.readTree(response).get("id").asText();

        // Then get the project by ID
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("X-Tenant-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    @WithMockUser(roles = {"PROJECT_MANAGER"})
    void whenUpdateProject_thenReturnUpdatedProject() throws Exception {
        // First create a project
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .header("X-Tenant-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProjectRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        String projectId = objectMapper.readTree(response).get("id").asText();

        // Update the project
        Map<String, Object> updateRequest = new HashMap<>(validProjectRequest);
        updateRequest.put("name", "Updated Test Project");
        updateRequest.put("description", "Updated description");

        mockMvc.perform(put("/api/projects/" + projectId)
                        .header("X-Tenant-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("Updated Test Project"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void whenDeleteProject_thenReturnSuccess() throws Exception {
        // First create a project
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .header("X-Tenant-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProjectRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        String projectId = objectMapper.readTree(response).get("id").asText();

        // Delete the project
        mockMvc.perform(delete("/api/projects/" + projectId)
                        .header("X-Tenant-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project deleted successfully"));
    }

    @Test
    @WithMockUser(roles = {"PROJECT_MANAGER"})
    void whenSearchProjects_thenReturnMatchingProjects() throws Exception {
        // Create a project
        mockMvc.perform(post("/api/projects")
                        .header("X-Tenant-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProjectRequest)))
                .andExpect(status().isCreated());

        // Search for projects
        mockMvc.perform(get("/api/projects/search")
                        .header("X-Tenant-ID", "1")
                        .param("searchTerm", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void whenGetProjectStatistics_thenReturnStats() throws Exception {
        mockMvc.perform(get("/api/projects/statistics")
                        .header("X-Tenant-ID", "1"))
                .andExpect(status().isOk());
    }
}
