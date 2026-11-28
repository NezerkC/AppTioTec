# TíoTec Preventivo · Evaluación y Orientación para Seguridad Digital (`AppTioTec`)

[![Android CI](https://github.com/NezerkC/AppTioTec/actions/workflows/android-ci.yml/badge.svg)](https://github.com/NezerkC/AppTioTec/actions/workflows/android-ci.yml)
[![Auto-Tracker](https://github.com/NezerkC/AppTioTec/actions/workflows/auto-progress-tracker.yml/badge.svg)](https://github.com/NezerkC/AppTioTec/actions/workflows/auto-progress-tracker.yml)
![Entrega](https://img.shields.io/badge/Entrega-EP4%20Completada%20%7C%20Kover%2088%25-0E8A16?style=flat&logo=github)

**Asignatura:** DSY1105 - Desarrollo de Aplicaciones Móviles (Sección 005V)  
**Institución:** Duoc UC · Escuela de Informática y Telecomunicaciones (Sede Alameda)  
**Organización del Caso:** MCmovil SpA  
**Tablero de Seguimiento:** [GitHub Projects (AppTioTec · Unidades 2 y 3)](https://github.com/users/NezerkC/projects/5)  

---

## 👥 Integrantes del Equipo de Desarrollo

| Nombre Completo | Usuario GitHub | Rol / Aportes Principales |
| :--- | :--- | :--- |
| **Víctor Manuel Martínez Muñoz** | [@NezerkC](https://github.com/NezerkC) (`vmmm25`) | Arquitectura MVVM, Modelos, ViewModel, Preferences DataStore (#8), Room Database SQLite (#10), Asistencia Simulada (#12), CI/CD y automatización. Cuidado Tech, persistencia CRUD Room↔Retrofit, endpoints/tests de diagnósticos en backend y CI/CD dual (Kover + Spring Boot JDK 21). |
| **Danilo Mauricio Sepúlveda Araneda** | [@Daakhen](https://github.com/Daakhen) | UI Jetpack Compose, Perfil y Validación (#6), Cuestionario 4 Pilares (#7), Recursos Nativos Cámara/Notificaciones (#9), Historial (#11). Microservicio Spring Boot (H2 + CRUD Dispositivos), API externa endoflife.date, cobertura Kover, APK release firmado y guía EP4/EP5. |

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

### Entrega EP4: microservicio, integración y API externa

* **Microservicio Spring Boot propio** (`backend/`): Spring Boot 4, Spring Data JPA y **H2 persistente en archivo**. CRUD de dispositivos y registro de diagnósticos de hardware, con validación (`@Valid`) y errores 400/404 en JSON.
* **Integración app ↔ microservicio (CRUD completo):** crear, listar, **editar** y eliminar dispositivos desde *Perfil*. Cada operación se guarda en **Room** y en el **microservicio** mediante **Retrofit**. Al abrir la app se descargan los dispositivos del servidor. Si el servidor no responde, el dato queda en el teléfono y la pantalla lo informa.
* **API externa con Retrofit (endoflife.date):** muestra qué versiones de Android siguen recibiendo actualizaciones de seguridad e indica en cada dispositivo si su versión tiene soporte. Usa un cliente Retrofit separado y es de solo lectura: no escribe en Room ni en el microservicio.
* **Pruebas unitarias:** 71 tests en la app (JUnit 4 + corrutinas + fakes) con **88% de cobertura de líneas de la lógica** medida con **Kover**, y 23 tests en el backend (JUnit 5, Mockito y MockMvc).
* **APK release firmado** con `app/release-tiotec.jks` (alias `tiotec_release`). Copia en [`entrega/`](./entrega/).

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

## 4.1 Endpoints

### Microservicio propio (Spring Boot)

URL base: `http://localhost:8080` desde el PC · `http://10.0.2.2:8080` desde el emulador.

| Método | Endpoint | Descripción | Respuestas |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/dispositivos` | Lista los dispositivos | 200 |
| `GET` | `/api/v1/dispositivos/{id}` | Obtiene un dispositivo | 200 · 404 |
| `POST` | `/api/v1/dispositivos` | Crea un dispositivo | 201 · 400 |
| `PUT` | `/api/v1/dispositivos/{id}` | Actualiza un dispositivo | 200 · 400 · 404 |
| `DELETE` | `/api/v1/dispositivos/{id}` | Elimina un dispositivo | 204 · 404 |
| `GET` | `/api/v1/diagnosticos` | Lista los diagnósticos de hardware | 200 |
| `GET` | `/api/v1/diagnosticos/{id}` | Obtiene un diagnóstico de hardware | 200 · 404 |
| `POST` | `/api/v1/diagnosticos` | Registra un diagnóstico de hardware | 201 · 400 |
| `DELETE` | `/api/v1/diagnosticos/{id}` | Elimina un diagnóstico de hardware | 204 · 404 |

Cuerpo de un dispositivo:

```json
{ "id": "disp_1795813000000", "nombre": "Pixel 8", "tipo": "SMARTPHONE", "sistemaOperativo": "Android 14" }
```

Estructura del backend:

```text
backend/src/main/java/cl/duoc/tiotec/backend/
├── model/          # Entidades JPA: Dispositivo, Diagnostico
├── repository/     # JpaRepository (acceso a H2 sin SQL manual)
├── service/        # Lógica de negocio
├── controller/     # Endpoints REST
└── exception/      # Manejo centralizado de errores 400/404
```

### API externa

| Método | Endpoint | Uso en la app |
| :--- | :--- | :--- |
| `GET` | `https://endoflife.date/api/v1/products/android/` | Estado de soporte de cada versión de Android (`isEol`, `eolFrom`), mostrado en *Perfil* |

---

## 5. Instrucciones de Compilación y Ejecución

### Prerrequisitos
- Android Studio Ladybug / Meerkat o superior con JDK 17 / 21.
- Android SDK Platform 34 / 36 instalado.
- JDK 21 para el microservicio (Maven viene incluido con `mvnw`).

### Ejecutar el sistema completo
1. **Levantar el microservicio** (dejar la terminal abierta):
   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```
   Queda en `http://localhost:8080`. Consola de la base: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/tiotec`, usuario `sa`, sin contraseña).
2. **Ejecutar la app** desde Android Studio en un emulador. La app se conecta a `http://10.0.2.2:8080/`. Para un celular físico, cambiar la URL en *Cuidado → Spring Boot API* por la IP del PC.
3. Abrir **☰ → Perfil** para registrar, editar y eliminar dispositivos.

### Pruebas y cobertura
```powershell
.\gradlew testDebugUnitTest          # tests unitarios de la app
.\gradlew koverHtmlReportDebug       # informe en app/build/reports/kover/htmlDebug/index.html
.\gradlew koverVerifyDebug           # exige al menos 80% de cobertura
cd backend; .\mvnw.cmd test          # tests del microservicio
```

### APK release firmado
```powershell
.\gradlew assembleRelease            # app/build/outputs/apk/release/app-release.apk
```
- Keystore: `app/release-tiotec.jks` · alias `tiotec_release`.
- Configuración: bloque `signingConfigs` de `app/build.gradle.kts`.
- Nota de credenciales (evaluación): `app/build.gradle.kts` incluye credenciales de respaldo para permitir `./gradlew assembleRelease` directo sin configuración previa; en entornos reales se sobreescriben mediante `keystore.properties` (ver `keystore.properties.example`, excluido de Git).
- Certificado: `CN=AppTioTec, OU=DuocUC, O=MCmovil, L=Santiago, ST=RM, C=CL`.
- APK entregado: [`entrega/AppTioTec-v1.0-release.apk`](./entrega/).
- Captura de la verificación de firma: [docs/capturas/apk-firmado.png](./docs/capturas/apk-firmado.png).

### Pasos originales
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
| 📚 **[`06-Guia-Estudio-EP4-EP5.md`](./docs/06-Guia-Estudio-EP4-EP5.md)** | Microservicio, integración, API externa, tests, firma del APK, modificación en vivo y preguntas de la defensa. |
