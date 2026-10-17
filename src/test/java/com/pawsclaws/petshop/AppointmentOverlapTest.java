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
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regra de negócio: um profissional não pode ter dois atendimentos com
 * horários sobrepostos no mesmo petshop.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentOverlapTest {

    private static final String ADMIN = "marina@petly.com.br";
    private static final String PASSWORD = "password123";
    private static final String ANA = "Ana Martins";
    private static final String LUCAS = "Lucas Silva";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void overlappingSlotForSameProfessionalIsRejected() throws Exception {
        String token = token();
        long[] ids = firstClientAndPet(token);
        String base = "2027-01-15T10:00:00";

        // 10:00–11:00
        create(token, ids, base, ANA).andExpect(status().isCreated());

        // começa dentro do intervalo → 409
        create(token, ids, "2027-01-15T10:30:00", ANA).andExpect(status().isConflict());
        // termina dentro do intervalo → 409
        create(token, ids, "2027-01-15T09:30:00", ANA).andExpect(status().isConflict());
        // engloba todo o intervalo (09:00 às 12:00) → 409
        create(token, ids, "2027-01-15T09:00:00", ANA, 180).andExpect(status().isConflict());

        // encostado (adjacente) é permitido → 201
        create(token, ids, "2027-01-15T11:00:00", ANA).andExpect(status().isCreated());
    }

    @Test
    void adjacentSlotBeforeIsAllowed() throws Exception {
        String token = token();
        long[] ids = firstClientAndPet(token);

        create(token, ids, "2027-01-16T10:00:00", ANA).andExpect(status().isCreated());
        // 09:00–10:00 encosta em 10:00–11:00 → permitido
        create(token, ids, "2027-01-16T09:00:00", ANA).andExpect(status().isCreated());
    }

    @Test
    void sameTimeForAnotherProfessionalIsAllowed() throws Exception {
        String token = token();
        long[] ids = firstClientAndPet(token);

        create(token, ids, "2027-02-10T10:00:00", ANA).andExpect(status().isCreated());
        create(token, ids, "2027-02-10T10:00:00", LUCAS).andExpect(status().isCreated());
    }

    @Test
    void movingAnAppointmentOntoAnotherIsRejected() throws Exception {
        String token = token();
        long[] ids = firstClientAndPet(token);

        create(token, ids, "2027-03-10T10:00:00", ANA).andExpect(status().isCreated());
        long movingId = idOf(create(token, ids, "2027-03-10T14:00:00", ANA).andExpect(status().isCreated()));

        // mover para 10:30 colidiria com o atendimento das 10:00
        mockMvc.perform(put("/api/appointments/" + movingId)
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(ids, "2027-03-10T10:30:00", ANA)))
                .andExpect(status().isConflict());

        // manter o próprio horário não gera conflito consigo mesmo
        mockMvc.perform(put("/api/appointments/" + movingId)
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(ids, "2027-03-10T14:00:00", ANA)))
                .andExpect(status().isOk());
    }

    @Test
    void cancelledAppointmentDoesNotBlockTheSlot() throws Exception {
        String token = token();
        long[] ids = firstClientAndPet(token);

        long id = idOf(create(token, ids, "2027-04-10T10:00:00", ANA).andExpect(status().isCreated()));

        mockMvc.perform(put("/api/appointments/" + id)
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(ids, "2027-04-10T10:00:00", ANA, "CANCELLED")))
                .andExpect(status().isOk());

        // o horário liberado pode ser reagendado
        create(token, ids, "2027-04-10T10:00:00", ANA).andExpect(status().isCreated());
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

    private long[] firstClientAndPet(String token) throws Exception {
        String response = mockMvc.perform(get("/api/pets").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode pet = objectMapper.readTree(response).get(0);
        return new long[]{pet.get("clientId").asLong(), pet.get("id").asLong()};
    }

    private ResultActions create(String token, long[] ids, String scheduledAt, String staff) throws Exception {
        return create(token, ids, scheduledAt, staff, 60);
    }

    private ResultActions create(String token, long[] ids, String scheduledAt, String staff, int duration) throws Exception {
        return mockMvc.perform(post("/api/appointments")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(ids, scheduledAt, staff, "SCHEDULED", duration)));
    }

    private long idOf(ResultActions actions) throws Exception {
        String response = actions.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String payload(long[] ids, String scheduledAt, String staff) throws Exception {
        return payload(ids, scheduledAt, staff, "SCHEDULED", 60);
    }

    private String payload(long[] ids, String scheduledAt, String staff, String status) throws Exception {
        return payload(ids, scheduledAt, staff, status, 60);
    }

    private String payload(long[] ids, String scheduledAt, String staff, String status, int duration) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("clientId", ids[0]);
        body.put("petId", ids[1]);
        body.put("service", "Banho Essencial");
        body.put("scheduledAt", scheduledAt);
        body.put("status", status);
        body.put("staffName", staff);
        body.put("durationMinutes", duration);
        body.put("price", 55);
        return objectMapper.writeValueAsString(body);
    }
}
