package cl.duoc.tiotec.model

enum class EstadoAsistencia(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    ATENDIDA("Atendida")
}

data class SolicitudAsistencia(
    val id: String,
    val categoria: CategoriaSeguridad,
    val descripcion: String,
    val estado: EstadoAsistencia = EstadoAsistencia.PENDIENTE,
    val fecha: String
)
