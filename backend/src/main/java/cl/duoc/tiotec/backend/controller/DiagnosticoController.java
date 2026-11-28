package cl.duoc.tiotec.backend.controller;

import cl.duoc.tiotec.backend.model.Diagnostico;
import cl.duoc.tiotec.backend.service.DiagnosticoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de diagnósticos de hardware (los usa la pestaña "Spring Boot API" de Cuidado Tech).
 *
 * GET  /api/v1/diagnosticos      -> lista (200)
 * GET  /api/v1/diagnosticos/{id} -> detalle por id (200 o 404)
 * POST /api/v1/diagnosticos      -> guarda (201 o 400)
 * DELETE /api/v1/diagnosticos/{id} -> elimina (204 o 404)
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

    @GetMapping("/{id}")
    public Diagnostico buscar(@PathVariable String id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Diagnostico guardar(@Valid @RequestBody Diagnostico diagnostico) {
        return service.guardar(diagnostico);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}
