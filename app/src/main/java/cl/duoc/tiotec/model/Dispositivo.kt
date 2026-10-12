package cl.duoc.tiotec.model

enum class TipoDispositivo(val etiqueta: String) {
    SMARTPHONE("Teléfono inteligente"),
    TABLET("Tablet"),
    NOTEBOOK("Notebook / Laptop"),
    PC_ESCRITORIO("PC de escritorio")
}

data class Dispositivo(
    val id: String,
    val nombre: String,
    val tipo: TipoDispositivo,
    val sistemaOperativo: String,
    val fotoUri: String? = null
)
