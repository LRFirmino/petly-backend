package com.pawsclaws.petshop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the core security requirement: a login can only ever see and change
 * data of its own petshop.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TenantIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String MOEMA_ADMIN = "marina@petly.com.br";
    private static final String JARDINS_ADMIN = "carla@petlyjardins.com.br";
    private static final String PASSWORD = "password123";

    @Test
    void protectedEndpointsRejectAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/clients")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/pets")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/appointments")).andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/clients").header(AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", MOEMA_ADMIN, "password", "wrong-password"));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void eachLoginOnlySeesAndChangesItsOwnPetshop() throws Exception {
        String pawsToken = login(MOEMA_ADMIN, PASSWORD);
        String tailsToken = login(JARDINS_ADMIN, PASSWORD);

        List<Long> pawsClients = clientIds(pawsToken);
        List<Long> tailsClients = clientIds(tailsToken);

        assertThat(pawsClients).isNotEmpty();
        assertThat(tailsClients).isNotEmpty();
        assertThat(pawsClients).doesNotContainAnyElementsOf(tailsClients);

        // Reading another petshop's client must look like it does not exist.
        for (Long foreignId : tailsClients) {
            mockMvc.perform(get("/api/clients/" + foreignId).header(AUTHORIZATION, "Bearer " + pawsToken))
                    .andExpect(status().isNotFound());
        }

        // Updating and deleting must be blocked too.
        String updateBody = objectMapper.writeValueAsString(Map.of("name", "Hacked From Another Tenant"));
        for (Long foreignId : tailsClients) {
            mockMvc.perform(put("/api/clients/" + foreignId)
                            .header(AUTHORIZATION, "Bearer " + pawsToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateBody))
                    .andExpect(status().isNotFound());

            mockMvc.perform(delete("/api/clients/" + foreignId)
                            .header(AUTHORIZATION, "Bearer " + pawsToken))
                    .andExpect(status().isNotFound());
        }

        // The other tenant's data is untouched.
        assertThat(clientIds(tailsToken)).containsExactlyElementsOf(tailsClients);
    }

    @Test
    void authenticatedUserCanReadItsOwnProfile() throws Exception {
        String token = login(MOEMA_ADMIN, PASSWORD);

        String response = mockMvc.perform(get("/api/auth/me").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        assertThat(json.get("email").asText()).isEqualTo(MOEMA_ADMIN);
        assertThat(json.get("petshop").get("name").asText()).isEqualTo("Test Petshop Moema");
    }

    private String login(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", password));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private List<Long> clientIds(String token) throws Exception {
        String response = mockMvc.perform(get("/api/clients").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Long> ids = new ArrayList<>();
        objectMapper.readTree(response).forEach(node -> ids.add(node.get("id").asLong()));
        return ids;
    }
}
