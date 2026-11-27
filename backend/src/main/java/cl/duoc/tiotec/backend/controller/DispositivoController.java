package cl.duoc.tiotec.backend.controller;

import cl.duoc.tiotec.backend.model.Dispositivo;
import cl.duoc.tiotec.backend.service.DispositivoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST de dispositivos. Todas las rutas parten con /api/v1/dispositivos.
 *
 * GET    /api/v1/dispositivos        -> lista todos            (200)
 * GET    /api/v1/dispositivos/{id}   -> uno por id             (200 o 404)
 * POST   /api/v1/dispositivos        -> crea                   (201 o 400)
 * PUT    /api/v1/dispositivos/{id}   -> actualiza              (200, 400 o 404)
 * DELETE /api/v1/dispositivos/{id}   -> elimina                (204 o 404)
 */
@RestController
@RequestMapping("/api/v1/dispositivos")
public class DispositivoController {

    private final DispositivoService service;

    public DispositivoController(DispositivoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Dispositivo> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Dispositivo buscar(@PathVariable String id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Dispositivo crear(@Valid @RequestBody Dispositivo dispositivo) {
        return service.crear(dispositivo);
    }

    @PutMapping("/{id}")
    public Dispositivo actualizar(@PathVariable String id, @Valid @RequestBody Dispositivo dispositivo) {
        return service.actualizar(id, dispositivo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}
