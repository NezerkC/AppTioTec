package cl.duoc.tiotec.repository

import cl.duoc.tiotec.model.AccionChecklist
import cl.duoc.tiotec.model.CategoriaSeguridad
import cl.duoc.tiotec.model.Dispositivo
import cl.duoc.tiotec.model.EvaluacionSeguridad
import cl.duoc.tiotec.model.NivelExperiencia
import cl.duoc.tiotec.model.NivelRiesgo
import cl.duoc.tiotec.model.OpcionRespuesta
import cl.duoc.tiotec.model.PreguntaEvaluacion
import cl.duoc.tiotec.model.PrioridadAccion
import cl.duoc.tiotec.model.TipoDispositivo
import cl.duoc.tiotec.model.Usuario

class TioTecRepository {

    fun obtenerPerfilInicial(): Usuario = Usuario(
        id = "usr_001",
        nombre = "Usuario Demo TíoTec",
        nivelExperiencia = NivelExperiencia.BASICO
    )

    fun obtenerDispositivosIniciales(): List<Dispositivo> = listOf(
        Dispositivo(
            id = "disp_01",
            nombre = "Galaxy A54 Personal",
            tipo = TipoDispositivo.SMARTPHONE,
            sistemaOperativo = "Android 14"
        ),
        Dispositivo(
            id = "disp_02",
            nombre = "Notebook Estudio",
            tipo = TipoDispositivo.NOTEBOOK,
            sistemaOperativo = "Windows 11"
        )
    )

    fun obtenerPreguntasEvaluacion(): List<PreguntaEvaluacion> = listOf(
        PreguntaEvaluacion(
            id = 1,
            categoria = CategoriaSeguridad.RESPALDOS,
            enunciado = "¿Con qué frecuencia realizas copias de seguridad de tus fotos y documentos importantes?",
            opciones = listOf(
                OpcionRespuesta("Nunca o no sé cómo hacerlo", 0, "Configurar respaldo automático en la nube o disco externo"),
                OpcionRespuesta("Solo de vez en cuando manualmente", 1, "Programar respaldos automáticos semanales"),
                OpcionRespuesta("Automáticamente cada semana", 2, null)
            )
        ),
        PreguntaEvaluacion(
            id = 2,
            categoria = CategoriaSeguridad.ACTUALIZACIONES,
            enunciado = "¿Cómo gestionas las actualizaciones del sistema operativo y aplicaciones en tu teléfono?",
            opciones = listOf(
                OpcionRespuesta("Las pospongo o desactivo para ahorrar espacio", 0, "Activar actualizaciones automáticas del sistema con Wi-Fi"),
                OpcionRespuesta("Actualizo solo cuando una app deja de funcionar", 1, "Revisar actualizaciones pendientes mensualmente"),
                OpcionRespuesta("Tengo activadas las actualizaciones automáticas", 2, null)
            )
        ),
        PreguntaEvaluacion(
            id = 3,
            categoria = CategoriaSeguridad.AUTENTICACION,
            enunciado = "¿Utilizas verificación en dos pasos (2FA) y contraseñas distintas en tus cuentas principales?",
            opciones = listOf(
                OpcionRespuesta("Uso la misma contraseña en casi todo y sin 2FA", 0, "Activar verificación en dos pasos (2FA) en correo y banco"),
                OpcionRespuesta("Tengo contraseñas distintas pero sin segundo factor", 1, "Habilitar aplicación autenticadora en cuentas críticas"),
                OpcionRespuesta("Uso contraseñas únicas y doble factor (2FA) activo", 2, null)
            )
        ),
        PreguntaEvaluacion(
            id = 4,
            categoria = CategoriaSeguridad.RECUPERACION,
            enunciado = "¿Tienes configurado un correo o teléfono alternativo actualizado para recuperar tu cuenta principal?",
            opciones = listOf(
                OpcionRespuesta("No tengo datos de recuperación o son antiguos", 0, "Actualizar teléfono y correo de recuperación de cuenta"),
                OpcionRespuesta("Tengo uno configurado pero no lo he verificado", 1, "Verificar códigos de rescate de la cuenta principal"),
                OpcionRespuesta("Sí, están actualizados y verificados", 2, null)
            )
        )
    )

    fun obtenerChecklistInicial(): List<AccionChecklist> = listOf(
        AccionChecklist(
            id = "chk_01",
            titulo = "Activar verificación en dos pasos (2FA)",
            descripcion = "Asegura tu correo principal agregando un segundo factor de autenticación.",
            categoria = CategoriaSeguridad.AUTENTICACION,
            prioridad = PrioridadAccion.ALTA,
            completada = false,
            fechaSugerida = "Esta semana"
        ),
        AccionChecklist(
            id = "chk_02",
            titulo = "Revisar copia de seguridad en la nube",
            descripcion = "Verifica que tus contactos y documentos tengan respaldo reciente.",
            categoria = CategoriaSeguridad.RESPALDOS,
            prioridad = PrioridadAccion.MEDIA,
            completada = false,
            fechaSugerida = "Próximos 7 días"
        ),
        AccionChecklist(
            id = "chk_03",
            titulo = "Instalar parches de seguridad de Android",
            descripcion = "Entra a Ajustes > Actualización de software y aplica parches pendientes.",
            categoria = CategoriaSeguridad.ACTUALIZACIONES,
            prioridad = PrioridadAccion.ALTA,
            completada = true,
            fechaSugerida = "Completado"
        )
    )

    fun obtenerHistorialInicial(): List<EvaluacionSeguridad> = listOf(
        EvaluacionSeguridad(
            id = "eval_01",
            fecha = "10/10/2026",
            puntajeTotal = 4,
            puntajeMaximo = 8,
            nivelRiesgo = NivelRiesgo.MEDIO
        )
    )
}
