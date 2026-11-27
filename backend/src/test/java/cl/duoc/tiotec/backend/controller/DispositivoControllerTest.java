package cl.duoc.tiotec.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.tiotec.backend.repository.DispositivoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test de integración: levanta Spring completo con H2 en memoria y llama a los endpoints
 * como lo haría la app, pero sin red real.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DispositivoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DispositivoRepository repository;

    private static final String JSON_PIXEL =
            "{\"id\":\"d1\",\"nombre\":\"Pixel 7\",\"tipo\":\"SMARTPHONE\",\"sistemaOperativo\":\"Android 14\"}";

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    @Test
    void crudCompleto() throws Exception {
        // CREATE
        mockMvc.perform(post("/api/v1/dispositivos").contentType(MediaType.APPLICATION_JSON).content(JSON_PIXEL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("d1"));

        // READ
        mockMvc.perform(get("/api/v1/dispositivos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // UPDATE
        mockMvc.perform(put("/api/v1/dispositivos/d1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Pixel 8\",\"tipo\":\"SMARTPHONE\",\"sistemaOperativo\":\"Android 15\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pixel 8"));

        // DELETE
        mockMvc.perform(delete("/api/v1/dispositivos/d1")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/dispositivos/d1")).andExpect(status().isNotFound());
    }

    @Test
    void crear_nombreCorto_responde400() throws Exception {
        mockMvc.perform(post("/api/v1/dispositivos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"PC\",\"tipo\":\"PC_ESCRITORIO\",\"sistemaOperativo\":\"Windows 11\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nombre").exists());
    }

    @Test
    void eliminar_inexistente_responde404() throws Exception {
        mockMvc.perform(delete("/api/v1/dispositivos/no-existe")).andExpect(status().isNotFound());
    }

    @Test
    void diagnosticos_guardarYListar() throws Exception {
        String json = "{\"id\":\"g1\",\"fechaRegistro\":\"27-11-2026 18:00\",\"timestampMs\":1,"
                + "\"nivelBateria\":80,\"estaCargando\":false,\"temperaturaBateria\":30.5,"
                + "\"voltajeBateria\":4000,\"saludBateria\":\"Buena\",\"fuenteEnergia\":\"Batería\","
                + "\"almacenamientoTotalGb\":64.0,\"almacenamientoLibreGb\":20.0,"
                + "\"porcentajeAlmacenamientoUsado\":68,\"ramTotalMb\":4096,\"ramDisponibleMb\":1024,"
                + "\"totalSensores\":20,\"consumoSensoresMa\":12.5,\"nivelRiesgo\":\"BAJO\","
                + "\"recomendacion\":\"Todo bien\"}";
        mockMvc.perform(post("/api/v1/diagnosticos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sincronizadoRemoto").value(true));
        mockMvc.perform(get("/api/v1/diagnosticos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("g1"));
    }
}
