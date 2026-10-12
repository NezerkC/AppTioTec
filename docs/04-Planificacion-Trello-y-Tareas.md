# 04 · Planificación Colaborativa y Tablero Trello (Guía 7 - Parte 3)

> 📌 Este documento registra la planificación de tareas semanales del equipo para el proyecto **AppTioTec**, cumpliendo con los puntos 4 y 5 de la **Guía 7**.

---

## Estructura del Tablero Trello

### ✅ Lista 1: Finalizado (Done)
* **[Tarea 01]** Análisis del caso oficial *TíoTec Preventivo* (`Caso_005V_TioTec_Preventivo.pdf`) y levantamiento de requerimientos funcionales (`RF-01` a `RF-06`) y reglas de negocio (`RN-01` a `RN-04`).
* **[Tarea 02]** Creación del proyecto base en Android Studio con Jetpack Compose y namespace `cl.duoc.tiotec`.
* **[Tarea 03]** Estructuración de paquetes bajo el patrón **MVVM** (`model/`, `repository/`, `viewmodel/`, `ui/`, `theme/`).
* **[Tarea 04]** Implementación de modelos de dominio (`Usuario`, `Dispositivo`, `EvaluacionSeguridad`, `AccionChecklist`, `NivelRiesgo`).
* **[Tarea 05]** Implementación de `TioTecRepository` y `TioTecViewModel` con `StateFlow`.
* **[Tarea 06]** Creación del repositorio en GitHub y primer commit oficial: `"Inicio de proyecto + estructura base MVVM"`.

---

### 🔄 Lista 2: En Curso (In Progress)
* **[Tarea 07 · Guía 8 y 9]** Construcción visual de la pantalla principal (*Dashboard de Seguridad*) y sistema de diseño adaptable con **Material Design 3** (`Color.kt`, `Theme.kt`).
* **[Tarea 08 · Guía 10]** Implementación del flujo de navegación estructurada (`Scaffold`, `TopAppBar`, `NavigationBar`) entre las secciones: *Inicio*, *Evaluación*, *Checklist* y *Perfil/Dispositivos*.

---

### 📋 Lista 3: Por Hacer (To Do)
* **[Tarea 09 · Guía 11]** Pantalla de registro de Perfil y Dispositivos con formularios validados en tiempo real (`OutlinedTextField`, mensajes de error reactivos).
* **[Tarea 10 · Guía 11]** Pantalla interactiva del Cuestionario Preventivo (4 pilares) y vista de resultados con semáforo de riesgo.
* **[Tarea 11 · Guía 12]** Incorporación de animaciones declarativas (`AnimatedVisibility`, `animateColorAsState`) en el semáforo de diagnóstico y tarjetas del checklist.
* **[Tarea 12 · Guía 13]** Integración de recursos nativos: captura de fotografía para dispositivos de prueba con la **Cámara** y permisos en tiempo de ejecución.
* **[Tarea 13 · Actividad 2.5]** Integración de persistencia local avanzada con **Room Database (SQLite)** (`@Entity`, `@Dao`, `@Database`) y **Preferences DataStore**.

---

## Distribución Sugerida para Trabajo en Pareja

| Integrante | Responsabilidades Principales |
| :--- | :--- |
| **Integrante 1 (`NezerkC`)** | • Arquitectura base MVVM, `TioTecViewModel` y lógica de cálculo de riesgo (`RN-01` y `RN-02`).<br>• Flujo del Cuestionario Preventivo, Semáforo animado y Persistencia con Room/SQLite. |
| **Integrante 2 (Compañero)** | • Pantallas de Perfil Ficticio e Inventario de Dispositivos con validación de formularios.<br>• Vista interactiva del Checklist de Acciones e integración del recurso nativo Cámara. |
