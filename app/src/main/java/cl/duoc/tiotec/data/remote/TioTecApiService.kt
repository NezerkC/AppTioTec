package cl.duoc.tiotec.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Contrato Retrofit con NUESTRO microservicio Spring Boot (carpeta backend/).
 * Cada función es un endpoint; Retrofit genera el código HTTP a partir de las anotaciones.
 */
interface TioTecApiService {

    // ---------------- Diagnósticos de hardware (Cuidado Tech) ----------------

    @GET("api/v1/diagnosticos")
    suspend fun obtenerDiagnosticosRemotos(): List<DiagnosticoHardwareResponseDto>

    @POST("api/v1/diagnosticos")
    suspend fun enviarDiagnosticoRemoto(
        @Body request: DiagnosticoHardwareRequestDto
    ): DiagnosticoHardwareResponseDto

    // ---------------- Dispositivos: CRUD completo ----------------

    @GET("api/v1/dispositivos")
    suspend fun obtenerDispositivos(): List<DispositivoDto>

    @POST("api/v1/dispositivos")
    suspend fun crearDispositivo(@Body dispositivo: DispositivoDto): DispositivoDto

    @PUT("api/v1/dispositivos/{id}")
    suspend fun actualizarDispositivo(
        @Path("id") id: String,
        @Body dispositivo: DispositivoDto
    ): DispositivoDto

    /** Response<Unit> porque el backend responde 204 (sin cuerpo). */
    @DELETE("api/v1/dispositivos/{id}")
    suspend fun eliminarDispositivo(@Path("id") id: String): Response<Unit>
}
