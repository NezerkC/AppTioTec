# Guía de estudio EP4 y EP5 · AppTioTec

Esta guía explica **qué se implementó para la EP4**, **cómo funciona por dentro** y **qué preparar para la defensa individual (EP5)**. Está escrita para leerse en orden.

> **Orden recomendado (≈ 3 horas):**
> 1. Sección 1 (qué hay) y 2 (mapa general): 15 min.
> 2. Sección 3 (conceptos base): 60 min. Es lo que más preguntan.
> 3. Sección 4 (un caso completo de punta a punta): 20 min.
> 4. Secciones 5 a 7 (endpoints, tests, APK): 40 min.
> 5. Sección 8 (modificación en vivo): **ensáyala una vez en tu PC**. Vale el 40% de la EP5.
> 6. Sección 9 (preguntas): repásala en voz alta.

---

## 1. Qué se implementó (contra la rúbrica EP4)

| Ítem de la rúbrica | Qué hay en el proyecto | Dónde mirarlo |
| :--- | :--- | :--- |
| **3.1.1** App con pantallas, formularios y flujos sin errores | Inicio, Cuidado, Evaluar, Checklist, Historial, Perfil y Asistencia. En Perfil ahora se puede **editar** un dispositivo. | `app/src/main/java/cl/duoc/tiotec/ui/` |
| **3.1.2** Microservicio Spring Boot con base de datos y endpoints | Carpeta `backend/`: Spring Boot 4 + JPA + **H2 en archivo**. CRUD de dispositivos y GET/POST de diagnósticos. | `backend/src/main/java/cl/duoc/tiotec/backend/` |
| **3.1.3** Integración app–microservicio con CRUD completo | Crear, listar, editar y eliminar dispositivos desde la app van a **Room** y al **microservicio** con Retrofit. | `TioTecApiService.kt`, `TioTecRepository.kt`, `TioTecViewModel.kt` |
| **3.1.4** API externa con Retrofit, visible en la UI, sin interferir | **endoflife.date**: muestra qué versiones de Android tienen soporte de seguridad y marca cada dispositivo. Cliente Retrofit aparte, solo lectura. | `EndOfLifeApi.kt`, `SoporteAndroid.kt`, tarjeta en Perfil |
| **3.2.1** Tests unitarios ≥ 80% de la lógica | **71 tests** en la app (**88% de líneas** medido con Kover) y **23 tests** en el backend. | `app/src/test/`, `backend/src/test/` |
| **3.3.1** APK release firmado, `.jks` y configuración en el repo | `app/release-tiotec.jks` + `signingConfigs` en `app/build.gradle.kts`. APK en `entrega/`. | Sección 7 |
| **3.3.2** GitHub con commits por integrante | Commits de cada uno en la rama `feature/ep4-backend`. | `git log --author=Daakhen` |

También se corrigió un **error de Room**: los datos de ejemplo se mostraban al abrir la app y desaparecían al guardar el primer registro real. Ahora la pantalla muestra exactamente lo que hay en la base. Además, hacer dos evaluaciones iguales ya no duplica acciones en el checklist.

---

## 2. Mapa general

```text
┌──────────────────────── APP ANDROID (Kotlin + Compose) ────────────────────────┐
│                                                                                 │
│  View (Compose)  ──eventos──▶  ViewModel  ──llama──▶  Repository               │
│  PerfilScreen     ◀─estado──   (StateFlow)            │      │       │         │
│                                                       ▼      ▼       ▼         │
│                                                     Room   Retrofit  Retrofit  │
│                                                   (SQLite)    │         │      │
└───────────────────────────────────────────────────────────────│─────────│──────┘
                                                                │         │
                                    http://10.0.2.2:8080/       │         │  https://endoflife.date/
                                                                ▼         ▼
                                          ┌── MICROSERVICIO PROPIO ──┐   ┌── API EXTERNA ──┐
                                          │ Spring Boot (backend/)   │   │ No es nuestra.  │
                                          │ Controller → Service →   │   │ Solo se lee.    │
                                          │ Repository → H2 (archivo)│   └─────────────────┘
                                          └──────────────────────────┘
```

Hay **dos bases de datos distintas**:

| | Room | H2 |
| :--- | :--- | :--- |
| Dónde vive | En el celular/emulador | En el PC, dentro del backend |
| Quién la usa | La app directamente | Solo el backend |
| Para qué | Que la app funcione sin conexión | Que los datos queden en el servidor |

La app **nunca** entra directo a H2: le pide cosas al backend por HTTP y el backend lee o escribe en H2.

---

## 3. Conceptos base

### 3.1 MVVM

MVVM separa la app en tres responsabilidades:

| Capa | Qué hace | En el proyecto |
| :--- | :--- | :--- |
| **View** | Dibuja la pantalla y avisa lo que hace el usuario. No decide nada. | `PerfilScreen.kt` (Compose) |
| **ViewModel** | Guarda el estado de la pantalla, valida y decide qué hacer. Sobrevive a la rotación. | `TioTecViewModel.kt` |
| **Model** | Los datos y de dónde vienen (Room, servidor, API). | `model/`, `repository/`, `data/` |

**Ejemplo real (registrar un dispositivo):**

1. El usuario escribe "Pixel 8". `PerfilScreen` llama `onNombreDispositivoChange("Pixel 8")`.
2. El ViewModel valida con `ValidadorFormularios` y actualiza `uiState` (`nombreDispositivoInput`, `errorNombreDispositivo`).
3. Compose ve que cambió el estado y vuelve a dibujar el campo (con o sin error).
4. Al tocar **Registrar**, el ViewModel crea el `Dispositivo` y llama al `TioTecRepository`.
5. El repositorio guarda en Room y en el servidor.

**Por qué MVVM (respuesta para la defensa):** separa la interfaz de la lógica. Así la lógica se puede **probar con tests sin emulador** (los tests llaman al ViewModel directamente), el estado no se pierde al rotar la pantalla y cambiar la UI no obliga a tocar la lógica.

### 3.2 Estado con StateFlow

```kotlin
private val _uiState = MutableStateFlow(TioTecUiState(...))   // solo el ViewModel lo modifica
val uiState: StateFlow<TioTecUiState> = _uiState.asStateFlow() // la pantalla solo lo lee
```

- `TioTecUiState` es un `data class` con **todo** lo que la pantalla necesita mostrar.
- Para cambiarlo se usa `_uiState.update { it.copy(campo = nuevoValor) }`: se crea una copia con el cambio.
- En `Navigation.kt`, `collectAsStateWithLifecycle()` hace que Compose se redibuje cada vez que el estado cambia.

### 3.3 Corrutinas

Leer la base o llamar al servidor tarda. Si se hiciera en el hilo principal, la app se congelaría.

- `suspend fun`: una función que puede **esperar** sin bloquear la pantalla.
- `viewModelScope.launch { ... }`: inicia trabajo en segundo plano ligado al ViewModel; si la pantalla se destruye, se cancela solo.

### 3.4 Room (base local)

| Pieza | Qué es | Archivo |
| :--- | :--- | :--- |
| `@Entity` | Una tabla | `DispositivoEntity.kt` (tabla `dispositivos`) |
| `@Dao` | Las consultas (`@Query`, `@Insert`, `@Delete`) | `TioTecDao.kt` |
| `@Database` | Reúne las tablas y crea la base | `TioTecDatabase.kt` |

- `obtenerTodosLosDispositivos()` devuelve un **`Flow`**: cada vez que la tabla cambia, emite la lista nueva y el ViewModel actualiza la pantalla.
- `@Insert(onConflict = REPLACE)`: si el id ya existe lo reemplaza. Por eso **editar** usa la misma función que crear.
- `version = 2` y `fallbackToDestructiveMigration()`: si cambias una tabla, subes la versión y Room borra y recrea la base local.

### 3.5 Retrofit (cliente HTTP)

Retrofit convierte una **interfaz de Kotlin** en llamadas HTTP:

```kotlin
@PUT("api/v1/dispositivos/{id}")
suspend fun actualizarDispositivo(@Path("id") id: String, @Body dispositivo: DispositivoDto): DispositivoDto
```

| Anotación | Significado |
| :--- | :--- |
| `@GET`, `@POST`, `@PUT`, `@DELETE` | Verbo HTTP y ruta |
| `@Path("id")` | Reemplaza `{id}` en la URL |
| `@Body` | El objeto se envía como JSON en el cuerpo |

- **Gson** convierte entre objetos Kotlin y JSON. `@SerializedName("nombre")` dice qué clave del JSON corresponde a cada campo.
- **DTO** (*Data Transfer Object*): la forma del JSON que viaja. `DispositivoDto` no tiene `fotoUri` porque la foto es una ruta dentro del teléfono.
- **`10.0.2.2`**: dentro del emulador, esa dirección apunta al PC. `localhost` apuntaría al propio emulador.
- `usesCleartextTraffic="true"` en el manifiesto permite HTTP sin cifrar hacia el servidor local.
- `RetrofitClient` = cliente del microservicio propio. `EndOfLifeClient` = cliente de la API externa. **Son dos clientes separados**, con URL base distinta.

### 3.6 REST y códigos HTTP

| Verbo | Uso | Código de éxito |
| :--- | :--- | :--- |
| GET | Leer | 200 OK |
| POST | Crear | 201 Created |
| PUT | Actualizar | 200 OK |
| DELETE | Eliminar | 204 No Content |

Errores que devuelve el backend: **400** (datos inválidos, por ejemplo nombre con menos de 3 letras) y **404** (no existe ese id).

### 3.7 Spring Boot (microservicio)

Cada petición pasa por tres capas:

```text
Petición HTTP → Controller → Service → Repository → H2
```

| Capa | Anotación | Responsabilidad | Archivo |
| :--- | :--- | :--- | :--- |
| Entidad | `@Entity`, `@Table`, `@Id` | Clase que se vuelve tabla | `model/Dispositivo.java` |
| Repository | `extends JpaRepository<Dispositivo, String>` | Acceso a datos; Spring genera `findAll`, `save`, `deleteById`… sin escribir SQL | `repository/DispositivoRepository.java` |
| Service | `@Service` | Lógica de negocio: generar id, limpiar textos, lanzar 404 | `service/DispositivoService.java` |
| Controller | `@RestController`, `@RequestMapping` | Recibe HTTP y responde JSON | `controller/DispositivoController.java` |
| Errores | `@RestControllerAdvice` | Convierte excepciones en 400/404 con JSON claro | `exception/ManejadorErrores.java` |

Otras piezas que conviene saber explicar:

- **Inyección de dependencias:** el controlador recibe el servicio en su constructor; Spring crea y entrega los objetos. Nadie hace `new DispositivoService()`.
- **Validación:** `@NotBlank` y `@Size(min = 3)` en la entidad; `@Valid` en el controlador las activa. Si fallan, `ManejadorErrores` responde 400.
- **JPA/Hibernate:** traduce los objetos Java a SQL. `spring.jpa.hibernate.ddl-auto=update` crea o amplía las tablas según las clases.
- **H2 en archivo:** `jdbc:h2:file:./data/tiotec` guarda en `backend/data/` y **no se borra al apagar**. Con `mem` se perdería todo.
- **Tests del backend** usan otra configuración (`src/test/resources/application.properties`) con H2 **en memoria**, para no ensuciar la base real.

### 3.8 Microservicio propio vs API externa

| | Microservicio (Spring Boot) | API externa (endoflife.date) |
| :--- | :--- | :--- |
| ¿Quién la hizo? | Nosotros | Un tercero |
| ¿Se puede modificar? | Sí | No |
| Operaciones | CRUD completo | Solo lectura (GET) |
| ¿Guarda datos de la app? | Sí, en H2 | No |
| Cliente Retrofit | `RetrofitClient` + `TioTecApiService` | `EndOfLifeClient` + `EndOfLifeApiService` |

**"Sin interferir"** (rúbrica 3.1.4): la respuesta de endoflife.date solo se guarda en el estado de la pantalla (`versionesAndroid`). No se escribe en Room ni en el backend. Si no hay internet, aparece un error en su tarjeta y lo demás sigue funcionando (hay un test que lo comprueba: `viewModel_sinInternet_muestraErrorSinTocarLosDispositivos`).

---

## 4. Un caso completo: editar un dispositivo

Recorrido desde el botón hasta la base y de vuelta. **Si entiendes esto, entiendes el proyecto.**

1. **View** — `PerfilScreen.kt`: tocas ✏️ → `onEditarDispositivo(dispositivo.id)`.
2. **ViewModel** — `iniciarEdicionDispositivo(id)`: copia los datos del dispositivo al formulario y guarda `dispositivoEnEdicionId`. El botón cambia a **Guardar cambios**.
3. Cambias el nombre y tocas **Guardar cambios** → `registrarDispositivo()`. Como `dispositivoEnEdicionId` no es null, llama a `actualizarDispositivo(...)`.
4. **ViewModel** actualiza la lista en memoria y lanza una corrutina:
   ```kotlin
   repository.guardarDispositivo(actualizado)                       // Room: REPLACE = update
   repository.actualizarDispositivoRemoto(actualizado, url)         // Servidor: PUT
   ```
5. **Repository** — `actualizarDispositivoRemoto`: llama `PUT /api/v1/dispositivos/{id}`. Si el servidor responde **404** (por ejemplo, se creó sin conexión), lo crea con **POST**. Devuelve `Result` (éxito o falla, sin lanzar excepción).
6. **Retrofit** convierte `DispositivoDto` a JSON y hace la petición a `10.0.2.2:8080`.
7. **Backend** — `DispositivoController.actualizar` recibe el JSON, `@Valid` lo valida, `DispositivoService.actualizar` busca el existente (404 si no está), copia los campos y `repository.save` hace el `UPDATE` en H2.
8. **Respuesta**: 200 con el JSON actualizado. En la app, `informarResultadoServidor` pone "Cambios guardados en el servidor" en la tarjeta del microservicio.
9. **Room** emite la lista nueva por el `Flow` → el ViewModel actualiza `uiState` → Compose redibuja.

**Si el servidor está apagado:** el paso 4 guarda igual en Room y la tarjeta muestra "Guardado solo en el teléfono: no se pudo conectar…". La app no se cae.

**Al abrir la app:** `sincronizarDispositivosConServidor()` hace `GET` y copia en Room lo que hay en el servidor, conservando la foto local.

---

## 5. Endpoints

### Microservicio propio (`http://localhost:8080` desde el PC, `http://10.0.2.2:8080` desde el emulador)

| Método | Ruta | Cuerpo | Respuesta |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/dispositivos` | — | 200 + lista |
| GET | `/api/v1/dispositivos/{id}` | — | 200 o 404 |
| POST | `/api/v1/dispositivos` | `{id, nombre, tipo, sistemaOperativo}` | 201 o 400 |
| PUT | `/api/v1/dispositivos/{id}` | `{nombre, tipo, sistemaOperativo}` | 200, 400 o 404 |
| DELETE | `/api/v1/dispositivos/{id}` | — | 204 o 404 |
| GET | `/api/v1/diagnosticos` | — | 200 + lista |
| GET | `/api/v1/diagnosticos/{id}` | — | 200 o 404 |
| POST | `/api/v1/diagnosticos` | diagnóstico de hardware | 201 o 400 |
| DELETE | `/api/v1/diagnosticos/{id}` | — | 204 o 404 |

Ejemplo de JSON de un dispositivo:

```json
{ "id": "disp_1795813000000", "nombre": "Pixel 8", "tipo": "SMARTPHONE", "sistemaOperativo": "Android 14" }
```

Probar sin la app (PowerShell):

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/dispositivos
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/dispositivos -ContentType "application/json" -Body '{"nombre":"Notebook Casa","tipo":"NOTEBOOK","sistemaOperativo":"Windows 11"}'
```

Ver la base: `http://localhost:8080/h2-console` → JDBC URL `jdbc:h2:file:./data/tiotec`, usuario `sa`, sin contraseña → `SELECT * FROM DISPOSITIVOS;`

### API externa

| Método | URL | Qué se usa |
| :--- | :--- | :--- |
| GET | `https://endoflife.date/api/v1/products/android/` | `result.releases[]`: `name` ("14"), `isEol` (true = sin soporte), `eolFrom` (fecha) |

La lógica que cruza el sistema operativo del dispositivo con la respuesta está en `SoporteAndroid.evaluar(...)`: de "Android 12" extrae "12", busca esa versión y devuelve *Con soporte*, *Sin soporte desde (fecha)*, *Sin información* o *Consulta solo disponible para Android*.

---

## 6. Tests unitarios

### Qué son y para qué sirven

Un test unitario prueba **una pieza de lógica aislada** y verifica automáticamente que el resultado sea el esperado. Sirven para:

- detectar si un cambio rompe algo que antes funcionaba (regresión),
- documentar cómo debe comportarse el código,
- probar casos difíciles de reproducir a mano (servidor apagado, sin internet, datos inválidos).

### Cómo están hechos: *fakes*

Los tests no usan emulador, base real ni red. En `app/src/test/.../viewmodel/FakesTioTec.kt` hay versiones falsas:

| Fake | Reemplaza a | Comportamiento |
| :--- | :--- | :--- |
| `FakeTioTecDao` | Room | Guarda en listas en memoria |
| `FakeTioTecApiService` | El microservicio | Un "servidor" en un `Map`; cuenta POST/PUT/DELETE |
| `FakeApiCaida` | El microservicio apagado | Siempre lanza error de conexión |
| `FakeEndOfLifeApi` | endoflife.date | Devuelve Android 15, 14 y 12 fijos |

Esto funciona porque el `TioTecRepository` **recibe** sus dependencias por el constructor (inyección de dependencias). En la app real recibe Room y Retrofit; en el test, los fakes.

### Estructura de un test: Arrange – Act – Assert

Test recomendado para explicar en la defensa (`DispositivosServidorTest.kt`):

```kotlin
@Test
fun registrar_guardaEnRoomYEnElServidor() = runTest(testDispatcher) {
    // Arrange: ViewModel con Room falso y servidor falso
    val vm = crearViewModel()
    advanceUntilIdle()

    // Act: el usuario llena el formulario y registra
    vm.llenarFormulario("Pixel 8", "Android 14")
    vm.registrarDispositivo()
    advanceUntilIdle()                       // espera a que terminen las corrutinas

    // Assert: quedó en Room, se hizo 1 POST y con el mismo id en ambos lados
    assertEquals(1, dao.dispositivos.size)
    assertEquals(1, api.llamadasPost)
    assertEquals(dao.dispositivos.first().id, api.servidor.keys.first())
    assertEquals(true, vm.uiState.value.servidorDispositivosOk)
}
```

- `Dispatchers.setMain(testDispatcher)`: el ViewModel usa el hilo principal de Android, que no existe en un test de JVM; se reemplaza por uno de prueba.
- `runTest` + `advanceUntilIdle()`: ejecuta las corrutinas pendientes antes de verificar.

Otros tests que vale la pena conocer:

| Test | Qué demuestra |
| :--- | :--- |
| `servidorApagado_guardaEnRoomYAvisaSinCaerse` | Manejo de errores sin conexión |
| `editar_siElServidorNoLoTiene_loCreaConPost` | El caso 404 → POST |
| `evaluar_versionSinSoporte_incluyeFecha` | Lógica pura de la API externa |
| `dosEvaluacionesIguales_noDuplicanAccionesPendientes` | La corrección del checklist |
| Backend `DispositivoServiceTest` | Usa **Mockito** (`mock(...)`, `when(...)`, `verify(...)`) en vez de un fake escrito a mano |
| Backend `DispositivoControllerTest` | Levanta Spring con **MockMvc** y prueba el CRUD completo por HTTP con H2 en memoria |

### Comandos

```powershell
# App (desde la raíz del proyecto)
.\gradlew testDebugUnitTest            # 71 tests
.\gradlew koverHtmlReportDebug         # informe: app\build\reports\kover\htmlDebug\index.html
.\gradlew koverVerifyDebug             # falla si la cobertura baja de 80%

# Backend (desde backend\)
.\mvnw.cmd test                        # 23 tests
```

En Android Studio: abre el archivo del test y presiona la **flecha verde** junto al nombre del test.

**Cobertura:** Kover mide qué líneas ejecutaron los tests. Se mide la **lógica** (ViewModel, Repository, modelos, validaciones, DTO). Se excluyen las pantallas Compose y los helpers de hardware (batería, cámara, sensores), porque necesitan un dispositivo real y se prueban con tests instrumentados (`app/src/androidTest/`). Resultado: **88% de líneas**.

---

## 7. APK release firmado

### Por qué se firma

Android **no instala** un APK sin firma. La firma demuestra quién publicó la app y garantiza que nadie la modificó. Las actualizaciones deben firmarse con **la misma clave**; si se pierde el `.jks`, no se puede actualizar la app publicada.

### Piezas

| Pieza | Valor |
| :--- | :--- |
| Keystore (`.jks`) | `app/release-tiotec.jks` (almacén de claves) |
| Alias | `tiotec_release` (nombre de la clave dentro del almacén) |
| Certificado | `CN=AppTioTec, OU=DuocUC, O=MCmovil, L=Santiago, ST=RM, C=CL` |
| Configuración | `signingConfigs { create("release") { ... } }` y `buildTypes.release.signingConfig` en `app/build.gradle.kts` |
| APK generado | `app/build/outputs/apk/release/app-release.apk` (copia en `entrega/`) |

### Pasos en Android Studio (para explicarlos en la defensa)

1. **Build → Generate Signed App Bundle or APK…**
2. Elegir **APK** → Next.
3. **Key store path:** elegir `app/release-tiotec.jks` (o **Create new…** para crear uno: ruta, contraseña, alias, validez en años y datos del certificado).
4. Ingresar la contraseña del almacén, el alias `tiotec_release` y la contraseña de la clave → Next.
5. Variante **release** → **Create**.
6. Android Studio avisa con **locate** dónde quedó el APK.

### Pasos por consola (lo que usa el proyecto)

```powershell
.\gradlew assembleRelease
# Verificar la firma (apksigner viene en el SDK; cambia 36.0.0 por la versión que tengas en build-tools)
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.0.0\apksigner.bat" verify --print-certs app\build\outputs\apk\release\app-release.apk
```

Si la firma es correcta, `apksigner` muestra `Signer #1 certificate DN: CN=AppTioTec, OU=DuocUC, ...`.

> **Ojo (posible pregunta):** en este repositorio la contraseña está escrita en `build.gradle.kts` para que el profesor pueda compilar. En un proyecto real se guarda en `keystore.properties` (que no se sube a GitHub) o en variables de entorno. El proyecto ya soporta esa opción: si existe `keystore.properties`, se usa ese archivo. Hay un ejemplo en `keystore.properties.example`.

---

## 8. Modificación en vivo (40% de la EP5)

El profesor pedirá un cambio que **afecte la app, la base de datos y el microservicio**. El cambio más probable es **agregar un campo nuevo al dispositivo**. Este ensayo ya se probó: compila y todos los tests pasan.

### Ejemplo: agregar `marca` (texto opcional)

Orden recomendado: **backend → modelo de la app → Room → DTO → ViewModel → pantalla**.

**Backend (2 archivos):**

1. `backend/.../model/Dispositivo.java`: agrega el atributo y su getter/setter (Source Action → Generate Getters and Setters).
   ```java
   private String marca;
   ```
2. `backend/.../service/DispositivoService.java`, en `actualizar(...)`, debajo de `setSistemaOperativo`:
   ```java
   existente.setMarca(datos.getMarca());
   ```
   Reinicia el servidor. Como `ddl-auto=update`, **H2 agrega la columna MARCA solo**. Compruébalo en la consola H2.

**App (7 archivos):**

3. `model/Dispositivo.kt`: `val marca: String = ""` (con valor por defecto, para no romper el código existente).
4. `data/local/DispositivoEntity.kt`: `val marca: String = ""` y agregar `marca = marca` en `toDomain()` y `marca = d.marca` en `fromDomain()`.
5. `data/local/TioTecDatabase.kt`: `version = 2` → **`version = 3`**. Cambió una tabla de Room; si no subes la versión, la app se cae al abrir.
6. `data/remote/DispositivoDto.kt`: `@SerializedName("marca") val marca: String? = null`; en `toDto()` → `marca = marca`; en `toDomain()` → `marca = marca ?: ""`.
7. `viewmodel/TioTecViewModel.kt`:
   - En `TioTecUiState`: `val marcaInput: String = "",`
   - Nueva función:
     ```kotlin
     fun onMarcaChange(texto: String) {
         _uiState.update { it.copy(marcaInput = texto) }
     }
     ```
   - En `registrarDispositivo()`: pasar `marca = state.marcaInput` a `actualizarDispositivo(...)` y a `agregarDispositivo(...)`.
   - En esas dos funciones: agregar el parámetro `marca: String = ""` y `marca = marca.trim()` al crear el `Dispositivo`.
   - En `iniciarEdicionDispositivo`: `marcaInput = dispositivo.marca,`
   - En `limpiarFormularioDispositivo`: `marcaInput = "",`
8. `ui/perfil/PerfilScreen.kt`:
   - Parámetro nuevo `onMarcaChange: (String) -> Unit = {}` en `PerfilScreen` y en `TarjetaRegistroDispositivo`, y pasarlo de una a la otra.
   - Campo nuevo antes de la foto:
     ```kotlin
     CampoTextoValidado(
         valor = state.marcaInput,
         onValorChange = onMarcaChange,
         etiqueta = "Marca (opcional)",
         icono = Icons.Default.Phone,
         error = null
     )
     ```
   - En `ItemDispositivo`, mostrarla: `"${dispositivo.marca} ${dispositivo.tipo.etiqueta} · ${dispositivo.sistemaOperativo}".trim()`
9. `Navigation.kt`, en la llamada a `PerfilScreen(...)`: `onMarcaChange = viewModel::onMarcaChange`.

**Cómo demostrar que funcionó:** registra un dispositivo con marca → recarga `localhost:8080/api/v1/dispositivos` (aparece `"marca"`) → `SELECT * FROM DISPOSITIVOS;` en H2 (columna nueva con el valor).

> Al subir la versión de Room, la base local se borra (`fallbackToDestructiveMigration`). Los dispositivos vuelven solos porque al abrir la app se descargan del servidor. Es una buena observación para mencionar.

### Otros cambios posibles y dónde tocar

| Pedido | Dónde |
| :--- | :--- |
| Cambiar una validación (ej. nombre mínimo 5) | Backend: `@Size(min = 5)` en `Dispositivo.java`. App: `MIN_CARACTERES_NOMBRE` en `ValidadorFormularios.kt` (y ajustar sus tests) |
| Nuevo endpoint (ej. filtrar por tipo) | Repository: `List<Dispositivo> findByTipo(String tipo);` → Service → Controller `@GetMapping("/tipo/{tipo}")` → en la app, `@GET("api/v1/dispositivos/tipo/{tipo}")` en `TioTecApiService` |
| Ordenar la lista por nombre | App: `ORDER BY nombre ASC` en la `@Query` de `TioTecDao`. Backend: `findAllByOrderByNombreAsc()` en el repository y usarlo en `listar()` |
| Nuevo tipo de dispositivo (ej. SMARTWATCH) | App: agregar al `enum TipoDispositivo`. Backend: no cambia (guarda el tipo como texto) |

---

## 9. Preguntas probables (respuestas cortas)

**Entorno**
- *¿Qué necesitas para ejecutar el proyecto?* Android Studio con JDK 17+ y SDK 36 para la app; JDK 21 para el backend (`.\mvnw.cmd spring-boot:run`, Maven viene incluido con el *wrapper*). Primero se levanta el backend y después la app en el emulador.
- *¿Por qué `10.0.2.2`?* Es la dirección con la que el emulador llega al PC. En un celular físico se usa la IP del PC en la misma red Wi-Fi (se cambia en Cuidado → pestaña Spring Boot API).

**MVVM**
- *¿Qué hace el ViewModel?* Mantiene el estado de la pantalla en un `StateFlow`, valida y coordina el repositorio. La pantalla no tiene lógica.
- *¿Qué pasa al rotar el teléfono?* El ViewModel sobrevive, así que el estado se mantiene.
- *¿Para qué sirve el Repository?* Es el único lugar que sabe de dónde vienen los datos (Room, servidor, API). El ViewModel no conoce Retrofit ni Room directamente.

**Retrofit / APIs**
- *¿Qué es RetrofitService?* La interfaz `TioTecApiService` con un método por endpoint. Retrofit genera la implementación que hace las peticiones HTTP.
- *¿Diferencia entre API externa y microservicio?* El microservicio es nuestro (CRUD, guarda en H2). La API externa es de un tercero, solo se consulta y no guarda nada de la app.
- *¿Qué pasa si el servidor está apagado?* Se guarda en Room y se avisa en la tarjeta del servidor. La app no se cae porque todas las llamadas devuelven `Result`.

**Microservicio**
- *Entidades:* `Dispositivo` (id, nombre, tipo, sistemaOperativo) y `Diagnostico` (datos de batería, almacenamiento, RAM, sensores y nivel de riesgo).
- *Persistencia:* Spring Data JPA con H2 en archivo (`backend/data/tiotec.mv.db`). Las tablas las crea Hibernate con `ddl-auto=update`.
- *¿Por qué el id es String?* Para usar el mismo id que la app guarda en Room y reconocer el mismo dispositivo en ambos lados.
- *¿Qué hace `@RestControllerAdvice`?* Captura excepciones de todos los controladores y devuelve 400/404 con un JSON claro.

**Tests**
- *¿Por qué fakes?* Para probar la lógica sin red, sin base real y sin emulador: los tests son rápidos y siempre dan el mismo resultado.
- *¿Qué mide Kover?* El porcentaje de líneas de lógica que ejecutan los tests (88%).
- *¿Unitario vs instrumentado?* El unitario corre en la JVM del PC (`src/test`). El instrumentado corre en el emulador y puede probar la UI (`src/androidTest`).

**APK**
- *¿Qué es el `.jks`?* El almacén con la clave privada que firma la app.
- *¿Diferencia entre debug y release?* Debug se firma con una clave genérica de Android Studio, solo para desarrollo. Release se firma con nuestra clave y es la que se publica.

---

## 10. Limitaciones conocidas (mejor decirlas tú antes que te las encuentren)

- La sincronización es simple: si eliminas un dispositivo con el servidor apagado, vuelve a aparecer en la próxima sincronización porque sigue en el servidor.
- La foto del dispositivo no se sube al servidor (es una ruta local del teléfono).
- endoflife.date marca su API v1 como beta; si cambia el formato, la tarjeta mostrará un error, pero el resto de la app sigue funcionando.
- La contraseña del keystore está en `build.gradle.kts` (ver sección 7).

---

## 11. Antes de la defensa (checklist)

- [ ] `.\mvnw.cmd spring-boot:run` en `backend\` → `Started BackendApplication`.
- [ ] App en el emulador → Perfil → tarjeta del servidor dice *Conectado*.
- [ ] Crear, editar y eliminar un dispositivo y verlo en `localhost:8080/api/v1/dispositivos` y en la consola H2.
- [ ] Tarjeta de soporte Android carga versiones (requiere internet).
- [ ] `.\gradlew testDebugUnitTest` y `.\mvnw.cmd test` en verde.
- [ ] Ensayar la sección 8 una vez y deshacerla (`git checkout -- .`).
- [ ] Tener abierto el test `registrar_guardaEnRoomYEnElServidor` para explicarlo.
