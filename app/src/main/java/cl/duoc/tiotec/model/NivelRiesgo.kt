package cl.duoc.tiotec.model

enum class NivelRiesgo(val etiqueta: String, val descripcion: String) {
    BAJO(
        etiqueta = "Riesgo Bajo",
        descripcion = "Tus hábitos preventivos son sólidos. Mantén tus revisiones periódicas."
    ),
    MEDIO(
        etiqueta = "Riesgo Medio",
        descripcion = "Tienes medidas básicas, pero existen brechas importantes por corregir."
    ),
    ALTO(
        etiqueta = "Riesgo Alto",
        descripcion = "Brechas críticas detectadas. Se requiere aplicar acciones preventivas urgentes."
    )
}
