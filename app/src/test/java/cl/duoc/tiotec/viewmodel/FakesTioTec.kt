package cl.duoc.tiotec.viewmodel

import cl.duoc.tiotec.data.local.AccionChecklistEntity
import cl.duoc.tiotec.data.local.DiagnosticoHardwareDao
import cl.duoc.tiotec.data.local.DiagnosticoHardwareEntity
import cl.duoc.tiotec.data.local.DispositivoEntity
import cl.duoc.tiotec.data.local.EvaluacionEntity
import cl.duoc.tiotec.data.local.SolicitudAsistenciaEntity
import cl.duoc.tiotec.data.local.TioTecDao
import cl.duoc.tiotec.data.remote.DiagnosticoHardwareRequestDto
import cl.duoc.tiotec.data.remote.DiagnosticoHardwareResponseDto
import cl.duoc.tiotec.data.remote.DispositivoDto
import cl.duoc.tiotec.data.remote.EndOfLifeApiService
import cl.duoc.tiotec.data.remote.EndOfLifeProductoDto
import cl.duoc.tiotec.data.remote.EndOfLifeRespuestaDto
import cl.duoc.tiotec.data.remote.EndOfLifeVersionDto
import cl.duoc.tiotec.data.remote.TioTecApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/*
 * "Fakes": versiones falsas de Room y de Retrofit que guardan todo en listas.
 * Permiten probar el ViewModel y el Repository sin emulador, sin base de datos y sin red.
 */

class FakeTioTecDao : TioTecDao {
    val dispositivos = mutableListOf<DispositivoEntity>()
    val acciones = mutableListOf<AccionChecklistEntity>()
    val evaluaciones = mutableListOf<EvaluacionEntity>()
    val solicitudes = mutableListOf<SolicitudAsistenciaEntity>()

    private val dispositivosFlow = MutableStateFlow<List<DispositivoEntity>>(emptyList())
    private val checklistFlow = MutableStateFlow<List<AccionChecklistEntity>>(emptyList())
    private val evaluacionesFlow = MutableStateFlow<List<EvaluacionEntity>>(emptyList())
    private val solicitudesFlow = MutableStateFlow<List<SolicitudAsistenciaEntity>>(emptyList())

    override fun obtenerTodosLosDispositivos(): Flow<List<DispositivoEntity>> = dispositivosFlow.asStateFlow()

    override suspend fun obtenerDispositivoPorId(id: String): DispositivoEntity? =
        dispositivos.find { it.id == id }

    override suspend fun insertarDispositivo(dispositivo: DispositivoEntity): Long {
        dispositivos.removeAll { it.id == dispositivo.id }
        dispositivos.add(dispositivo)
        dispositivosFlow.value = dispositivos.toList()
        return 1L
    }

    override suspend fun insertarDispositivos(dispositivos: List<DispositivoEntity>): List<Long> {
        dispositivos.forEach { insertarDispositivo(it) }
        return dispositivos.map { 1L }
    }

    override suspend fun eliminarDispositivo(dispositivo: DispositivoEntity): Int {
        return eliminarDispositivoPorId(dispositivo.id)
    }

    override suspend fun eliminarDispositivoPorId(id: String): Int {
        val removed = dispositivos.removeAll { it.id == id }
        dispositivosFlow.value = dispositivos.toList()
        return if (removed) 1 else 0
    }

    override fun obtenerChecklist(): Flow<List<AccionChecklistEntity>> = checklistFlow.asStateFlow()

    override suspend fun insertarAccion(accion: AccionChecklistEntity): Long {
        acciones.removeAll { it.id == accion.id }
        acciones.add(accion)
        checklistFlow.value = acciones.toList()
        return 1L
    }

    override suspend fun insertarAcciones(accionesNuevas: List<AccionChecklistEntity>): List<Long> {
        accionesNuevas.forEach { insertarAccion(it) }
        return accionesNuevas.map { 1L }
    }

    override suspend fun actualizarAccion(accion: AccionChecklistEntity): Int {
        insertarAccion(accion)
        return 1
    }

    override suspend fun alternarEstadoAccion(id: String, completada: Boolean): Int {
        val index = acciones.indexOfFirst { it.id == id }
        if (index != -1) {
            acciones[index] = acciones[index].copy(completada = completada)
            checklistFlow.value = acciones.toList()
            return 1
        }
        return 0
    }

    override suspend fun eliminarAccion(accion: AccionChecklistEntity): Int {
        val removed = acciones.removeAll { it.id == accion.id }
        checklistFlow.value = acciones.toList()
        return if (removed) 1 else 0
    }

    override fun obtenerHistorialEvaluaciones(): Flow<List<EvaluacionEntity>> = evaluacionesFlow.asStateFlow()

    override suspend fun insertarEvaluacion(evaluacion: EvaluacionEntity): Long {
        evaluaciones.removeAll { it.id == evaluacion.id }
        evaluaciones.add(0, evaluacion)
        evaluacionesFlow.value = evaluaciones.toList()
        return 1L
    }

    override suspend fun insertarEvaluaciones(evaluacionesNuevas: List<EvaluacionEntity>): List<Long> {
        evaluacionesNuevas.forEach { insertarEvaluacion(it) }
        return evaluacionesNuevas.map { 1L }
    }

    override fun obtenerUltimaEvaluacion(): Flow<EvaluacionEntity?> = MutableStateFlow(evaluaciones.firstOrNull())

    override fun obtenerSolicitudesAsistencia(): Flow<List<SolicitudAsistenciaEntity>> = solicitudesFlow.asStateFlow()

    override suspend fun insertarSolicitudAsistencia(solicitud: SolicitudAsistenciaEntity): Long {
        solicitudes.add(solicitud)
        solicitudesFlow.value = solicitudes.toList()
        return 1L
    }

    override suspend fun actualizarEstadoAsistencia(id: String, estado: String): Int {
        val index = solicitudes.indexOfFirst { it.id == id }
        if (index != -1) {
            solicitudes[index] = solicitudes[index].copy(estado = estado)
            solicitudesFlow.value = solicitudes.toList()
            return 1
        }
        return 0
    }
}

class FakeDiagnosticoHardwareDao : DiagnosticoHardwareDao {
    val diagnosticos = mutableListOf<DiagnosticoHardwareEntity>()
    private val historialFlow = MutableStateFlow<List<DiagnosticoHardwareEntity>>(emptyList())

    override fun obtenerHistorialDiagnosticos(): Flow<List<DiagnosticoHardwareEntity>> = historialFlow.asStateFlow()

    override suspend fun insertarDiagnostico(diagnostico: DiagnosticoHardwareEntity): Long {
        diagnosticos.removeAll { it.id == diagnostico.id }
        diagnosticos.add(0, diagnostico)
        historialFlow.value = diagnosticos.toList()
        return 1L
    }

    override suspend fun marcarSincronizado(id: String): Int {
        val index = diagnosticos.indexOfFirst { it.id == id }
        if (index != -1) {
            diagnosticos[index] = diagnosticos[index].copy(sincronizadoRemoto = true)
            historialFlow.value = diagnosticos.toList()
            return 1
        }
        return 0
    }

    override suspend fun eliminarHistorialDiagnosticos(): Int {
        val count = diagnosticos.size
        diagnosticos.clear()
        historialFlow.value = emptyList()
        return count
    }

    override suspend fun eliminarDiagnosticoPorId(id: String): Int {
        val removed = diagnosticos.removeAll { it.id == id }
        historialFlow.value = diagnosticos.toList()
        return if (removed) 1 else 0
    }
}

class FakeTioTecApiService : TioTecApiService {
    var llamadasEnviar = 0
    var llamadasObtener = 0

    // "Servidor" en memoria para los dispositivos
    val servidor = linkedMapOf<String, DispositivoDto>()
    var llamadasPost = 0
    var llamadasPut = 0
    var llamadasDelete = 0

    override suspend fun obtenerDispositivos(): List<DispositivoDto> = servidor.values.toList()

    override suspend fun crearDispositivo(dispositivo: DispositivoDto): DispositivoDto {
        llamadasPost++
        servidor[dispositivo.id!!] = dispositivo
        return dispositivo
    }

    override suspend fun actualizarDispositivo(id: String, dispositivo: DispositivoDto): DispositivoDto {
        llamadasPut++
        if (!servidor.containsKey(id)) {
            throw HttpException(Response.error<Any>(404, "".toResponseBody(null)))
        }
        servidor[id] = dispositivo
        return dispositivo
    }

    override suspend fun eliminarDispositivo(id: String): Response<Unit> {
        llamadasDelete++
        return if (servidor.remove(id) != null) Response.success(Unit)
        else Response.error(404, "".toResponseBody(null))
    }

    override suspend fun obtenerDiagnosticosRemotos(): List<DiagnosticoHardwareResponseDto> {
        llamadasObtener++
        return emptyList()
    }

    override suspend fun enviarDiagnosticoRemoto(request: DiagnosticoHardwareRequestDto): DiagnosticoHardwareResponseDto {
        llamadasEnviar++
        return DiagnosticoHardwareResponseDto(
            id = request.id,
            fechaRegistro = request.fechaRegistro,
            timestampMs = request.timestampMs,
            nivelBateria = request.nivelBateria,
            estaCargando = request.estaCargando,
            temperaturaBateria = request.temperaturaBateria,
            voltajeBateria = request.voltajeBateria,
            saludBateria = request.saludBateria,
            fuenteEnergia = request.fuenteEnergia,
            almacenamientoTotalGb = request.almacenamientoTotalGb,
            almacenamientoLibreGb = request.almacenamientoLibreGb,
            porcentajeAlmacenamientoUsado = request.porcentajeAlmacenamientoUsado,
            ramTotalMb = request.ramTotalMb,
            ramDisponibleMb = request.ramDisponibleMb,
            totalSensores = request.totalSensores,
            consumoSensoresMa = request.consumoSensoresMa,
            nivelRiesgo = request.nivelRiesgo,
            recomendacion = request.recomendacion,
            sincronizadoRemoto = true
        )
    }
}


/** Fake de Retrofit que siempre falla, como si el servidor estuviera apagado. */
class FakeApiCaida : TioTecApiService {
    private fun caida(): Nothing = throw java.io.IOException("Failed to connect to /10.0.2.2:8080")
    override suspend fun obtenerDiagnosticosRemotos(): List<DiagnosticoHardwareResponseDto> = caida()
    override suspend fun enviarDiagnosticoRemoto(request: DiagnosticoHardwareRequestDto): DiagnosticoHardwareResponseDto = caida()
    override suspend fun obtenerDispositivos(): List<DispositivoDto> = caida()
    override suspend fun crearDispositivo(dispositivo: DispositivoDto): DispositivoDto = caida()
    override suspend fun actualizarDispositivo(id: String, dispositivo: DispositivoDto): DispositivoDto = caida()
    override suspend fun eliminarDispositivo(id: String): Response<Unit> = caida()
}

/** Fake de la API externa endoflife.date con datos fijos. */
class FakeEndOfLifeApi(private val falla: Boolean = false) : EndOfLifeApiService {
    var llamadas = 0
    override suspend fun obtenerCicloDeVidaAndroid(): EndOfLifeRespuestaDto {
        llamadas++
        if (falla) throw java.io.IOException("Sin internet")
        return EndOfLifeRespuestaDto(
            EndOfLifeProductoDto(
                name = "android",
                releases = listOf(
                    EndOfLifeVersionDto("15", "15 'Vanilla Ice Cream'", "2024-09-03", false, null),
                    EndOfLifeVersionDto("14", "14 'Upside Down Cake'", "2023-10-04", false, null),
                    EndOfLifeVersionDto("12", "12 'Snow Cone'", "2021-10-04", true, "2025-03-03")
                )
            )
        )
    }
}
