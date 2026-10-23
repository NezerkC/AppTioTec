package cl.duoc.tiotec.ui.components

import androidx.compose.ui.graphics.Color
import cl.duoc.tiotec.model.NivelRiesgo

/**
 * Colores del semáforo de riesgo (RN-01): verde, amarillo/naranjo y rojo.
 * Se usan en el resultado del cuestionario y en el historial.
 */
data class ColoresRiesgo(val fondo: Color, val texto: Color, val acento: Color)

fun coloresDe(nivel: NivelRiesgo): ColoresRiesgo = when (nivel) {
    NivelRiesgo.BAJO -> ColoresRiesgo(Color(0xFFE8F5E9), Color(0xFF1B5E20), Color(0xFF2E7D32))
    NivelRiesgo.MEDIO -> ColoresRiesgo(Color(0xFFFFF8E1), Color(0xFFE65100), Color(0xFFF57C00))
    NivelRiesgo.ALTO -> ColoresRiesgo(Color(0xFFFFEBEE), Color(0xFFB71C1C), Color(0xFFD32F2F))
}
