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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cadastro público desabilitado + gestão de usuários restrita a administradores.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserManagementTest {

    private static final String ADMIN = "marina@petly.com.br";
    private static final String STAFF = "ana@petly.com.br";
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicRegistrationIsDisabled() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("petshopName", "Novo Petshop");
        body.put("name", "Alguém");
        body.put("email", "alguem@example.com");
        body.put("password", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateAUserWhoCanThenLogIn() throws Exception {
        String token = login(ADMIN, PASSWORD);
        String email = "novo.funcionario@petly.com.br";

        mockMvc.perform(post("/api/users")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPayload("Novo Funcionário", email, "senha12345", "STAFF")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.active").value(true));

        login(email, "senha12345");
    }

    @Test
    void adminCannotCreateDuplicateEmail() throws Exception {
        String token = login(ADMIN, PASSWORD);
        mockMvc.perform(post("/api/users")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPayload("Duplicado", STAFF, "senha12345", "STAFF")))
                .andExpect(status().isConflict());
    }

    @Test
    void staffCannotManageUsers() throws Exception {
        String token = login(STAFF, PASSWORD);
        mockMvc.perform(get("/api/users").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/users")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPayload("Sem Permissão", "sem.permissao@petly.com.br", "senha12345", "STAFF")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotDeleteOwnAccount() throws Exception {
        String token = login(ADMIN, PASSWORD);
        long ownId = userIdByEmail(token, ADMIN);

        mockMvc.perform(delete("/api/users/" + ownId).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void usersAreTenantScoped() throws Exception {
        String token = login(ADMIN, PASSWORD);
        String response = mockMvc.perform(get("/api/users").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Todo usuário listado pertence ao petshop do admin (nunca de outro tenant).
        for (JsonNode user : objectMapper.readTree(response)) {
            String email = user.get("email").asText();
            if (email.contains("petlyjardins")) {
                throw new AssertionError("Usuário de outro petshop vazou: " + email);
            }
        }
    }

    // ---- helpers ------------------------------------------------------

    private String login(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", password));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private long userIdByEmail(String token, String email) throws Exception {
        String response = mockMvc.perform(get("/api/users").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode user : objectMapper.readTree(response)) {
            if (email.equalsIgnoreCase(user.get("email").asText())) {
                return user.get("id").asLong();
            }
        }
        throw new AssertionError("Usuário não encontrado: " + email);
    }

    private String userPayload(String name, String email, String password, String role) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        body.put("role", role);
        return objectMapper.writeValueAsString(body);
    }
}
