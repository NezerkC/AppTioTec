package cl.duoc.tiotec.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.tiotec.model.Dispositivo
import cl.duoc.tiotec.model.TipoDispositivo

@Entity(tableName = "dispositivos")
data class DispositivoEntity(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val tipo: String,
    val sistemaOperativo: String,
    val fotoUri: String? = null
) {
    fun toDomain(): Dispositivo {
        val tipoEnum = runCatching { TipoDispositivo.valueOf(tipo) }.getOrDefault(TipoDispositivo.SMARTPHONE)
        return Dispositivo(
            id = id,
            nombre = nombre,
            tipo = tipoEnum,
            sistemaOperativo = sistemaOperativo,
            fotoUri = fotoUri
        )
    }

    companion object {
        fun fromDomain(d: Dispositivo): DispositivoEntity = DispositivoEntity(
            id = d.id,
            nombre = d.nombre,
            tipo = d.tipo.name,
            sistemaOperativo = d.sistemaOperativo,
            fotoUri = d.fotoUri
        )
    }
}
