package io.github.raissaamaral.mapadevagas.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService service;

    @Test
    void createShouldReturnBadRequestWhenCompanyIsMissing() throws Exception {
        String body = """
                {
                  "jobTitle": "Estágio"
                }
                """;

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.company").exists());

        verify(service, never()).create(any());
    }

    @Test
    void createShouldReturnBadRequestWhenJobUrlHasUnsafeScheme() throws Exception {
        String body = """
                {
                  "jobTitle": "Estágio",
                  "company": "Rai Corp",
                  "jobUrl": "javascript:alert(1)"
                }
                """;

        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.jobUrl").exists());

        verify(service, never()).create(any());
    }
}