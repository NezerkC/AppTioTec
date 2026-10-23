package cl.duoc.tiotec.validation

/**
 * Reglas de validación de los formularios (RN-03).
 *
 * Cada función recibe el texto de UN campo y devuelve:
 *  - el mensaje de error, si el valor no cumple la regla, o
 *  - null, si el valor es válido.
 *
 */
object ValidadorFormularios {

    const val MIN_CARACTERES_NOMBRE = 3

    fun validarNombrePerfil(nombre: String): String? {
        if (nombre.trim().length < MIN_CARACTERES_NOMBRE) {
            return "El nombre debe tener al menos $MIN_CARACTERES_NOMBRE caracteres"
        }
        return null
    }

    fun validarNombreDispositivo(nombre: String): String? {
        if (nombre.trim().length < MIN_CARACTERES_NOMBRE) {
            return "El nombre del dispositivo debe tener al menos $MIN_CARACTERES_NOMBRE caracteres"
        }
        return null
    }

    fun validarSistemaOperativo(so: String): String? {
        if (so.isBlank()) {
            return "Este campo no puede estar vacío"
        }
        return null
    }
}
