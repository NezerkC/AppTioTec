package cl.duoc.tiotec.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.tiotec.model.CategoriaSeguridad
import cl.duoc.tiotec.model.EstadoAsistencia
import cl.duoc.tiotec.model.SolicitudAsistencia

@Entity(tableName = "solicitudes_asistencia")
data class SolicitudAsistenciaEntity(
    @PrimaryKey
    val id: String,
    val categoria: String,
    val descripcion: String,
    val estado: String,
    val fecha: String
) {
    fun toDomain(): SolicitudAsistencia {
        val catEnum = runCatching { CategoriaSeguridad.valueOf(categoria) }.getOrDefault(CategoriaSeguridad.RESPALDOS)
        val estadoEnum = runCatching { EstadoAsistencia.valueOf(estado) }.getOrDefault(EstadoAsistencia.PENDIENTE)
        return SolicitudAsistencia(
            id = id,
            categoria = catEnum,
            descripcion = descripcion,
            estado = estadoEnum,
            fecha = fecha
        )
    }

    companion object {
        fun fromDomain(s: SolicitudAsistencia): SolicitudAsistenciaEntity = SolicitudAsistenciaEntity(
            id = s.id,
            categoria = s.categoria.name,
            descripcion = s.descripcion,
            estado = s.estado.name,
            fecha = s.fecha
        )
    }
}
