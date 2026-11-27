package cl.duoc.tiotec.backend.controller;

import cl.duoc.tiotec.backend.model.Diagnostico;
import cl.duoc.tiotec.backend.service.DiagnosticoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de diagnósticos de hardware (los usa la pestaña "Spring Boot API" de Cuidado Tech).
 *
 * GET  /api/v1/diagnosticos -> lista (200)
 * POST /api/v1/diagnosticos -> guarda (201 o 400)
 */
@RestController
@RequestMapping("/api/v1/diagnosticos")
public class DiagnosticoController {

    private final DiagnosticoService service;

    public DiagnosticoController(DiagnosticoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Diagnostico> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Diagnostico guardar(@Valid @RequestBody Diagnostico diagnostico) {
        return service.guardar(diagnostico);
    }
}
