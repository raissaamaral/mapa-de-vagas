package io.github.raissaamaral.mapadevagas.application;

import io.github.raissaamaral.mapadevagas.user.User;
import io.github.raissaamaral.mapadevagas.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApplicationOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private User owner;
    private User otherUser;
    private Application application;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User("owner@test.local", "hash", Instant.now()));
        otherUser = userRepository.save(new User("other@test.local", "hash", Instant.now()));

        Application newApplication = new Application();
        newApplication.setCompany("Rai Corp");
        newApplication.setJobTitle("Estágio");
        newApplication.setStatus(ApplicationStatus.SAVED);
        newApplication.setOwner(owner);
        application = applicationRepository.save(newApplication);
    }

    @Test
    void ownerCanViewTheirApplication() throws Exception {
        mockMvc.perform(get("/applications/{id}", application.getId()).with(authenticatedAs(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("Rai Corp"));
    }

    @Test
    void listOnlyReturnsTheUsersOwnApplications() throws Exception {
        mockMvc.perform(get("/applications").with(authenticatedAs(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/applications").with(authenticatedAs(otherUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void otherUserCannotAccessOrChangeTheApplication() throws Exception {
        Long id = application.getId();

        mockMvc.perform(get("/applications/{id}", id).with(authenticatedAs(otherUser)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/applications/{id}/history", id).with(authenticatedAs(otherUser)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/applications/{id}", id).with(authenticatedAs(otherUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"company": "Changed", "jobTitle": "Changed"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/applications/{id}/status", id).with(authenticatedAs(otherUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "REJECTED"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/applications/{id}", id).with(authenticatedAs(otherUser)))
                .andExpect(status().isNotFound());

        Application unchanged = applicationRepository.findById(id).orElseThrow();
        assertEquals("Rai Corp", unchanged.getCompany());
        assertEquals(ApplicationStatus.SAVED, unchanged.getStatus());
    }

    private static JwtRequestPostProcessor authenticatedAs(User user) {
        return jwt().jwt(token -> token.subject(user.getId().toString()));
    }
}