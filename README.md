# TíoTec Preventivo · Evaluación y Orientación para Seguridad Digital (`AppTioTec`)

[![Android CI](https://github.com/NezerkC/AppTioTec/actions/workflows/android-ci.yml/badge.svg)](https://github.com/NezerkC/AppTioTec/actions/workflows/android-ci.yml)
[![Auto-Tracker](https://github.com/NezerkC/AppTioTec/actions/workflows/auto-progress-tracker.yml/badge.svg)](https://github.com/NezerkC/AppTioTec/actions/workflows/auto-progress-tracker.yml)
![Progreso](https://img.shields.io/badge/Progreso-100%25%20(10%2F10%20Tareas)-0E8A16?style=flat&logo=github)

**Asignatura:** DSY1105 - Desarrollo de Aplicaciones Móviles (Sección 005V)  
**Institución:** Duoc UC · Escuela de Informática y Telecomunicaciones (Sede Alameda)  
**Organización del Caso:** MCmovil SpA  
**Tablero de Seguimiento:** [GitHub Projects (AppTioTec · Unidad 2)](https://github.com/users/NezerkC/projects/5)  

---

## 👥 Integrantes del Equipo de Desarrollo

| Nombre Completo | Usuario GitHub | Rol / Aportes Principales |
| :--- | :--- | :--- |
| **Víctor Manuel Martínez Muñoz** | [@NezerkC](https://github.com/NezerkC) (`vmmm25`) | Arquitectura MVVM, Modelos, ViewModel, Preferences DataStore (#8), Room Database SQLite (#10), Asistencia Simulada (#12), CI/CD y automatización. |
| **Danilo Mauricio Sepúlveda Araneda** | [@Daakhen](https://github.com/Daakhen) | UI Jetpack Compose, Perfil y Validación (#6), Cuestionario 4 Pilares (#7), Recursos Nativos Cámara/Notificaciones (#9), Historial (#11). |

---

## 1. Contexto y Propósito del Proyecto

**MCmovil SpA** impulsa la iniciativa **TíoTec Preventivo**, orientada a usuarios no especialistas para ayudarles a evaluar sus hábitos básicos de seguridad digital, detectar brechas preventivas antes de que ocurra un incidente y dar seguimiento a un plan de acción guiado.

---

## 2. Alcance del MVP y Funcionalidades Implementadas

* **Gestión de Perfil Ficticio (RF-01):** Alias y nivel de experiencia digital con validación reactiva en tiempo real y persistencia en **Preferences DataStore**.
* **Inventario de Dispositivos (RF-02):** Registro de dispositivos (*Smartphone, Tablet, Notebook, PC*) con fotografía tomada mediante recurso nativo de **Cámara** y persistencia relacional en **Room SQLite**.
* **Cuestionario Preventivo (RF-03):** Evaluación de 4 pilares: *Respaldos*, *Actualizaciones*, *Autenticación (2FA)* y *Recuperación de Cuentas*.
* **Diagnóstico y Semáforo de Riesgo (RF-04):** Cálculo reactivo del porcentaje de cumplimiento y nivel de riesgo (*Bajo, Medio, Alto*) con transiciones animadas mediante `animateColorAsState` y feedback con `AnimatedVisibility`.
* **Checklist de Acciones Preventivas (RF-05 / RN-02):** Generación automática de tareas prioritarias ante brechas detectadas con alternancia de estado (*Pendientes / Completadas*) sincronizada en SQLite.
* **Historial Preventivo (RF-06):** Almacenamiento y consulta de diagnósticos históricos persistidos en **Room Database**.
* **Solicitud de Asistencia Técnica Simulada (Caso 3.2):** Registro de consultas guiadas clasificadas por pilar, validadas con `ValidadorFormularios` (mín. 10 caracteres) y seguimiento de estado (*Pendiente / Atendida*).

---

## 3. Reglas de Negocio (RN)

* **RN-01 (Semáforo de Riesgo):**
  * $\ge 80\%$: **Riesgo Bajo** (Color Verde)
  * $50\% - 79\%$: **Riesgo Medio** (Color Ámbar/Naranja)
  * $< 50\%$: **Riesgo Alto** (Color Rojo)
* **RN-02 (Plan Preventivo Automático):** Toda respuesta no óptima (< 2 puntos) genera automáticamente una acción priorizada en el Checklist.
* **RN-03 (Validación de Datos):** Campos requeridos sin espacios en blanco, mínimo 3 caracteres para nombres y mínimo 10 caracteres para consultas de asistencia.
* **RN-04 (Seguridad y Privacidad):** Prohibido solicitar o almacenar contraseñas reales, números de tarjeta o datos sensibles del usuario.

---

## 4. Estructura Arquitectónica (Patrón MVVM + Room + DataStore)

```text
app/src/main/java/cl/duoc/tiotec/
├── data/
│   └── local/             # Persistencia relacional SQLite con Room (@Entity, @Dao, @Database)
│       ├── AccionChecklistEntity.kt
│       ├── DispositivoEntity.kt
│       ├── EvaluacionEntity.kt
│       ├── SolicitudAsistenciaEntity.kt
│       ├── TioTecDao.kt
│       └── TioTecDatabase.kt
├── model/                 # Modelos puros del dominio (Usuario, Dispositivo, Evaluacion, Accion, etc.)
├── repository/            # Origen de datos unificado y Preferences DataStore
│   ├── PreferenciasDataStore.kt
│   └── TioTecRepository.kt
├── viewmodel/             # Lógica de presentación y gestión de estado reactivo con StateFlow
│   └── TioTecViewModel.kt
├── ui/                    # Interfaces declarativas en Jetpack Compose y Navigation
│   ├── components/        # Componentes reutilizables (CampoTextoValidado, SelectorOpciones, etc.)
│   ├── main/              # Dashboard principal y Semáforo animado
│   ├── evaluacion/        # Cuestionario preventivo de 4 pilares
│   ├── historial/         # Historial de evaluaciones guardadas
│   ├── perfil/            # Perfil ficticio e inventario de dispositivos
│   └── asistencia/        # Formulario de solicitud de asistencia técnica simulada
├── nativo/                # Integración con recursos nativos (Cámara y Notificaciones)
├── validation/            # Reglas de validación centralizadas (ValidadorFormularios)
└── theme/                 # Sistema de diseño Material Design 3 (Color, Typography, Theme)
```

---

## 5. Instrucciones de Compilación y Ejecución

### Prerrequisitos
- Android Studio Ladybug / Meerkat o superior con JDK 17 / 21.
- Android SDK Platform 34 / 36 instalado.

### Pasos para Ejecutar
1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/NezerkC/AppTioTec.git
   cd AppTioTec
   ```
2. **Ejecutar suite de pruebas unitarias:**
   ```bash
   ./gradlew testDebugUnitTest
   ```
3. **Compilar APK de depuración:**
   ```bash
   ./gradlew assembleDebug
   ```
4. **Verificar el asistente de avance y requisitos:**
   ```powershell
   .\progreso.ps1
   ```

---

## 6. Documentación Adicional del Proyecto (`docs/`)

| Documento | Contenido |
| :--- | :--- |
| 📄 **[`01-Caso-Oficial-TioTec.md`](./docs/01-Caso-Oficial-TioTec.md)** | Enunciado íntegro del caso *TíoTec Preventivo* (MCmovil SpA) y contexto del problema. |
| 📋 **[`02-Requerimientos-y-Reglas-Negocio.md`](./docs/02-Requerimientos-y-Reglas-Negocio.md)** | Especificación de Requerimientos Funcionales (`RF-01` a `RF-06`), Reglas de Negocio (`RN-01` a `RN-04`) y diagramas. |
| 🛠️ **[`03-Guia-Companero-MVVM-y-Defensa.md`](./docs/03-Guia-Companero-MVVM-y-Defensa.md)** | Guía paso a paso para clonar y abrir en Android Studio, explicación de capas MVVM y **preguntas/respuestas para la defensa oral**. |
| 📌 **[`04-Planificacion-GitHub-Projects-y-CI.md`](./docs/04-Planificacion-GitHub-Projects-y-CI.md)** | Automatización del tablero en **GitHub Projects** y pipeline **Android CI/CD** (`.github/workflows/`). |
