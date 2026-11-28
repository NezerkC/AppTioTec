package cl.duoc.tiotec.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.tiotec.backend.exception.RecursoNoEncontradoException;
import cl.duoc.tiotec.backend.model.Diagnostico;
import cl.duoc.tiotec.backend.repository.DiagnosticoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DiagnosticoServiceTest {

    private DiagnosticoRepository repository;
    private DiagnosticoService service;

    @BeforeEach
    void setUp() {
        repository = mock(DiagnosticoRepository.class);
        service = new DiagnosticoService(repository);
        when(repository.save(any(Diagnostico.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Diagnostico nuevo(String id) {
        Diagnostico d = new Diagnostico();
        d.setId(id);
        d.setFechaRegistro("27-11-2026 18:00");
        d.setTimestampMs(1000L);
        d.setNivelBateria(80);
        d.setEstaCargando(false);
        d.setTemperaturaBateria(30.0f);
        d.setVoltajeBateria(4000);
        d.setSaludBateria("Buena");
        d.setFuenteEnergia("Batería");
        d.setAlmacenamientoTotalGb(64.0);
        d.setAlmacenamientoLibreGb(20.0);
        d.setPorcentajeAlmacenamientoUsado(68);
        d.setRamTotalMb(4096L);
        d.setRamDisponibleMb(1024L);
        d.setTotalSensores(10);
        d.setConsumoSensoresMa(12.0);
        d.setNivelRiesgo("BAJO");
        d.setRecomendacion("Todo en orden");
        return d;
    }

    @Test
    void listar_devuelveLoDelRepositorioOrdenado() {
        when(repository.findAllByOrderByTimestampMsDesc()).thenReturn(List.of(nuevo("diag-1")));
        List<Diagnostico> lista = service.listar();
        assertEquals(1, lista.size());
        assertEquals("diag-1", lista.get(0).getId());
    }

    @Test
    void guardar_persisteYRetornaDiagnostico() {
        Diagnostico d = nuevo("diag-1");
        Diagnostico guardado = service.guardar(d);
        assertNotNull(guardado);
        assertEquals("diag-1", guardado.getId());
        verify(repository).save(d);
    }

    @Test
    void buscarPorId_existente_devuelveDiagnostico() {
        Diagnostico d = nuevo("diag-1");
        when(repository.findById("diag-1")).thenReturn(Optional.of(d));
        Diagnostico resultado = service.buscarPorId("diag-1");
        assertEquals("diag-1", resultado.getId());
    }

    @Test
    void buscarPorId_inexistente_lanzaNoEncontrado() {
        when(repository.findById("no-existe")).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorId("no-existe"));
    }

    @Test
    void eliminar_existente_borra() {
        when(repository.existsById("diag-1")).thenReturn(true);
        service.eliminar("diag-1");
        verify(repository).deleteById("diag-1");
    }

    @Test
    void eliminar_inexistente_noBorraYLanzaNoEncontrado() {
        when(repository.existsById("no-existe")).thenReturn(false);
        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar("no-existe"));
        verify(repository, never()).deleteById("no-existe");
    }
}
