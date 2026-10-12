package cl.duoc.tiotec.model

enum class NivelExperiencia(val etiqueta: String) {
    BASICO("Principiante / No especialista"),
    INTERMEDIO("Usuario cotidiano"),
    AVANZADO("Conocimientos técnicos previos")
}

data class Usuario(
    val id: String = "usr_001",
    val nombre: String = "Usuario Demo",
    val nivelExperiencia: NivelExperiencia = NivelExperiencia.BASICO
)
