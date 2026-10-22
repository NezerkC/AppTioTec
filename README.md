# TíoTec Preventivo · Evaluación y Orientación para Seguridad Digital (`AppTioTec`)

[![Android CI](https://github.com/NezerkC/AppTioTec/actions/workflows/android-ci.yml/badge.svg)](https://github.com/NezerkC/AppTioTec/actions/workflows/android-ci.yml)
[![Auto-Tracker](https://github.com/NezerkC/AppTioTec/actions/workflows/auto-progress-tracker.yml/badge.svg)](https://github.com/NezerkC/AppTioTec/actions/workflows/auto-progress-tracker.yml)
![Progreso](https://img.shields.io/badge/Progreso-50%25%20(5%2F10%20Tareas)-0E8A16?style=flat&logo=github)

**Asignatura:** DSY1105 - Desarrollo de Aplicaciones Móviles (Sección 005V)  
**Institución:** Duoc UC · Escuela de Informática y Telecomunicaciones (Sede Alameda)  
**Organización del Caso:** MCmovil SpA  
**Tablero de Seguimiento:** [GitHub Projects (AppTioTec · Unidad 2)](https://github.com/users/NezerkC/projects/5)  

---

## 1. Contexto y Propósito del Proyecto

**MCmovil SpA** impulsa la iniciativa **TíoTec Preventivo**, orientada a usuarios no especialistas para ayudarles a evaluar sus hábitos básicos de seguridad digital, detectar brechas preventivas antes de que ocurra un incidente y dar seguimiento a un plan de acción guiado.

---

## 2. Alcance del MVP

* **Incluye:**
  * Gestión de perfil ficticio y nivel de experiencia digital.
  * Registro de dispositivos de prueba (*Smartphone, Tablet, Notebook, PC*) e integración con cámara para evidencia ficticia.
  * Cuestionario preventivo en 4 pilares: *Respaldos*, *Actualizaciones*, *Autenticación (2FA)* y *Recuperación de Cuentas*.
  * Cálculo automático de Nivel de Riesgo (*Bajo, Medio, Alto*) con retroalimentación visual y animaciones.
  * Checklist priorizado de acciones preventivas (*Pendientes / Completadas*) e historial de evaluaciones.
  * Persistencia local y arquitectura limpia **MVVM + Jetpack Compose + Material Design 3**.
* **Fuera del alcance:** Datos personales reales o sensibles, pagos en línea, videollamadas, mapas o asignación real de técnicos.

---

## 3. Requerimientos del Sistema

### Requerimientos Funcionales (RF)
* **RF-01 · Gestión de Perfil Ficticio:** Crear y editar perfil sin datos sensibles (Alias y nivel de experiencia).
* **RF-02 · Inventario de Dispositivos:** Registrar dispositivos de prueba con nombre, tipo, sistema operativo y fotografía opcional.
* **RF-03 · Cuestionario Preventivo:** Responder evaluación sobre los 4 pilares de seguridad digital.
* **RF-04 · Diagnóstico y Semáforo de Riesgo:** Clasificar automáticamente el estado en Riesgo Bajo, Medio o Alto.
* **RF-05 · Checklist de Acciones:** Generar y marcar tareas preventivas priorizadas (*Urgente, Recomendado, Opcional*).
* **RF-06 · Historial Preventivo:** Consultar evaluaciones anteriores almacenadas en el dispositivo.

### Reglas de Negocio (RN)
* **RN-01 (Semáforo de Riesgo):**
  * $\ge 80\%$: **Riesgo Bajo**
  * $50\% - 79\%$: **Riesgo Medio**
  * $< 50\%$: **Riesgo Alto**
* **RN-02 (Plan Preventivo Automático):** Toda respuesta no óptima genera automáticamente una tarea asociada en el Checklist.
* **RN-03 (Validación de Datos):** No se permiten campos vacíos, nombres menores a 3 caracteres ni finalizar evaluaciones incompletas.

---

## 4. Estructura Arquitectónica (Patrón MVVM - Guía 7)

```text
app/src/main/java/cl/duoc/tiotec/
├── model/          # Entidades y modelos de dominio (Usuario, Dispositivo, EvaluacionSeguridad, AccionChecklist, NivelRiesgo)
├── viewmodel/      # Lógica de presentación y gestión de estado con StateFlow (TioTecViewModel)
├── repository/     # Origen de datos y catálogo preventivo (TioTecRepository)
├── ui/             # Pantallas declarativas en Jetpack Compose y navegación
└── theme/          # Sistema de diseño Material Design 3 (Color, Type, Theme)
```

---

## 5. Carpeta de Documentación para el Equipo (`docs/`)

Para facilitar el trabajo colaborativo y la preparación de las defensas, toda la documentación del proyecto se encuentra en la carpeta [`docs/`](./docs):

| Documento | Contenido |
| :--- | :--- |
| 📄 **[`01-Caso-Oficial-TioTec.md`](./docs/01-Caso-Oficial-TioTec.md)** | Enunciado íntegro del caso *TíoTec Preventivo* (MCmovil SpA) y contexto del problema. |
| 📋 **[`02-Requerimientos-y-Reglas-Negocio.md`](./docs/02-Requerimientos-y-Reglas-Negocio.md)** | Especificación de Requerimientos Funcionales (`RF-01` a `RF-06`), Reglas de Negocio (`RN-01` a `RN-04`) y diagrama Mermaid (Guía 7 - Parte 1). |
| 🛠️ **[`03-Guia-Companero-MVVM-y-Defensa.md`](./docs/03-Guia-Companero-MVVM-y-Defensa.md)** | Guía paso a paso para clonar y abrir en Android Studio, explicación de capas MVVM y **preguntas/respuestas para la defensa oral (Parcial 3)**. |
| 📌 **[`04-Planificacion-GitHub-Projects-y-CI.md`](./docs/04-Planificacion-GitHub-Projects-y-CI.md)** | Automatización del tablero en **GitHub Projects**, creación automática de Issues por Guía y pipeline **Android CI/CD** (`.github/workflows/`). |
| 📁 **[`docs/pdfs/`](./docs/pdfs)** | Copia de respaldo de los PDFs oficiales (`Caso_005V_TioTec_Preventivo.pdf` y `Guía 7`). |

