package com.dvgs.scheme;

import com.dvgs.scheme.domain.SchemeStatus;
import com.dvgs.scheme.dto.SchemeDtos;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@EnabledIfSystemProperty(named = "it.docker", matches = "true")
@SpringBootTest
@AutoConfigureMockMvc
class ArchivedSchemeBlocksDraftIntegrationTest extends IntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void createDraft_forArchivedScheme_returns409() throws Exception {
        // create scheme
        SchemeDtos.CreateSchemeRequest createScheme = new SchemeDtos.CreateSchemeRequest(
                "SCM-ARCH-BLOCK-1",
                "Archived Block",
                "desc",
                "dept",
                "cash",
                "details",
                SchemeStatus.ACTIVE,
                null,
                null,
                java.util.List.of()
        );

        String schemeJson = mockMvc.perform(post("/api/schemes")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createScheme)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long schemeId = objectMapper.readTree(schemeJson).get("id").asLong();

        // archive
        mockMvc.perform(post("/api/schemes/{id}/archive", schemeId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        // citizen create draft should fail
        mockMvc.perform(post("/api/applications/draft")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("u1").roles("CITIZEN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"schemeId\":" + schemeId + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SCHEME_ARCHIVED"))
                .andExpect(jsonPath("$.schemeId").value((int) schemeId));
    }
}
