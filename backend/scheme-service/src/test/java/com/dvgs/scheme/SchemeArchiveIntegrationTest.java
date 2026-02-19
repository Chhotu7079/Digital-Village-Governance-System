package com.dvgs.scheme;

import com.dvgs.scheme.domain.SchemeStatus;
import com.dvgs.scheme.dto.SchemeDtos;
import com.dvgs.scheme.repository.SchemeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@EnabledIfSystemProperty(named = "it.docker", matches = "true")
@SpringBootTest
@AutoConfigureMockMvc
class SchemeArchiveIntegrationTest extends IntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired SchemeRepository schemeRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void archiveAndHideFromNonAdminList() throws Exception {
        // create scheme
        SchemeDtos.CreateSchemeRequest req = new SchemeDtos.CreateSchemeRequest(
                "SCM-ARCH-1",
                "Archive Test",
                "desc",
                "dept",
                "cash",
                "details",
                SchemeStatus.ACTIVE,
                null,
                null,
                java.util.List.of()
        );

        String createdJson = mockMvc.perform(post("/api/schemes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        Long schemeId = objectMapper.readTree(createdJson).get("id").asLong();

        // archive it
        mockMvc.perform(post("/api/schemes/{id}/archive", schemeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        // non-admin listing should NOT show archived even if includeArchived=true
        mockMvc.perform(get("/api/schemes")
                        .param("includeArchived", "true")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("c").roles("CITIZEN")))
                .andExpect(status().isOk())
                // content[0] might be empty - we assert no entry with this schemeCode
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SCM-ARCH-1"))));

        // admin listing with includeArchived=true should show it
        mockMvc.perform(get("/api/schemes")
                        .param("includeArchived", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SCM-ARCH-1")));
    }
}
