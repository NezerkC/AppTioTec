package cl.duoc.tiotec.backend.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Convierte los errores en respuestas JSON claras para la app.
 * Sin esta clase, Spring respondería un error genérico.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> noEncontrado(RecursoNoEncontradoException e) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", e.getMessage());
        return cuerpo;
    }

    /** Se activa cuando falla una validación de @Valid (por ejemplo, nombre con menos de 3 letras). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> datosInvalidos(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> campos.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", "Datos inválidos");
        cuerpo.put("campos", campos);
        return cuerpo;
    }

    /** JSON mal escrito o con tipos incorrectos. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> jsonInvalido(HttpMessageNotReadableException e) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", "El cuerpo de la petición no es un JSON válido");
        return cuerpo;
    }
}
