package cl.duoc.tiotec.backend.repository;

import cl.duoc.tiotec.backend.model.Diagnostico;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticoRepository extends JpaRepository<Diagnostico, String> {

    /** Spring arma la consulta a partir del nombre del método: ORDER BY timestamp_ms DESC. */
    List<Diagnostico> findAllByOrderByTimestampMsDesc();
}
