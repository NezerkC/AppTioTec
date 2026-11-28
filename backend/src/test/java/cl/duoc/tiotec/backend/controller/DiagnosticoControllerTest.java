package cl.duoc.tiotec.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.duoc.tiotec.backend.repository.DiagnosticoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DiagnosticoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DiagnosticoRepository repository;

    private static final String JSON_VALIDO = "{\"id\":\"diag-1\",\"fechaRegistro\":\"27-11-2026 18:00\",\"timestampMs\":1000,"
            + "\"nivelBateria\":80,\"estaCargando\":false,\"temperaturaBateria\":30.5,"
            + "\"voltajeBateria\":4000,\"saludBateria\":\"Buena\",\"fuenteEnergia\":\"Batería\","
            + "\"almacenamientoTotalGb\":64.0,\"almacenamientoLibreGb\":20.0,"
            + "\"porcentajeAlmacenamientoUsado\":68,\"ramTotalMb\":4096,\"ramDisponibleMb\":1024,"
            + "\"totalSensores\":20,\"consumoSensoresMa\":12.5,\"nivelRiesgo\":\"BAJO\","
            + "\"recomendacion\":\"Todo bien\"}";

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    @Test
    void cicloCompletoDiagnostico() throws Exception {
        // 1. POST (201, sincronizadoRemoto=true)
        mockMvc.perform(post("/api/v1/diagnosticos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("diag-1"))
                .andExpect(jsonPath("$.sincronizadoRemoto").value(true));

        // 2. GET (200)
        mockMvc.perform(get("/api/v1/diagnosticos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("diag-1"));

        // 3. GET /{id} (200)
        mockMvc.perform(get("/api/v1/diagnosticos/diag-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("diag-1"));

        // 4. DELETE /{id} (204)
        mockMvc.perform(delete("/api/v1/diagnosticos/diag-1"))
                .andExpect(status().isNoContent());

        // 5. GET /{id} (404)
        mockMvc.perform(get("/api/v1/diagnosticos/diag-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void post_nivelBateriaInvalido_responde400() throws Exception {
        String jsonInvalido = "{\"id\":\"diag-bad\",\"fechaRegistro\":\"27-11-2026 18:00\",\"timestampMs\":1000,"
                + "\"nivelBateria\":150,\"estaCargando\":false,\"temperaturaBateria\":30.5,"
                + "\"voltajeBateria\":4000,\"saludBateria\":\"Buena\",\"fuenteEnergia\":\"Batería\","
                + "\"almacenamientoTotalGb\":64.0,\"almacenamientoLibreGb\":20.0,"
                + "\"porcentajeAlmacenamientoUsado\":68,\"ramTotalMb\":4096,\"ramDisponibleMb\":1024,"
                + "\"totalSensores\":20,\"consumoSensoresMa\":12.5,\"nivelRiesgo\":\"BAJO\","
                + "\"recomendacion\":\"Todo bien\"}";
        mockMvc.perform(post("/api/v1/diagnosticos").contentType(MediaType.APPLICATION_JSON).content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nivelBateria").exists());
    }

    @Test
    void post_idVacio_responde400() throws Exception {
        String jsonSinId = "{\"id\":\"\",\"fechaRegistro\":\"27-11-2026 18:00\",\"timestampMs\":1000,"
                + "\"nivelBateria\":50,\"estaCargando\":false,\"temperaturaBateria\":30.5,"
                + "\"voltajeBateria\":4000,\"saludBateria\":\"Buena\",\"fuenteEnergia\":\"Batería\","
                + "\"almacenamientoTotalGb\":64.0,\"almacenamientoLibreGb\":20.0,"
                + "\"porcentajeAlmacenamientoUsado\":68,\"ramTotalMb\":4096,\"ramDisponibleMb\":1024,"
                + "\"totalSensores\":20,\"consumoSensoresMa\":12.5,\"nivelRiesgo\":\"BAJO\","
                + "\"recomendacion\":\"Todo bien\"}";
        mockMvc.perform(post("/api/v1/diagnosticos").contentType(MediaType.APPLICATION_JSON).content(jsonSinId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.id").exists());
    }

    @Test
    void eliminar_inexistente_responde404() throws Exception {
        mockMvc.perform(delete("/api/v1/diagnosticos/no-existe")).andExpect(status().isNotFound());
    }
}
