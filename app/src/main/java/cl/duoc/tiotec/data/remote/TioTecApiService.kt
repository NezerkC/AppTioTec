package cl.duoc.tiotec.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Contrato Retrofit para la API REST Spring Boot de diagnósticos de hardware.
 */
interface TioTecApiService {

    @GET("api/v1/diagnosticos")
    suspend fun obtenerDiagnosticosRemotos(): List<DiagnosticoHardwareResponseDto>

    @POST("api/v1/diagnosticos")
    suspend fun enviarDiagnosticoRemoto(
        @Body request: DiagnosticoHardwareRequestDto
    ): DiagnosticoHardwareResponseDto
}
