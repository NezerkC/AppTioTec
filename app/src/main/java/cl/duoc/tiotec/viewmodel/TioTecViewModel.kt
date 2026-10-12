package cl.duoc.tiotec.viewmodel

import androidx.lifecycle.ViewModel
import cl.duoc.tiotec.model.AccionChecklist
import cl.duoc.tiotec.model.Dispositivo
import cl.duoc.tiotec.model.EvaluacionSeguridad
import cl.duoc.tiotec.model.NivelExperiencia
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.PreguntaEvaluacion
import cl.duoc.tiotec.model.PrioridadAccion
import cl.duoc.tiotec.model.TipoDispositivo
import cl.duoc.tiotec.model.Usuario
import cl.duoc.tiotec.repository.TioTecRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TioTecUiState(
    val usuario: Usuario = Usuario(),
    val dispositivos: List<Dispositivo> = emptyList(),
    val preguntas: List<PreguntaEvaluacion> = emptyList(),
    val respuestasActuales: Map<Int, Int> = emptyMap(),
    val checklist: List<AccionChecklist> = emptyList(),
    val historial: List<EvaluacionSeguridad> = emptyList(),
    val ultimaEvaluacion: EvaluacionSeguridad? = null,
    val errorFormulario: String? = null,
    val mensajeExito: String? = null
)

class TioTecViewModel(
    private val repository: TioTecRepository = TioTecRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TioTecUiState(
            usuario = repository.obtenerPerfilInicial(),
            dispositivos = repository.obtenerDispositivosIniciales(),
            preguntas = repository.obtenerPreguntasEvaluacion(),
            checklist = repository.obtenerChecklistInicial(),
            historial = repository.obtenerHistorialInicial(),
            ultimaEvaluacion = repository.obtenerHistorialInicial().firstOrNull()
        )
    )
    val uiState: StateFlow<TioTecUiState> = _uiState.asStateFlow()

    fun actualizarPerfil(nombre: String, nivel: NivelExperiencia) {
        if (nombre.trim().length < 3) {
            _uiState.update { it.copy(errorFormulario = "El nombre debe tener al menos 3 caracteres", mensajeExito = null) }
            return
        }
        _uiState.update {
            it.copy(
                usuario = it.usuario.copy(nombre = nombre.trim(), nivelExperiencia = nivel),
                errorFormulario = null,
                mensajeExito = "Perfil actualizado correctamente"
            )
        }
    }

    fun agregarDispositivo(nombre: String, tipo: TipoDispositivo, sistemaOperativo: String, fotoUri: String? = null) {
        if (nombre.trim().length < 3 || sistemaOperativo.trim().isEmpty()) {
            _uiState.update { it.copy(errorFormulario = "Completa un nombre válido (mín. 3 letras) y el sistema operativo", mensajeExito = null) }
            return
        }
        val nuevo = Dispositivo(
            id = "disp_${System.currentTimeMillis()}",
            nombre = nombre.trim(),
            tipo = tipo,
            sistemaOperativo = sistemaOperativo.trim(),
            fotoUri = fotoUri
        )
        _uiState.update {
            it.copy(
                dispositivos = it.dispositivos + nuevo,
                errorFormulario = null,
                mensajeExito = "Dispositivo '${nuevo.nombre}' registrado"
            )
        }
    }

    fun seleccionarRespuesta(preguntaId: Int, puntaje: Int) {
        _uiState.update { state ->
            state.copy(
                respuestasActuales = state.respuestasActuales + (preguntaId to puntaje),
                errorFormulario = null
            )
        }
    }

    fun finalizarEvaluacion(): Boolean {
        val state = _uiState.value
        if (state.respuestasActuales.size < state.preguntas.size) {
            _uiState.update { it.copy(errorFormulario = "Debes responder las ${state.preguntas.size} preguntas antes de finalizar") }
            return false
        }

        val puntajeTotal = state.respuestasActuales.values.sum()
        val puntajeMaximo = state.preguntas.size * 2
        val porcentaje = (puntajeTotal.toDouble() / puntajeMaximo.toDouble()) * 100.0

        // RN-01: Semáforo de Riesgo
        val nivelRiesgo = when {
            porcentaje >= 80.0 -> NivelRiesgo.BAJO
            porcentaje >= 50.0 -> NivelRiesgo.MEDIO
            else -> NivelRiesgo.ALTO
        }

        val nuevaEval = EvaluacionSeguridad(
            id = "eval_${System.currentTimeMillis()}",
            fecha = "Hoy",
            puntajeTotal = puntajeTotal,
            puntajeMaximo = puntajeMaximo,
            nivelRiesgo = nivelRiesgo,
            respuestas = state.respuestasActuales
        )

        // RN-02: Generar acciones automáticas para brechas detectadas
        val nuevasAcciones = mutableListOf<AccionChecklist>()
        state.preguntas.forEach { pregunta ->
            val puntajeObtenido = state.respuestasActuales[pregunta.id] ?: 0
            val opcionElegida = pregunta.opciones.find { it.puntaje == puntajeObtenido }
            if (puntajeObtenido < 2 && opcionElegida?.recomendacionSiFalla != null) {
                nuevasAcciones.add(
                    AccionChecklist(
                        id = "chk_${pregunta.id}_${System.currentTimeMillis()}",
                        titulo = opcionElegida.recomendacionSiFalla,
                        descripcion = "Generado tras diagnóstico en ${pregunta.categoria.titulo}",
                        categoria = pregunta.categoria,
                        prioridad = if (puntajeObtenido == 0) PrioridadAccion.ALTA else PrioridadAccion.MEDIA,
                        completada = false,
                        fechaSugerida = if (puntajeObtenido == 0) "Urgente (24h)" else "Esta semana"
                    )
                )
            }
        }

        _uiState.update {
            it.copy(
                ultimaEvaluacion = nuevaEval,
                historial = listOf(nuevaEval) + it.historial,
                checklist = (nuevasAcciones + it.checklist).distinctBy { acc -> acc.titulo },
                respuestasActuales = emptyMap(),
                errorFormulario = null,
                mensajeExito = "Evaluación completada: ${nivelRiesgo.etiqueta}"
            )
        }
        return true
    }

    fun alternarEstadoAccion(accionId: String) {
        _uiState.update { state ->
            state.copy(
                checklist = state.checklist.map { accion ->
                    if (accion.id == accionId) accion.copy(completada = !accion.completada) else accion
                }
            )
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(errorFormulario = null, mensajeExito = null) }
    }
}
