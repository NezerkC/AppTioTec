package cl.duoc.tiotec.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.tiotec.model.AccionChecklist
import cl.duoc.tiotec.model.CategoriaSeguridad
import cl.duoc.tiotec.model.PrioridadAccion

@Entity(tableName = "checklist_acciones")
data class AccionChecklistEntity(
    @PrimaryKey
    val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val prioridad: String,
    val completada: Boolean,
    val fechaSugerida: String
) {
    fun toDomain(): AccionChecklist {
        val catEnum = runCatching { CategoriaSeguridad.valueOf(categoria) }.getOrDefault(CategoriaSeguridad.RESPALDOS)
        val prioEnum = runCatching { PrioridadAccion.valueOf(prioridad) }.getOrDefault(PrioridadAccion.MEDIA)
        return AccionChecklist(
            id = id,
            titulo = titulo,
            descripcion = descripcion,
            categoria = catEnum,
            prioridad = prioEnum,
            completada = completada,
            fechaSugerida = fechaSugerida
        )
    }

    companion object {
        fun fromDomain(a: AccionChecklist): AccionChecklistEntity = AccionChecklistEntity(
            id = a.id,
            titulo = a.titulo,
            descripcion = a.descripcion,
            categoria = a.categoria.name,
            prioridad = a.prioridad.name,
            completada = a.completada,
            fechaSugerida = a.fechaSugerida
        )
    }
}
