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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Área do administrador da plataforma (equipe Petly): cria petshops e contas,
 * e é inacessível aos admins de petshop.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlatformAdminTest {

    // Conta de plataforma definida em src/test/resources/application-test.yml.
    private static final String PLATFORM = "platform.admin@test.local";
    private static final String PLATFORM_PASSWORD = "test-password-123";
    private static final String PETSHOP_ADMIN = "marina@petly.com.br";
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void anonymousCannotAccessPlatformArea() throws Exception {
        mockMvc.perform(get("/api/platform/petshops")).andExpect(status().isUnauthorized());
    }

    @Test
    void petshopAdminCannotAccessPlatformArea() throws Exception {
        String token = login(PETSHOP_ADMIN, PASSWORD);
        mockMvc.perform(get("/api/platform/petshops").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void platformAdminListsRealPetshopsOnly() throws Exception {
        String token = login(PLATFORM, PLATFORM_PASSWORD);
        String response = mockMvc.perform(get("/api/platform/petshops").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode shops = objectMapper.readTree(response);
        java.util.List<String> names = new java.util.ArrayList<>();
        shops.forEach(shop -> names.add(shop.get("name").asText()));

        assertThat(names).contains("Test Petshop Moema");
        assertThat(names).doesNotContain("Petly (Plataforma)");
    }

    @Test
    void platformAdminCreatesPetshopAndItsAdminCanLogIn() throws Exception {
        String token = login(PLATFORM, PLATFORM_PASSWORD);
        String adminEmail = "novo.petshop@petly.com.br";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("petshopName", "Petly Teste Plataforma");
        body.put("phone", "(11) 3000-0000");
        body.put("email", "contato@petlyteste.com.br");
        body.put("address", "Rua Teste, 10 - São Paulo - SP");
        body.put("adminName", "Responsável Teste");
        body.put("adminEmail", adminEmail);
        body.put("adminPassword", "senha12345");

        mockMvc.perform(post("/api/platform/petshops")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // o admin recém-criado consegue logar e enxergar o próprio petshop
        String newToken = login(adminEmail, "senha12345");
        String me = mockMvc.perform(get("/api/auth/me").header(AUTHORIZATION, "Bearer " + newToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(me).get("petshop").get("name").asText())
                .isEqualTo("Petly Teste Plataforma");
    }

    @Test
    void platformAdminCreatesUserInAnExistingPetshop() throws Exception {
        String token = login(PLATFORM, PLATFORM_PASSWORD);
        long petshopId = petshopIdByName(token, "Test Petshop Moema");
        String email = "suporte.extra@petly.com.br";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Suporte Extra");
        body.put("email", email);
        body.put("password", "senha12345");
        body.put("role", "STAFF");

        mockMvc.perform(post("/api/platform/petshops/" + petshopId + "/users")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        login(email, "senha12345");
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

    private long petshopIdByName(String token, String name) throws Exception {
        String response = mockMvc.perform(get("/api/platform/petshops").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode shop : objectMapper.readTree(response)) {
            if (name.equals(shop.get("name").asText())) {
                return shop.get("id").asLong();
            }
        }
        throw new AssertionError("Petshop não encontrado: " + name);
    }
}
