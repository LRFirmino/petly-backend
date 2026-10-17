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

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Opção B: combos são agendáveis como serviços (duração + preço por porte), os
 * agendamentos referenciam o item de catálogo e relatórios/popularidade passam
 * a distinguir serviços de combos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComboSchedulingTest {

    private static final String ADMIN = "marina@petly.com.br";
    private static final String PASSWORD = "password123";
    private static final String STAFF = "Beatriz Rocha";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void schedulesComboDerivingDurationAndPriceFromPetSize() throws Exception {
        String token = token();
        JsonNode combo = comboByName(token, "Combo Completo"); // preços 100/130/170 · 90 min
        JsonNode pet = petByName(token, "Amora");              // porte MEDIUM

        JsonNode created = createComboAppointment(token, combo, pet, "2027-09-09T08:15:00", null);

        assertThat(created.get("itemType").asText()).isEqualTo("COMBO");
        assertThat(created.get("itemId").asLong()).isEqualTo(combo.get("id").asLong());
        assertThat(created.get("service").asText()).isEqualTo("Combo Completo");
        assertThat(created.get("durationMinutes").asInt())
                .isEqualTo(combo.get("durationMinutes").asInt());
        assertThat(created.get("price").decimalValue())
                .isEqualByComparingTo(combo.get("prices").get(1).decimalValue());
    }

    @Test
    void explicitPriceOverridesCatalogPrice() throws Exception {
        String token = token();
        JsonNode combo = comboByName(token, "Combo Completo");
        JsonNode pet = petByName(token, "Amora");

        JsonNode created = createComboAppointment(token, combo, pet, "2027-09-09T11:15:00", new BigDecimal("99.50"));

        assertThat(created.get("price").decimalValue()).isEqualByComparingTo(new BigDecimal("99.50"));
    }

    @Test
    void comboPopularityCountsRealAppointments() throws Exception {
        String token = token();
        long before = comboByName(token, "Combo Completo").get("popularity").asLong();
        JsonNode combo = comboByName(token, "Combo Completo");
        JsonNode pet = petByName(token, "Amora");

        createComboAppointment(token, combo, pet, "2027-09-10T08:15:00", null);

        long after = comboByName(token, "Combo Completo").get("popularity").asLong();
        assertThat(after).isEqualTo(before + 1);
    }

    @Test
    void reportsSeparateServicesFromCombos() throws Exception {
        String token = token();
        JsonNode summary = getJson("/api/reports/summary", token);

        assertThat(names(summary.get("combos"))).contains("Combo Completo");
        assertThat(names(summary.get("services"))).doesNotContain("Combo Completo");
        assertThat(names(summary.get("services"))).contains("Tosa higiênica");
    }

    @Test
    void legacyAppointmentWithoutCatalogItemStillWorks() throws Exception {
        String token = token();
        JsonNode pet = petByName(token, "Amora");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clientId", pet.get("clientId").asLong());
        body.put("petId", pet.get("id").asLong());
        body.put("service", "Corte de unhas");
        body.put("scheduledAt", "2027-09-11T08:15:00");
        body.put("status", "SCHEDULED");
        body.put("staffName", STAFF);
        body.put("durationMinutes", 15);
        body.put("price", 20);

        JsonNode created = json(mockMvc.perform(post("/api/appointments")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated()));

        assertThat(created.get("service").asText()).isEqualTo("Corte de unhas");
        assertThat(created.get("itemType").isNull()).isTrue();
    }

    @Test
    void unknownComboIsRejected() throws Exception {
        String token = token();
        JsonNode pet = petByName(token, "Amora");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clientId", pet.get("clientId").asLong());
        body.put("petId", pet.get("id").asLong());
        body.put("itemType", "COMBO");
        body.put("itemId", 999999);
        body.put("scheduledAt", "2027-09-12T08:15:00");
        body.put("status", "SCHEDULED");
        body.put("staffName", STAFF);

        mockMvc.perform(post("/api/appointments")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    // ---- helpers ------------------------------------------------------

    private String token() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", ADMIN, "password", PASSWORD));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private JsonNode comboByName(String token, String name) throws Exception {
        JsonNode combos = getJson("/api/combos", token);
        for (JsonNode combo : combos) {
            if (name.equals(combo.get("name").asText())) {
                return combo;
            }
        }
        throw new AssertionError("Combo não encontrado: " + name);
    }

    private JsonNode petByName(String token, String name) throws Exception {
        JsonNode pets = getJson("/api/pets", token);
        for (JsonNode pet : pets) {
            if (name.equals(pet.get("name").asText())) {
                return pet;
            }
        }
        throw new AssertionError("Pet não encontrado: " + name);
    }

    private JsonNode createComboAppointment(String token, JsonNode combo, JsonNode pet,
                                            String scheduledAt, BigDecimal price) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clientId", pet.get("clientId").asLong());
        body.put("petId", pet.get("id").asLong());
        body.put("itemType", "COMBO");
        body.put("itemId", combo.get("id").asLong());
        body.put("scheduledAt", scheduledAt);
        body.put("status", "SCHEDULED");
        body.put("staffName", STAFF);
        if (price != null) {
            body.put("price", price);
        }
        return json(mockMvc.perform(post("/api/appointments")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated()));
    }

    private JsonNode getJson(String path, String token) throws Exception {
        return json(mockMvc.perform(get(path).header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk()));
    }

    private JsonNode json(org.springframework.test.web.servlet.ResultActions actions) throws Exception {
        return objectMapper.readTree(actions.andReturn().getResponse()
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static java.util.List<String> names(JsonNode array) {
        java.util.List<String> result = new java.util.ArrayList<>();
        array.forEach(node -> result.add(node.get("name").asText()));
        return result;
    }
}
