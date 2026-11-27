package cl.duoc.tiotec.data.remote

import cl.duoc.tiotec.model.Dispositivo
import cl.duoc.tiotec.model.TipoDispositivo
import com.google.gson.annotations.SerializedName

/**
 * Forma del JSON que viaja entre la app y el backend Spring Boot (/api/v1/dispositivos).
 * Los nombres coinciden con los atributos de la entidad Dispositivo.java del backend.
 * La foto (fotoUri) no viaja: es una ruta dentro del teléfono y el servidor no puede abrirla.
 */
data class DispositivoDto(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("sistemaOperativo") val sistemaOperativo: String
)

fun Dispositivo.toDto(): DispositivoDto = DispositivoDto(
    id = id,
    nombre = nombre,
    tipo = tipo.name,
    sistemaOperativo = sistemaOperativo
)

/** fotoUri se conserva desde la copia local (Room), si existe. */
fun DispositivoDto.toDomain(fotoUri: String? = null): Dispositivo = Dispositivo(
    id = id ?: "",
    nombre = nombre,
    tipo = runCatching { TipoDispositivo.valueOf(tipo.uppercase()) }.getOrDefault(TipoDispositivo.SMARTPHONE),
    sistemaOperativo = sistemaOperativo,
    fotoUri = fotoUri
)
