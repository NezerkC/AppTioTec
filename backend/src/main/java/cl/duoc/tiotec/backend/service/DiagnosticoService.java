package cl.duoc.tiotec.backend.service;

import cl.duoc.tiotec.backend.model.Diagnostico;
import cl.duoc.tiotec.backend.repository.DiagnosticoRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticoService {

    private final DiagnosticoRepository repository;

    public DiagnosticoService(DiagnosticoRepository repository) {
        this.repository = repository;
    }

    /** Los más recientes primero. */
    public List<Diagnostico> listar() {
        return repository.findAllByOrderByTimestampMsDesc();
    }

    /** Si el mismo diagnóstico se sube dos veces, se sobrescribe (mismo id) y no se duplica. */
    public Diagnostico guardar(Diagnostico diagnostico) {
        return repository.save(diagnostico);
    }
}
