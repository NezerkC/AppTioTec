package cl.duoc.tiotec.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

/**
 * API EXTERNA (no es nuestra): https://endoflife.date
 * Informa qué versiones de Android siguen recibiendo actualizaciones de seguridad.
 * Es pública, gratuita y no pide clave. Solo se LEE; no se guarda en Room ni en el backend.
 */
interface EndOfLifeApiService {

    @GET("api/v1/products/android/")
    suspend fun obtenerCicloDeVidaAndroid(): EndOfLifeRespuestaDto
}

data class EndOfLifeRespuestaDto(
    @SerializedName("result") val result: EndOfLifeProductoDto
)

data class EndOfLifeProductoDto(
    @SerializedName("name") val name: String,
    @SerializedName("releases") val releases: List<EndOfLifeVersionDto>
)

data class EndOfLifeVersionDto(
    @SerializedName("name") val name: String,          // "14"
    @SerializedName("label") val label: String?,        // "14 'Upside Down Cake'"
    @SerializedName("releaseDate") val releaseDate: String?,
    @SerializedName("isEol") val isEol: Boolean,        // true = ya no recibe soporte
    @SerializedName("eolFrom") val eolFrom: String?     // fecha en que perdió el soporte
)

/** Cliente Retrofit separado del backend propio: otra URL base y otra interfaz. */
object EndOfLifeClient {

    const val BASE_URL = "https://endoflife.date/"

    fun crearServicio(baseUrl: String = BASE_URL): EndOfLifeApiService {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EndOfLifeApiService::class.java)
    }
}
