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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@EnabledIfSystemProperty(named = "it.docker", matches = "true")
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationSubmitRequiredDocsIntegrationTest extends IntegrationTestBase {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void submitDraft_missingRequiredDocs_returns400() throws Exception {
        // create scheme with required doc AADHAAR
        SchemeDtos.CreateSchemeRequest createScheme = new SchemeDtos.CreateSchemeRequest(
                "SCM-REQ-1",
                "Req Docs Scheme",
                "desc",
                "dept",
                "cash",
                "details",
                SchemeStatus.ACTIVE,
                null,
                null,
                java.util.List.of(new SchemeDtos.DocumentRequirementDto("AADHAAR", true))
        );

        String schemeJson = mockMvc.perform(post("/api/schemes")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createScheme)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long schemeId = objectMapper.readTree(schemeJson).get("id").asLong();

        // citizen creates draft
        String draftJson = mockMvc.perform(post("/api/applications/draft")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("u1").roles("CITIZEN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"schemeId\":" + schemeId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        long appId = objectMapper.readTree(draftJson).get("id").asLong();

        // submit without documents should fail
        mockMvc.perform(post("/api/applications/my/{id}/submit", appId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("u1").roles("CITIZEN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_REQUIRED_DOCUMENTS"))
                .andExpect(jsonPath("$.missingDocTypes[0]").value(org.hamcrest.Matchers.containsStringIgnoringCase("aadhaar")));
    }
}
