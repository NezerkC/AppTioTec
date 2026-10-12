package cl.duoc.tiotec.model

enum class PrioridadAccion(val etiqueta: String) {
    ALTA("Urgente"),
    MEDIA("Recomendado"),
    BAJA("Opcional")
}

data class AccionChecklist(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: CategoriaSeguridad,
    val prioridad: PrioridadAccion,
    val completada: Boolean = false,
    val fechaSugerida: String
)
