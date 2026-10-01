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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void findAllShouldPassFiltersToService() throws Exception {
        mockMvc.perform(get("/applications")
                        .param("status", "APPLIED")
                        .param("workModel", "REMOTE"))
                .andExpect(status().isOk());

        verify(service).findAll(ApplicationStatus.APPLIED, null, WorkModel.REMOTE);
    }

    @Test
    void findAllShouldReturnBadRequestWhenFilterIsInvalid() throws Exception {
        mockMvc.perform(get("/applications").param("status", "ABC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter: status"));

        verify(service, never()).findAll(any(), any(), any());
    }
}