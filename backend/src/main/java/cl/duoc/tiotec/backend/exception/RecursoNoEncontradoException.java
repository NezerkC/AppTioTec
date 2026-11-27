package cl.duoc.tiotec.backend.exception;

/** Se lanza cuando se pide un id que no existe. El manejador la convierte en HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
