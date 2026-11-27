package cl.duoc.tiotec.backend.service;

import cl.duoc.tiotec.backend.exception.RecursoNoEncontradoException;
import cl.duoc.tiotec.backend.model.Dispositivo;
import cl.duoc.tiotec.backend.repository.DispositivoRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Lógica de negocio de los dispositivos.
 * El controlador recibe la petición HTTP y delega aquí; esta clase usa el repositorio.
 */
@Service
public class DispositivoService {

    private final DispositivoRepository repository;

    public DispositivoService(DispositivoRepository repository) {
        this.repository = repository;
    }

    public List<Dispositivo> listar() {
        return repository.findAll();
    }

    public Dispositivo buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el dispositivo con id " + id));
    }

    /**
     * Crea un dispositivo. Si la app ya trae un id (el mismo que usa en Room) se respeta;
     * si no trae, se genera uno.
     */
    public Dispositivo crear(Dispositivo dispositivo) {
        if (dispositivo.getId() == null || dispositivo.getId().isBlank()) {
            dispositivo.setId(UUID.randomUUID().toString());
        }
        limpiarTextos(dispositivo);
        return repository.save(dispositivo);
    }

    public Dispositivo actualizar(String id, Dispositivo datos) {
        Dispositivo existente = buscarPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setTipo(datos.getTipo());
        existente.setSistemaOperativo(datos.getSistemaOperativo());
        limpiarTextos(existente);
        return repository.save(existente);
    }

    public void eliminar(String id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el dispositivo con id " + id);
        }
        repository.deleteById(id);
    }

    private void limpiarTextos(Dispositivo d) {
        d.setNombre(d.getNombre().trim());
        d.setTipo(d.getTipo().trim().toUpperCase());
        d.setSistemaOperativo(d.getSistemaOperativo().trim());
    }
}
