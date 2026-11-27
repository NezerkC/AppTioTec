package cl.duoc.tiotec.model

import cl.duoc.tiotec.data.remote.EndOfLifeVersionDto

/** Una versión de Android con su estado de soporte, según la API externa endoflife.date. */
data class VersionAndroid(
    val version: String,
    val etiqueta: String,
    val conSoporte: Boolean,
    val sinSoporteDesde: String?
)

/** Resultado de cruzar el sistema operativo de un dispositivo con la API externa. */
sealed class EstadoSoporte(val texto: String) {
    data object ConSoporte : EstadoSoporte("Con soporte de seguridad")
    data class SinSoporte(val desde: String?) :
        EstadoSoporte(if (desde != null) "Sin soporte desde $desde" else "Sin soporte de seguridad")
    data object SinInformacion : EstadoSoporte("Sin información de soporte")
    data object NoEsAndroid : EstadoSoporte("Consulta solo disponible para Android")
}

/**
 * Lógica pura (sin red ni Android): por eso se puede probar con tests unitarios.
 */
object SoporteAndroid {

    private val patronAndroid = Regex("""android\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)

    fun desdeDto(dto: EndOfLifeVersionDto): VersionAndroid = VersionAndroid(
        version = dto.name,
        etiqueta = dto.label ?: dto.name,
        conSoporte = !dto.isEol,
        sinSoporteDesde = dto.eolFrom
    )

    /** "Android 14" -> "14", "android 12.1" -> "12.1", "Windows 11" -> null */
    fun extraerVersion(sistemaOperativo: String): String? =
        patronAndroid.find(sistemaOperativo)?.groupValues?.get(1)

    fun evaluar(sistemaOperativo: String, versiones: List<VersionAndroid>): EstadoSoporte {
        val version = extraerVersion(sistemaOperativo) ?: return EstadoSoporte.NoEsAndroid
        val mayor = version.substringBefore(".")
        val encontrada = versiones.firstOrNull { it.version == version }
            ?: versiones.firstOrNull { it.version == mayor }
            ?: return EstadoSoporte.SinInformacion
        return if (encontrada.conSoporte) EstadoSoporte.ConSoporte
        else EstadoSoporte.SinSoporte(encontrada.sinSoporteDesde)
    }
}
