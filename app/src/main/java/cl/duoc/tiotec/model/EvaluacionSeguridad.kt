package cl.duoc.tiotec.model

enum class CategoriaSeguridad(val titulo: String, val descripcion: String) {
    RESPALDOS("Copias de Seguridad", "Respaldos periódicos en la nube o disco externo"),
    ACTUALIZACIONES("Actualizaciones de Software", "Parches de seguridad y sistema operativo"),
    AUTENTICACION("Autenticación y Contraseñas", "Doble factor (2FA) y gestión de claves"),
    RECUPERACION("Recuperación de Cuentas", "Canales de contacto y métodos de rescate")
}

data class OpcionRespuesta(
    val texto: String,
    val puntaje: Int,
    val recomendacionSiFalla: String? = null
)

data class PreguntaEvaluacion(
    val id: Int,
    val categoria: CategoriaSeguridad,
    val enunciado: String,
    val opciones: List<OpcionRespuesta>
)

data class EvaluacionSeguridad(
    val id: String,
    val fecha: String,
    val puntajeTotal: Int,
    val puntajeMaximo: Int,
    val nivelRiesgo: NivelRiesgo,
    val respuestas: Map<Int, Int> = emptyMap()
) {
    val porcentajeCumplimiento: Double
        get() = if (puntajeMaximo > 0) (puntajeTotal.toDouble() / puntajeMaximo.toDouble()) * 100.0 else 0.0
}
