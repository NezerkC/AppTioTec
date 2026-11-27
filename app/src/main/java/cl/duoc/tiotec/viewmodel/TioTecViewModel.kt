package cl.duoc.tiotec.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.tiotec.model.AccionChecklist
import cl.duoc.tiotec.model.Dispositivo
import cl.duoc.tiotec.model.EvaluacionSeguridad
import cl.duoc.tiotec.model.NivelExperiencia
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.PreguntaEvaluacion
import cl.duoc.tiotec.model.CategoriaSeguridad
import cl.duoc.tiotec.model.EstadoAsistencia
import cl.duoc.tiotec.model.PrioridadAccion
import cl.duoc.tiotec.model.SolicitudAsistencia
import cl.duoc.tiotec.model.TipoDispositivo
import cl.duoc.tiotec.model.Usuario
import cl.duoc.tiotec.model.VersionAndroid
import cl.duoc.tiotec.repository.PreferenciasDataStore
import cl.duoc.tiotec.repository.TioTecRepository
import cl.duoc.tiotec.validation.ValidadorFormularios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.Context
import cl.duoc.tiotec.model.DiagnosticoHardware
import cl.duoc.tiotec.model.DiagnosticoHardwareRegistro
import cl.duoc.tiotec.model.DiagnosticoSistema
import cl.duoc.tiotec.model.MetricasBienestarDigital
import cl.duoc.tiotec.model.TelemetriaSistema
import cl.duoc.tiotec.nativo.BienestarDigitalHelper
import cl.duoc.tiotec.nativo.ChequeoMantenimientoHelper
import cl.duoc.tiotec.nativo.HardwareSaludHelper
import cl.duoc.tiotec.nativo.TelemetriaSistemaHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val errorSistemaOperativo: String? = null,
    val fotoDispositivoInput: String? = null, // ruta de la foto tomada con la cámara (#9)
    val dispositivoEnEdicionId: String? = null, // null = registrando uno nuevo; con id = editando

    // Estado de la conexión con el microservicio Spring Boot (dispositivos)
    val servidorDispositivosOk: Boolean? = null, // null = aún no se intenta
    val estadoServidorDispositivos: String? = null,
    val sincronizandoDispositivos: Boolean = false,

    // API externa endoflife.date: versiones de Android y su soporte
    val versionesAndroid: List<VersionAndroid> = emptyList(),
    val cargandoSoporteAndroid: Boolean = false,
    val errorSoporteAndroid: String? = null,

    // Cuestionario (RF-03 / RF-04)
    val preguntasSinResponder: Set<Int> = emptySet(),
    val resultadoReciente: EvaluacionSeguridad? = null,
    val accionesGeneradas: List<AccionChecklist> = emptyList(),

    // Solicitudes de Asistencia Simulada (Issue #12)
    val solicitudesAsistencia: List<SolicitudAsistencia> = emptyList(),
    val categoriaAsistenciaSeleccionada: CategoriaSeguridad = CategoriaSeguridad.RESPALDOS,
    val descripcionAsistenciaInput: String = "",
    val errorDescripcionAsistencia: String? = null,

    // Cuidado Tecnológico: Hardware / Batería / Almacenamiento y Bienestar Digital (Unidad 3)
    val diagnosticoHardware: DiagnosticoHardware = DiagnosticoHardware(),
    val metricasBienestar: MetricasBienestarDigital = MetricasBienestarDigital(),
    val telemetriaSistema: TelemetriaSistema = TelemetriaSistema(),
    val diagnosticoSistema: DiagnosticoSistema = DiagnosticoSistema(),
    val historialDiagnosticos: List<DiagnosticoHardwareRegistro> = emptyList(),
    val sincronizandoApi: Boolean = false,
    val urlServidorSpring: String = "http://10.0.2.2:8080/",
    val historialRamPorcentaje: List<Float> = emptyList(),
    val historialAlmacenamientoPorcentaje: List<Float> = emptyList(),
    val historialRedBajadaKbps: List<Float> = emptyList(),
    val historialRedSubidaKbps: List<Float> = emptyList(),
    val historialCpuAppPorcentaje: List<Float> = emptyList(),
    val historialPotenciaWatts: List<Float> = emptyList(),
    val monitoreoEnVivoActivo: Boolean = true
)

class TioTecViewModel(
    private val repository: TioTecRepository = TioTecRepository(),
    private val preferenciasDataStore: PreferenciasDataStore? = null
) : ViewModel() {

    /*
     * Con Room, las listas parten vacías y se llenan con lo que hay guardado en la base.
     * Sin Room (tests y previews) se usan los datos de ejemplo en memoria.
     * Antes la app mostraba los datos de ejemplo y estos desaparecían al guardar el primer
     * registro real; ahora lo que se ve es exactamente lo que hay en Room.
     */
    private val usaRoom = repository.tieneRoom

    private val _uiState = MutableStateFlow(
        TioTecUiState(
            usuario = repository.obtenerPerfilInicial(),
            dispositivos = if (usaRoom) emptyList() else repository.obtenerDispositivosIniciales(),
            preguntas = repository.obtenerPreguntasEvaluacion(),
            checklist = repository.obtenerChecklistInicial(),
            historial = if (usaRoom) emptyList() else repository.obtenerHistorialInicial(),
            ultimaEvaluacion = if (usaRoom) null else repository.obtenerHistorialInicial().firstOrNull(),
            solicitudesAsistencia = if (usaRoom) emptyList() else repository.obtenerSolicitudesAsistenciaIniciales()
        )
    )
    val uiState: StateFlow<TioTecUiState> = _uiState.asStateFlow()

    init {
        preferenciasDataStore?.let { dataStore ->
            viewModelScope.launch {
                dataStore.aliasUsuarioFlow.collect { alias ->
                    if (!alias.isNullOrBlank()) {
                        _uiState.update { current ->
                            current.copy(usuario = current.usuario.copy(nombre = alias))
                        }
                    }
                }
            }
            viewModelScope.launch {
                dataStore.nivelExperienciaFlow.collect { nivel ->
                    if (!nivel.isNullOrBlank()) {
                        val exp = runCatching { NivelExperiencia.valueOf(nivel) }.getOrNull()
                        if (exp != null) {
                            _uiState.update { current ->
                                current.copy(usuario = current.usuario.copy(nivelExperiencia = exp))
                            }
                        }
                    }
                }
            }
        }

        // Observación reactiva desde Room (Actividad 2.5)
        // La lista en pantalla = lo que hay en Room (también cuando queda vacía).
        repository.observarDispositivos()?.let { flow ->
            viewModelScope.launch {
                flow.collect { lista ->
                    _uiState.update { it.copy(dispositivos = lista) }
                }
            }
        }
        repository.observarChecklist()?.let { flow ->
            viewModelScope.launch {
                var primeraLectura = true
                flow.collect { lista ->
                    if (lista.isEmpty() && primeraLectura) {
                        // Primera vez que se abre la app: se guardan las acciones sugeridas en Room
                        // para que marcarlas como completadas también quede guardado.
                        primeraLectura = false
                        repository.guardarAcciones(repository.obtenerChecklistInicial())
                    } else {
                        primeraLectura = false
                        _uiState.update { it.copy(checklist = lista) }
                    }
                }
            }
        }
        repository.observarHistorial()?.let { flow ->
            viewModelScope.launch {
                flow.collect { lista ->
                    _uiState.update {
                        it.copy(
                            historial = lista,
                            ultimaEvaluacion = lista.firstOrNull()
                        )
                    }
                }
            }
        }
        repository.observarSolicitudesAsistencia()?.let { flow ->
            viewModelScope.launch {
                flow.collect { lista ->
                    _uiState.update { it.copy(solicitudesAsistencia = lista) }
                }
            }
        }

        // Al abrir la app se traen los dispositivos guardados en el microservicio.
        if (usaRoom && repository.tieneServidor) {
            sincronizarDispositivosConServidor(mostrarMensaje = false)
        }
        repository.observarHistorialDiagnosticos()?.let { flow ->
            viewModelScope.launch {
                flow.collect { lista ->
                    _uiState.update { it.copy(historialDiagnosticos = lista) }
                }
            }
        }
    }

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
        val nuevoNombre = state.nombrePerfilInput.trim()
        val nuevoNivel = state.nivelExperienciaSeleccionado
        _uiState.update {
            it.copy(
                usuario = it.usuario.copy(
                    nombre = nuevoNombre,
                    nivelExperiencia = nuevoNivel
                ),
                nombrePerfilInput = "",
                errorNombrePerfil = null,
                mensajeExito = "Perfil actualizado correctamente"
            )
        }
        preferenciasDataStore?.let { dataStore ->
            viewModelScope.launch {
                dataStore.guardarPerfil(
                    alias = nuevoNombre,
                    nivel = nuevoNivel.name
                )
            }
        }
    }

    /** Alias para compatibilidad con guías y scripts de avance. */
    fun actualizarPerfil() = guardarPerfil()

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

    /** Recibe la ruta de la foto tomada con la cámara (opcional). */
    fun onFotoDispositivoChange(ruta: String?) {
        _uiState.update { it.copy(fotoDispositivoInput = ruta) }
    }

    /** Valida los dos campos a la vez para mostrar todos los errores juntos. */
    /** Alias para compatibilidad con guías y scripts de avance. */
    fun agregarDispositivo() = registrarDispositivo()

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

        val idEnEdicion = state.dispositivoEnEdicionId
        if (idEnEdicion != null) {
            actualizarDispositivo(
                id = idEnEdicion,
                nombre = state.nombreDispositivoInput,
                tipo = state.tipoDispositivoSeleccionado,
                sistemaOperativo = state.sistemaOperativoInput,
                fotoUri = state.fotoDispositivoInput
            )
        } else {
            agregarDispositivo(
                nombre = state.nombreDispositivoInput,
                tipo = state.tipoDispositivoSeleccionado,
                sistemaOperativo = state.sistemaOperativoInput,
                fotoUri = state.fotoDispositivoInput
            )
        }
        limpiarFormularioDispositivo()
    }

    private fun limpiarFormularioDispositivo() {
        _uiState.update {
            it.copy(
                nombreDispositivoInput = "",
                sistemaOperativoInput = "",
                tipoDispositivoSeleccionado = TipoDispositivo.SMARTPHONE,
                fotoDispositivoInput = null,
                errorNombreDispositivo = null,
                errorSistemaOperativo = null,
                dispositivoEnEdicionId = null
            )
        }
    }

    /** Carga los datos del dispositivo en el formulario para editarlo (la "U" del CRUD). */
    fun iniciarEdicionDispositivo(id: String) {
        val dispositivo = _uiState.value.dispositivos.find { it.id == id } ?: return
        _uiState.update {
            it.copy(
                dispositivoEnEdicionId = dispositivo.id,
                nombreDispositivoInput = dispositivo.nombre,
                tipoDispositivoSeleccionado = dispositivo.tipo,
                sistemaOperativoInput = dispositivo.sistemaOperativo,
                fotoDispositivoInput = dispositivo.fotoUri,
                errorNombreDispositivo = null,
                errorSistemaOperativo = null
            )
        }
    }

    fun cancelarEdicionDispositivo() = limpiarFormularioDispositivo()

    private fun actualizarDispositivo(
        id: String,
        nombre: String,
        tipo: TipoDispositivo,
        sistemaOperativo: String,
        fotoUri: String?
    ) {
        val actualizado = Dispositivo(
            id = id,
            nombre = nombre.trim(),
            tipo = tipo,
            sistemaOperativo = sistemaOperativo.trim(),
            fotoUri = fotoUri
        )
        _uiState.update {
            it.copy(
                dispositivos = it.dispositivos.map { d -> if (d.id == id) actualizado else d },
                mensajeExito = "Dispositivo '${actualizado.nombre}' actualizado"
            )
        }
        viewModelScope.launch {
            repository.guardarDispositivo(actualizado) // Room: REPLACE con el mismo id = UPDATE
            if (repository.tieneServidor) {
                informarResultadoServidor(
                    repository.actualizarDispositivoRemoto(actualizado, _uiState.value.urlServidorSpring),
                    "Cambios guardados en el servidor"
                )
            }
        }
    }

    /** Muestra en la pantalla si el microservicio respondió bien o no. */
    private fun informarResultadoServidor(resultado: Result<*>, mensajeOk: String) {
        _uiState.update {
            if (resultado.isSuccess) {
                it.copy(servidorDispositivosOk = true, estadoServidorDispositivos = mensajeOk)
            } else {
                it.copy(
                    servidorDispositivosOk = false,
                    estadoServidorDispositivos = "Guardado solo en el teléfono: no se pudo conectar con el servidor " +
                        "(${resultado.exceptionOrNull()?.localizedMessage ?: "error desconocido"})"
                )
            }
        }
    }

    /** GET al microservicio y copia en Room. Se usa al abrir la app y con el botón "Sincronizar". */
    fun sincronizarDispositivosConServidor(mostrarMensaje: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizandoDispositivos = true) }
            val resultado = repository.descargarDispositivosRemotos(_uiState.value.urlServidorSpring)
            _uiState.update { it.copy(sincronizandoDispositivos = false) }
            val cantidad = resultado.getOrNull()?.size ?: 0
            informarResultadoServidor(resultado, "Conectado al servidor: $cantidad dispositivo(s) en Spring Boot")
            if (mostrarMensaje && resultado.isSuccess) {
                _uiState.update { it.copy(mensajeExito = "Dispositivos sincronizados con el servidor") }
            }
        }
    }

    /** Consulta la API externa endoflife.date. No modifica Room ni el microservicio. */
    fun consultarSoporteAndroid() {
        if (_uiState.value.cargandoSoporteAndroid) return
        viewModelScope.launch {
            _uiState.update { it.copy(cargandoSoporteAndroid = true, errorSoporteAndroid = null) }
            repository.consultarSoporteAndroid()
                .onSuccess { versiones ->
                    _uiState.update { it.copy(cargandoSoporteAndroid = false, versionesAndroid = versiones) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            cargandoSoporteAndroid = false,
                            errorSoporteAndroid = "No se pudo consultar endoflife.date (${error.localizedMessage ?: "sin conexión"})"
                        )
                    }
                }
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
        viewModelScope.launch {
            repository.guardarDispositivo(nuevo) // 1) Room (funciona sin internet)
            if (repository.tieneServidor) {      // 2) microservicio Spring Boot
                informarResultadoServidor(
                    repository.crearDispositivoRemoto(nuevo, _uiState.value.urlServidorSpring),
                    "Dispositivo guardado en el servidor"
                )
            }
        }
    }

    fun eliminarDispositivo(id: String) {
        _uiState.update {
            it.copy(
                dispositivos = it.dispositivos.filterNot { d -> d.id == id },
                mensajeExito = "Dispositivo eliminado"
            )
        }
        if (_uiState.value.dispositivoEnEdicionId == id) limpiarFormularioDispositivo()
        viewModelScope.launch {
            repository.eliminarDispositivo(id)
            if (repository.tieneServidor) {
                informarResultadoServidor(
                    repository.eliminarDispositivoRemoto(id, _uiState.value.urlServidorSpring),
                    "Dispositivo eliminado del servidor"
                )
            }
        }
    }

    fun seleccionarRespuesta(preguntaId: Int, puntaje: Int) {
        _uiState.update { state ->
            state.copy(
                respuestasActuales = state.respuestasActuales + (preguntaId to puntaje),
                // al responder, esa pregunta deja de estar marcada como pendiente
                preguntasSinResponder = state.preguntasSinResponder - preguntaId,
                errorFormulario = null
            )
        }
    }

    fun finalizarEvaluacion(): Boolean {
        val state = _uiState.value
        val faltantes = ValidadorFormularios.preguntasSinResponder(
            idsPreguntas = state.preguntas.map { it.id },
            respuestas = state.respuestasActuales
        )
        if (faltantes.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    preguntasSinResponder = faltantes,
                    errorFormulario = "Te faltan ${faltantes.size} pregunta(s) por responder"
                )
            }
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
            fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
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

        // Evita duplicados: no se agrega una acción si ya hay una pendiente con el mismo título.
        val titulosPendientes = state.checklist.filter { !it.completada }.map { it.titulo }.toSet()
        val accionesAGuardar = nuevasAcciones.filter { it.titulo !in titulosPendientes }

        _uiState.update {
            it.copy(
                ultimaEvaluacion = nuevaEval,
                historial = listOf(nuevaEval) + it.historial,
                checklist = accionesAGuardar + it.checklist,
                respuestasActuales = emptyMap(),
                preguntasSinResponder = emptySet(),
                resultadoReciente = nuevaEval,
                accionesGeneradas = nuevasAcciones,
                errorFormulario = null,
                mensajeExito = "Evaluación completada: ${nivelRiesgo.etiqueta}"
            )
        }
        viewModelScope.launch {
            repository.guardarEvaluacion(nuevaEval)
            if (accionesAGuardar.isNotEmpty()) {
                repository.guardarAcciones(accionesAGuardar)
            }
        }
        return true
    }

    /** Vuelve del resultado al inicio del cuestionario para una nueva evaluación. */
    fun cerrarResultado() {
        _uiState.update { it.copy(resultadoReciente = null, accionesGeneradas = emptyList()) }
    }

    fun alternarEstadoAccion(accionId: String) {
        _uiState.update { state ->
            state.copy(
                checklist = state.checklist.map { accion ->
                    if (accion.id == accionId) accion.copy(completada = !accion.completada) else accion
                }
            )
        }
        val nuevoEstado = _uiState.value.checklist.find { it.id == accionId }?.completada
        if (nuevoEstado != null) {
            viewModelScope.launch {
                repository.alternarAccion(accionId, nuevoEstado)
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(errorFormulario = null, mensajeExito = null) }
    }


    // ---------------------------------------------------------------------
    // Solicitudes de Asistencia Simulada (Issue #12 · Caso TíoTec 3.2)
    // ---------------------------------------------------------------------

    fun onCategoriaAsistenciaChange(categoria: CategoriaSeguridad) {
        _uiState.update { it.copy(categoriaAsistenciaSeleccionada = categoria) }
    }

    fun onDescripcionAsistenciaChange(texto: String) {
        _uiState.update {
            it.copy(
                descripcionAsistenciaInput = texto,
                errorDescripcionAsistencia = ValidadorFormularios.validarDescripcionAsistencia(texto)
            )
        }
    }

    fun registrarSolicitudAsistencia() {
        val state = _uiState.value
        val error = ValidadorFormularios.validarDescripcionAsistencia(state.descripcionAsistenciaInput)
        if (error != null) {
            _uiState.update { it.copy(errorDescripcionAsistencia = error) }
            return
        }

        val nueva = SolicitudAsistencia(
            id = "asist_${System.currentTimeMillis()}",
            categoria = state.categoriaAsistenciaSeleccionada,
            descripcion = state.descripcionAsistenciaInput.trim(),
            estado = EstadoAsistencia.PENDIENTE,
            fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        )

        _uiState.update {
            it.copy(
                solicitudesAsistencia = listOf(nueva) + it.solicitudesAsistencia,
                descripcionAsistenciaInput = "",
                errorDescripcionAsistencia = null,
                mensajeExito = "Solicitud de asistencia enviada correctamente"
            )
        }

        viewModelScope.launch {
            repository.guardarSolicitudAsistencia(nueva)
        }
    }

    fun alternarEstadoSolicitudAsistencia(id: String) {
        val actual = _uiState.value.solicitudesAsistencia.find { it.id == id } ?: return
        val nuevoEstado = if (actual.estado == EstadoAsistencia.PENDIENTE) EstadoAsistencia.ATENDIDA else EstadoAsistencia.PENDIENTE
        _uiState.update { state ->
            state.copy(
                solicitudesAsistencia = state.solicitudesAsistencia.map {
                    if (it.id == id) it.copy(estado = nuevoEstado) else it
                }
            )
        }
        viewModelScope.launch {
            repository.alternarEstadoAsistencia(id, nuevoEstado == EstadoAsistencia.ATENDIDA)
        }
    }

    // =========================================================================
    // CUIDADO TECNOLÓGICO: Hardware, Batería, Almacenamiento y Bienestar Digital
    // =========================================================================

    private fun agregarMuestra(
        historial: List<Float>,
        valor: Float,
        maxMuestras: Int = 24
    ): List<Float> = (historial + valor).takeLast(maxMuestras)

    private fun aplicarTelemetriaConHistorial(
        current: TioTecUiState,
        hardware: DiagnosticoHardware,
        diagnostico: DiagnosticoSistema
    ): TioTecUiState = current.copy(
        diagnosticoHardware = hardware,
        diagnosticoSistema = diagnostico,
        historialRamPorcentaje = agregarMuestra(
            current.historialRamPorcentaje,
            diagnostico.ram.porcentajeUsado.toFloat()
        ),
        historialAlmacenamientoPorcentaje = agregarMuestra(
            current.historialAlmacenamientoPorcentaje,
            diagnostico.almacenamiento.porcentajeUsado.toFloat()
        )
    )

    /**
     * Consulta los sensores nativos estables del dispositivo y actualiza el estado de la UI.
     */
    fun cargarCuidadoTecnologico(context: Context) {
        val hardware = HardwareSaludHelper.obtenerDiagnostico(context)
        val bienestar = BienestarDigitalHelper.obtenerMetricas(context)
        val diagnostico = TelemetriaSistemaHelper.capturarDiagnostico(context)
        _uiState.update { current ->
            aplicarTelemetriaConHistorial(
                current = current.copy(metricasBienestar = bienestar),
                hardware = hardware,
                diagnostico = diagnostico
            )
        }
    }

    /**
     * Refresca la telemetría del sistema en vivo para métricas estables.
     */
    fun refrescarTelemetriaEnVivo(context: Context) {
        val hardware = HardwareSaludHelper.obtenerDiagnostico(context)
        val diagnostico = TelemetriaSistemaHelper.capturarDiagnostico(context)
        _uiState.update { current ->
            aplicarTelemetriaConHistorial(
                current = current,
                hardware = hardware,
                diagnostico = diagnostico
            )
        }
    }

    fun alternarMonitoreoEnVivo() {
        _uiState.update { current ->
            current.copy(monitoreoEnVivoActivo = !current.monitoreoEnVivoActivo)
        }
    }

    /**
     * Ejecuta un escaneo en vivo del estado del dispositivo, refresca los datos
     * y emite una notificación nativa preventiva si detecta alguna anomalía.
     */
    fun ejecutarEscaneoMantenimiento(context: Context) {
        val hardware = HardwareSaludHelper.obtenerDiagnostico(context)
        val bienestar = BienestarDigitalHelper.obtenerMetricas(context)
        val diagnostico = TelemetriaSistemaHelper.capturarDiagnostico(context)
        ChequeoMantenimientoHelper.evaluarYNotificar(context, hardware, bienestar)

        _uiState.update { current ->
            aplicarTelemetriaConHistorial(
                current = current.copy(
                    metricasBienestar = bienestar,
                    mensajeExito = "Escaneo de hardware y bienestar completado con éxito"
                ),
                hardware = hardware,
                diagnostico = diagnostico
            )
        }
    }

    fun onUrlServidorSpringChange(nuevaUrl: String) {
        _uiState.update { it.copy(urlServidorSpring = nuevaUrl) }
    }

    /**
     * Guarda el snapshot de diagnóstico estable en la base de datos local Room (SQLite).
     */
    fun guardarDiagnosticoEnRoom() {
        val state = _uiState.value
        val registro = DiagnosticoHardwareRegistro.fromDiagnosticoSistema(state.diagnosticoSistema)
        _uiState.update { it.copy(mensajeExito = "Diagnóstico guardado exitosamente en Room (SQLite)") }
        viewModelScope.launch {
            repository.guardarDiagnosticoHardware(registro)
        }
    }

    fun eliminarDiagnosticoEnRoom(id: String) {
        _uiState.update { it.copy(mensajeExito = "Diagnóstico eliminado de Room") }
        viewModelScope.launch {
            repository.eliminarDiagnosticoHardware(id)
        }
    }

    /**
     * Envía el diagnóstico al backend Spring Boot mediante Retrofit.
     */
    fun sincronizarDiagnosticoConBackend(registroId: String? = null) {
        val state = _uiState.value
        val registro = if (registroId != null) {
            state.historialDiagnosticos.find { it.id == registroId }
                ?: DiagnosticoHardwareRegistro.fromDiagnosticoSistema(state.diagnosticoSistema)
        } else {
            DiagnosticoHardwareRegistro.fromDiagnosticoSistema(state.diagnosticoSistema)
        }

        viewModelScope.launch {
            _uiState.update { it.copy(sincronizandoApi = true) }
            val resultado = repository.enviarDiagnosticoRemoto(registro, baseUrl = _uiState.value.urlServidorSpring)
            resultado.fold(
                onSuccess = { dto ->
                    _uiState.update {
                        it.copy(
                            sincronizandoApi = false,
                            mensajeExito = "Sincronizado con Spring Boot: ${dto.id}"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            sincronizandoApi = false,
                            errorFormulario = "Fallo al conectar con Spring Boot (${error.localizedMessage ?: "Verifica que el servidor esté activo"})"
                        )
                    }
                }
            )
        }
    }

    /**
     * Consulta los diagnósticos registrados en el servidor Spring Boot vía Retrofit.
     */
    fun consultarDiagnosticosRemotos() {
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizandoApi = true) }
            val resultado = repository.obtenerDiagnosticosRemotos(baseUrl = _uiState.value.urlServidorSpring)
            resultado.fold(
                onSuccess = { listaRemota ->
                    _uiState.update {
                        it.copy(
                            sincronizandoApi = false,
                            mensajeExito = "Se consultaron ${listaRemota.size} diagnósticos desde Spring Boot"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            sincronizandoApi = false,
                            errorFormulario = "Error al consultar Spring Boot (${error.localizedMessage})"
                        )
                    }
                }
            )
        }
    }

    /**
     * Incorpora acciones preventivas al Checklist oficial según los riesgos detectados en hardware y uso digital.
     */
    fun generarAccionesCuidadoEnChecklist() {
        val state = _uiState.value
        val nuevasAcciones = mutableListOf<AccionChecklist>()
        val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        if (state.diagnosticoHardware.porcentajeAlmacenamientoUsado >= 80) {
            nuevasAcciones.add(
                AccionChecklist(
                    id = "check_storage_${System.currentTimeMillis()}",
                    titulo = "Limpieza de almacenamiento: depurar caché y desinstalar apps en desuso",
                    descripcion = "El almacenamiento supera el 80% de ocupación. Libera espacio para mantener el rendimiento.",
                    categoria = CategoriaSeguridad.ACTUALIZACIONES,
                    prioridad = PrioridadAccion.ALTA,
                    fechaSugerida = fechaHoy
                )
            )
        }

        if (state.diagnosticoHardware.temperaturaBateria >= 38.0f) {
            nuevasAcciones.add(
                AccionChecklist(
                    id = "check_temp_${System.currentTimeMillis()}",
                    titulo = "Cuidado térmico: evitar carga rápida con funda gruesa y tareas intensivas",
                    descripcion = "La batería superó los 38°C. Evita usar el teléfono mientras carga y retira fundas térmicas.",
                    categoria = CategoriaSeguridad.RESPALDOS,
                    prioridad = PrioridadAccion.ALTA,
                    fechaSugerida = fechaHoy
                )
            )
        }

        if (state.metricasBienestar.tiempoTotalPantallaMinutos >= 180) {
            nuevasAcciones.add(
                AccionChecklist(
                    id = "check_digital_${System.currentTimeMillis()}",
                    titulo = "Higiene digital: configurar pausas de pantalla y activar modo descanso nocturno",
                    descripcion = "Uso de pantalla superior a 3 horas diarias. Se recomienda activar límites y pausas programadas.",
                    categoria = CategoriaSeguridad.RECUPERACION,
                    prioridad = PrioridadAccion.MEDIA,
                    fechaSugerida = fechaHoy
                )
            )
        }

        if (state.diagnosticoSistema.ram.ramAlerta) {
            nuevasAcciones.add(
                AccionChecklist(
                    id = "check_ram_${System.currentTimeMillis()}",
                    titulo = "Optimización de RAM: cerrar procesos pesados y revisar apps en segundo plano",
                    descripcion = "La memoria RAM alcanzó el ${state.diagnosticoSistema.ram.porcentajeUsado}% de uso (${state.diagnosticoSistema.ram.usadaMb} MB usados).",
                    categoria = CategoriaSeguridad.ACTUALIZACIONES,
                    prioridad = PrioridadAccion.ALTA,
                    fechaSugerida = fechaHoy
                )
            )
        }

        if (state.diagnosticoSistema.bateria.temperaturaAlerta) {
            nuevasAcciones.add(
                AccionChecklist(
                    id = "check_energia_${System.currentTimeMillis()}",
                    titulo = "Temperatura elevada en batería: permitir enfriamiento del dispositivo",
                    descripcion = "Se detectó temperatura de ${state.diagnosticoSistema.bateria.temperaturaCelsius} °C en la batería.",
                    categoria = CategoriaSeguridad.RESPALDOS,
                    prioridad = PrioridadAccion.ALTA,
                    fechaSugerida = fechaHoy
                )
            )
        }

        if (state.diagnosticoSistema.almacenamiento.almacenamientoAlerta) {
            nuevasAcciones.add(
                AccionChecklist(
                    id = "check_storage_alert_${System.currentTimeMillis()}",
                    titulo = "Almacenamiento crítico: liberar espacio urgente",
                    descripcion = "Quedan solo ${state.diagnosticoSistema.almacenamiento.libreGb} GB libres de ${state.diagnosticoSistema.almacenamiento.totalGb} GB.",
                    categoria = CategoriaSeguridad.ACTUALIZACIONES,
                    prioridad = PrioridadAccion.ALTA,
                    fechaSugerida = fechaHoy
                )
            )
        }

        if (nuevasAcciones.isEmpty()) {
            nuevasAcciones.add(
                AccionChecklist(
                    id = "check_mant_${System.currentTimeMillis()}",
                    titulo = "Chequeo rutinario: reiniciar el teléfono semanalmente para liberar memoria RAM",
                    descripcion = "Tu dispositivo se encuentra en rangos saludables. Mantén un reinicio periódico preventivo.",
                    categoria = CategoriaSeguridad.ACTUALIZACIONES,
                    prioridad = PrioridadAccion.BAJA,
                    fechaSugerida = fechaHoy
                )
            )
        }

        val listaActualizada = state.checklist + nuevasAcciones
        _uiState.update {
            it.copy(
                checklist = listaActualizada,
                mensajeExito = "Se agregaron ${nuevasAcciones.size} tarea(s) preventivas a tu Checklist"
            )
        }

        viewModelScope.launch {
            repository.guardarAcciones(nuevasAcciones)
        }
    }
}

