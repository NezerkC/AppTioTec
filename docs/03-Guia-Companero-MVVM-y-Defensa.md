# 03 · Guía para el Equipo: Configuración, Arquitectura MVVM y Defensa Técnica

¡Bienvenido al repositorio de **AppTioTec**! Esta guía explica cómo abrir el proyecto sin errores, cómo está organizado el código bajo el patrón **MVVM** y las respuestas clave para la defensa individual (Evaluación Parcial 3).

---

## 1. Cómo Clonar y Abrir el Proyecto en Android Studio

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/NezerkC/AppTioTec.git
   ```
2. **Abrir en Android Studio:**
   * Ve a **`File > Open...`** y selecciona la carpeta raíz clonada (`AppTioTec`).
   * Espera a que **Gradle Sync** termine abajo a la derecha (descargará automáticamente el SDK 36 y las dependencias de Jetpack Compose).
3. **Ejecutar la aplicación:**
   * Crea o selecciona un dispositivo virtual en **Device Manager** (ej. Pixel 7 API 34+) y presiona el botón verde **Run ('app')**.

---

## 2. Explicación de las Carpetas del Proyecto (Guía 7 - Parte 2)

Dentro de `app/src/main/java/cl/duoc/tiotec/` encontrarás las capas del patrón **MVVM**:

* 📁 **`model/` (Capa de Modelo / Dominio):**
  * Contiene las clases de datos (`data class`) y enumeraciones (`enum class`) inmutables:
    * `NivelRiesgo.kt`: Estados `BAJO`, `MEDIO` y `ALTO`.
    * `Usuario.kt`: Datos del perfil ficticio y `NivelExperiencia`.
    * `Dispositivo.kt`: Datos del equipo registrado y URI opcional de fotografía.
    * `EvaluacionSeguridad.kt`: Preguntas, categorías (*Respaldos, Actualizaciones, Autenticación, Recuperación*) y resultados.
    * `AccionChecklist.kt`: Tareas preventivas priorizadas.
* 📁 **`repository/` (Capa de Origen de Datos):**
  * `TioTecRepository.kt`: Provee el catálogo de preguntas, perfil inicial, dispositivos de prueba y checklist base (y conectará con Room / DataStore).
* 📁 **`viewmodel/` (Capa de Lógica de Presentación):**
  * `TioTecViewModel.kt`: Administra el estado único de la interfaz (`TioTecUiState`) mediante `MutableStateFlow` y `StateFlow`. Ejecuta las validaciones de formularios y aplica las reglas de negocio (`RN-01` y `RN-02`).
* 📁 **`ui/` y `theme/` (Capa Visual Declarativa):**
  * Contiene los temas de **Material Design 3** (`Color.kt`, `Theme.kt`, `Type.kt`) y las pantallas `@Composable` que observan al ViewModel con `collectAsStateWithLifecycle()`.

---

## 3. Flujo de Trabajo en Git (Para no pisarnos el código)

Antes de empezar a programar cada día:
```bash
# 1. Traer los últimos cambios del compañero
git pull origin main

# 2. Al terminar tus cambios, guardarlos y subirlos
git add .
git commit -m "Descripción clara de lo que agregaste"
git push origin main
```

---

## 4. Preguntas Frecuentes de Defensa Oral (Evaluación Parcial 3)

Recuerda que en la **Evaluación Parcial 3 (32%)** el docente interroga individualmente durante 15 minutos sin apoyo de apuntes ni IA. Aquí están las preguntas clave sobre nuestra arquitectura:

### ❓ 1. ¿Cómo se comunica la Vista (Compose) con el ViewModel en este proyecto?
* **Respuesta:** Mediante el patrón **UDF (Unidirectional Data Flow / Flujo Unidireccional de Datos)**:
  * **El estado baja:** El `TioTecViewModel` expone un `uiState: StateFlow<TioTecUiState>` de solo lectura, y la pantalla `@Composable` lo observa con `collectAsStateWithLifecycle()`.
  * **Los eventos suben:** Cuando el usuario presiona un botón o escribe en un formulario, la vista llama a una función del ViewModel (por ejemplo, `viewModel.seleccionarRespuesta(...)` o `viewModel.finalizarEvaluacion()`).

### ❓ 2. ¿Por qué existe `_uiState` (con guion bajo) y `uiState` (sin guion bajo) en `TioTecViewModel`?
* **Respuesta:** Es la convención de **Backing Property** en Kotlin para proteger el estado:
  * `_uiState` es privado y de tipo `MutableStateFlow`, por lo que **solo el ViewModel** puede modificar los datos usando `_uiState.update { it.copy(...) }`.
  * `uiState` es público pero de tipo `StateFlow` (inmutable), impidiendo que las pantallas modifiquen el estado por accidente fuera de las reglas de negocio.

### ❓ 3. ¿Cómo se calcula el Nivel de Riesgo al terminar el cuestionario?
* **Respuesta:** En la función `finalizarEvaluacion()` de `TioTecViewModel`:
  1. Primero valida que todas las preguntas estén respondidas (`RN-03`).
  2. Suma los puntos obtenidos y calcula el porcentaje respecto al puntaje máximo posible.
  3. Según `RN-01`: si es $\ge 80\%$ asigna `NivelRiesgo.BAJO`, entre $50\%$ y $79\%$ asigna `NivelRiesgo.MEDIO`, y si es $< 50\%$ asigna `NivelRiesgo.ALTO`.
  4. Además (`RN-02`), por cada pregunta donde hubo fallas, agrega automáticamente la tarea preventiva recomendada al `checklist`.
