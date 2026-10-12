# 04 · Planificación Colaborativa en GitHub Projects y Pipeline CI/CD (Guía 7)

> 📌 En este proyecto utilizamos **GitHub Projects + GitHub Issues** automatizados mediante **GitHub Actions CI**, reemplazando tableros externos por trazabilidad integrada directamente con el código fuente.

---

## 1. Automatización CI/CD Configurada en `.github/`

El repositorio cuenta con dos flujos de trabajo (*workflows*) automáticos en [`.github/workflows/`](../.github/workflows):

| Workflow | Archivo | ¿Qué hace automáticamente? |
| :--- | :--- | :--- |
| **1. Sincronizador de Backlog (GitHub Projects)** | [`.github/workflows/seed-project-backlog.yml`](../.github/workflows/seed-project-backlog.yml) | Crea las etiquetas semánticas (`estado: finalizado`, `estado: en-curso`, `estado: por-hacer`, `prioridad: alta`, capas MVVM) y genera automáticamente las **10 tareas oficiales de la Unidad 2** como *GitHub Issues*, cerrando las ya completadas para que el tablero Kanban refleje el estado real. |
| **2. Android CI · Build & Test** | [`.github/workflows/android-ci.yml`](../.github/workflows/android-ci.yml) | En cada `git push` o `Pull Request` a `main`, levanta una máquina Ubuntu con JDK 17, compila todo el código Kotlin/Compose (`compileDebugKotlin`), ejecuta los tests unitarios (`testDebugUnitTest`) y genera el instalador **`AppTioTec-debug-apk`** descargable. |
| **3. Plantilla de Tareas** | [`.github/ISSUE_TEMPLATE/tarea-tiotec.yml`](../.github/ISSUE_TEMPLATE/tarea-tiotec.yml) | Formulario interactivo para crear nuevas tareas seleccionando la Guía del AVA y la capa MVVM correspondiente. |

---

## 2. Cómo Conectar los Issues al Tablero Visual de GitHub Projects (En 30 segundos)

Una vez que el workflow `seed-project-backlog.yml` termina de ejecutarse en la pestaña **Actions** de GitHub:

1. En el repositorio [`NezerkC/AppTioTec`](https://github.com/NezerkC/AppTioTec), haz clic en la pestaña superior **`Projects`** $\rightarrow$ **`New project`**.
2. Selecciona la plantilla **`Board` (Tablero Kanban)** y nómbralo **`AppTioTec · Unidad 2`**.
3. En la parte inferior de la columna **Todo**, haz clic en **`+ Add item` $\rightarrow$ `Add item from repository`**.
4. Selecciona el repositorio `AppTioTec`, marca los 10 Issues generados automáticamente y presiona **`Add selected items`**:
   * Las **Tareas 01 a 03** (ya cerradas) pasarán automáticamente a la columna **`Done` (Finalizado)**.
   * Las **Tareas 04 y 05** van en **`In Progress` (En curso)**.
   * Las **Tareas 06 a 10** quedan en **`Todo` (Por hacer)**.

---

## 3. Estado del Backlog en GitHub Issues / Projects

### ✅ Columna 1: Finalizado (`Done` · `estado: finalizado`)
* **Issue #1 · `[Tarea 01 · Guía 7]`:** Levantamiento de Requerimientos y Reglas de Negocio (Caso TíoTec).
* **Issue #2 · `[Tarea 02 · Guía 7]`:** Configuración Inicial del Proyecto Android con Jetpack Compose y MVVM.
* **Issue #3 · `[Tarea 03 · Guía 7]`:** Implementación de Modelos de Dominio, Repositorio y `TioTecViewModel`.

### 🔄 Columna 2: En Curso (`In Progress` · `estado: en-curso`)
* **Issue #4 · `[Tarea 04 · Guía 8 y 9]`:** Construcción Visual del Dashboard Principal y Adaptabilidad Material 3.
* **Issue #5 · `[Tarea 05 · Guía 10]`:** Flujo de Navegación Estructurada (`Scaffold`, `TopAppBar` y `NavigationBar`).

### 📋 Columna 3: Por Hacer (`Todo` · `estado: por-hacer`)
* **Issue #6 · `[Tarea 06 · Guía 11]`:** Pantalla de Perfil Ficticio e Inventario de Dispositivos con Validación Reactiva.
* **Issue #7 · `[Tarea 07 · Guía 11]`:** Pantalla Interactiva del Cuestionario Preventivo (4 Pilares de Seguridad).
* **Issue #8 · `[Tarea 08 · Guía 12]`:** Semáforo de Riesgo Animado y Persistencia de Perfil con DataStore.
* **Issue #9 · `[Tarea 09 · Guía 13]`:** Integración de Recursos Nativos: Cámara para Dispositivos y Notificaciones.
* **Issue #10 · `[Tarea 10 · Actividad 2.5]`:** Persistencia Relacional Local con Room Database (SQLite).

---

## 4. Cómo Cerrar Tareas Automáticamente desde un Commit

Cuando tú o tu compañero terminen una tarea, pueden cerrarla y moverla automáticamente a la columna **Done** en GitHub Projects escribiendo el número del Issue en el mensaje del commit:

```bash
git commit -m "feat(ui): implementar dashboard preventivo Material 3 (closes #4)"
git push origin main
```
Al subir el commit, GitHub cerrará el **Issue #4** y moverá su tarjeta a **Done** automáticamente.
