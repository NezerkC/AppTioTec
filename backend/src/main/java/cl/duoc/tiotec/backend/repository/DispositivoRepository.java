package cl.duoc.tiotec.backend.repository;

import cl.duoc.tiotec.backend.model.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla "dispositivos".
 * JpaRepository ya trae findAll, findById, save, existsById y deleteById;
 * Spring genera el SQL por nosotros.
 */
public interface DispositivoRepository extends JpaRepository<Dispositivo, String> {
}
