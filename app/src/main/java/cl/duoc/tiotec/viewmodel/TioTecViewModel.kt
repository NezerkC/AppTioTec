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
import cl.duoc.tiotec.validation.ValidadorFormularios
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
    val mensajeExito: String? = null,

    // Formulario de perfil (RF-01): lo que el usuario escribe y el error de cada campo
    val nombrePerfilInput: String = "",
    val errorNombrePerfil: String? = null,
    val nivelExperienciaSeleccionado: NivelExperiencia = NivelExperiencia.BASICO,

    // Formulario de dispositivo (RF-02)
    val nombreDispositivoInput: String = "",
    val errorNombreDispositivo: String? = null,
    val tipoDispositivoSeleccionado: TipoDispositivo = TipoDispositivo.SMARTPHONE,
    val sistemaOperativoInput: String = "",
    val errorSistemaOperativo: String? = null
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

    // ---------------------------------------------------------------------
    // Formulario de perfil (RF-01)
    // ---------------------------------------------------------------------

    /** Se llama con cada letra que escribe el usuario: validación en tiempo real. */
    fun onNombrePerfilChange(texto: String) {
        _uiState.update {
            it.copy(
                nombrePerfilInput = texto,
                errorNombrePerfil = ValidadorFormularios.validarNombrePerfil(texto)
            )
        }
    }

    fun onNivelExperienciaChange(nivel: NivelExperiencia) {
        _uiState.update { it.copy(nivelExperienciaSeleccionado = nivel) }
    }

    fun guardarPerfil() {
        val state = _uiState.value
        val error = ValidadorFormularios.validarNombrePerfil(state.nombrePerfilInput)
        if (error != null) {
            _uiState.update { it.copy(errorNombrePerfil = error, mensajeExito = null) }
            return
        }
        _uiState.update {
            it.copy(
                usuario = it.usuario.copy(
                    nombre = it.nombrePerfilInput.trim(),
                    nivelExperiencia = it.nivelExperienciaSeleccionado
                ),
                nombrePerfilInput = "",
                errorNombrePerfil = null,
                mensajeExito = "Perfil actualizado correctamente"
            )
        }
    }

    // ---------------------------------------------------------------------
    // Formulario de dispositivo (RF-02)
    // ---------------------------------------------------------------------

    fun onNombreDispositivoChange(texto: String) {
        _uiState.update {
            it.copy(
                nombreDispositivoInput = texto,
                errorNombreDispositivo = ValidadorFormularios.validarNombreDispositivo(texto)
            )
        }
    }

    fun onTipoDispositivoChange(tipo: TipoDispositivo) {
        _uiState.update { it.copy(tipoDispositivoSeleccionado = tipo) }
    }

    fun onSistemaOperativoChange(texto: String) {
        _uiState.update {
            it.copy(
                sistemaOperativoInput = texto,
                errorSistemaOperativo = ValidadorFormularios.validarSistemaOperativo(texto)
            )
        }
    }

    /** Valida los dos campos a la vez para mostrar todos los errores juntos. */
    fun registrarDispositivo() {
        val state = _uiState.value
        val errorNombre = ValidadorFormularios.validarNombreDispositivo(state.nombreDispositivoInput)
        val errorSo = ValidadorFormularios.validarSistemaOperativo(state.sistemaOperativoInput)

        if (errorNombre != null || errorSo != null) {
            _uiState.update {
                it.copy(
                    errorNombreDispositivo = errorNombre,
                    errorSistemaOperativo = errorSo,
                    mensajeExito = null
                )
            }
            return
        }

        agregarDispositivo(
            nombre = state.nombreDispositivoInput,
            tipo = state.tipoDispositivoSeleccionado,
            sistemaOperativo = state.sistemaOperativoInput
        )
        _uiState.update {
            it.copy(
                nombreDispositivoInput = "",
                sistemaOperativoInput = "",
                errorNombreDispositivo = null,
                errorSistemaOperativo = null
            )
        }
    }

    private fun agregarDispositivo(nombre: String, tipo: TipoDispositivo, sistemaOperativo: String, fotoUri: String? = null) {
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
