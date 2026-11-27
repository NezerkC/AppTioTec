package cl.duoc.tiotec.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.tiotec.backend.exception.RecursoNoEncontradoException;
import cl.duoc.tiotec.backend.model.Dispositivo;
import cl.duoc.tiotec.backend.repository.DispositivoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test unitario: el repositorio es un "mock" (falso), así probamos solo la lógica del servicio
 * sin base de datos.
 */
class DispositivoServiceTest {

    private DispositivoRepository repository;
    private DispositivoService service;

    @BeforeEach
    void setUp() {
        repository = mock(DispositivoRepository.class);
        service = new DispositivoService(repository);
        // save devuelve el mismo objeto que recibe
        when(repository.save(any(Dispositivo.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Dispositivo nuevo(String id, String nombre) {
        Dispositivo d = new Dispositivo();
        d.setId(id);
        d.setNombre(nombre);
        d.setTipo("smartphone");
        d.setSistemaOperativo(" Android 14 ");
        return d;
    }

    @Test
    void crear_sinId_generaUnId() {
        Dispositivo guardado = service.crear(nuevo(null, "Pixel 7"));
        assertFalse(guardado.getId().isBlank());
    }

    @Test
    void crear_conId_respetaElIdDeLaApp() {
        Dispositivo guardado = service.crear(nuevo("abc-123", "Pixel 7"));
        assertEquals("abc-123", guardado.getId());
    }

    @Test
    void crear_limpiaEspaciosYPasaTipoAMayusculas() {
        Dispositivo guardado = service.crear(nuevo("1", "  Pixel 7  "));
        assertEquals("Pixel 7", guardado.getNombre());
        assertEquals("SMARTPHONE", guardado.getTipo());
        assertEquals("Android 14", guardado.getSistemaOperativo());
    }

    @Test
    void buscarPorId_inexistente_lanzaNoEncontrado() {
        when(repository.findById("x")).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorId("x"));
    }

    @Test
    void actualizar_cambiaLosDatosDelExistente() {
        when(repository.findById("1")).thenReturn(Optional.of(nuevo("1", "Viejo")));
        Dispositivo cambios = nuevo(null, "Nuevo nombre");
        cambios.setTipo("TABLET");

        Dispositivo resultado = service.actualizar("1", cambios);

        assertEquals("1", resultado.getId());
        assertEquals("Nuevo nombre", resultado.getNombre());
        assertEquals("TABLET", resultado.getTipo());
    }

    @Test
    void eliminar_inexistente_noBorraYLanzaNoEncontrado() {
        when(repository.existsById("x")).thenReturn(false);
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar("x"));
        verify(repository, never()).deleteById("x");
    }

    @Test
    void eliminar_existente_borra() {
        when(repository.existsById("1")).thenReturn(true);
        service.eliminar("1");
        verify(repository).deleteById("1");
    }

    @Test
    void listar_devuelveLoDelRepositorio() {
        when(repository.findAll()).thenReturn(List.of(nuevo("1", "Pixel 7")));
        assertEquals(1, service.listar().size());
    }
}
