# 01 · Caso Oficial Asignado: TíoTec Preventivo

> 📄 **Documento original en PDF:** [Ver Caso_005V_TioTec_Preventivo.pdf](./pdfs/Caso_005V_TioTec_Preventivo.pdf)

---

## 1. Identificación del Caso

| Antecedente | Información para el caso |
| :--- | :--- |
| **Título breve del desafío** | TíoTec Preventivo: evaluación y orientación para seguridad digital |
| **Organización** | MCmovil SpA (Empresa de servicios y consultoría en TI) |
| **Rubro o ámbito** | Soporte técnico, reparación, optimización, consultoría y capacitación TI |
| **Sede / Coordinador(a)** | CITT Sede Alameda – Ronald Barros Barriga |
| **Asignatura** | DSY1105 · Desarrollo de Aplicaciones Móviles (Sección 005V) |

---

## 2. Descripción del Caso

### 2.1 Contexto de la Organización
**MCmovil SpA** es una pyme tecnológica con experiencia en soporte, reparación, optimización, asistencia remota y capacitación. A partir de esa experiencia impulsa **TíoTec** como una iniciativa de prevención y educación tecnológica. Para este desafío, TíoTec se acota a un **MVP móvil Android** orientado a personas no especialistas, con el propósito de ayudarles a evaluar hábitos básicos de seguridad digital, identificar riesgos y mantener un plan preventivo de acciones.

### 2.2 Situación Actual
Actualmente, las personas no especialistas buscan orientación preventiva en videos, redes sociales, sitios web o recomendaciones informales, pero no cuentan con un proceso único y periódico que les permita revisar el estado de medidas básicas como **respaldos, actualizaciones, autenticación reforzada y recuperación de cuentas**. La información se encuentra dispersa y no existe un registro simple de qué acciones ya fueron realizadas, cuáles están pendientes o cuándo deben revisarse nuevamente.

### 2.3 Necesidad o Problema
Disponer de un MVP móvil Android que permita organizar y dar seguimiento a la prevención digital antes de que ocurra un incidente. El MVP deberá trabajar con **perfiles y dispositivos ficticios**, entregar resultados preventivos comprensibles y permitir mantener un historial básico de evaluaciones y acciones, **sin registrar incidentes reales ni datos sensibles**.

### 2.4 Mejora Esperada
Mejorar la capacidad preventiva de usuarios no especialistas mediante una experiencia móvil clara y guiada que permita identificar brechas básicas de seguridad, priorizar acciones y mantener seguimiento de su avance.

---

## 3. Información Técnica para el Desarrollo

* **Perfiles de usuario:**
  * **Usuario final:** Crea un perfil ficticio, registra dispositivos de prueba, responde evaluaciones preventivas, consulta resultados y recomendaciones, marca acciones como pendientes o completadas y revisa su historial.
  * **Administrador de contenidos:** Gestiona categorías, preguntas, recomendaciones y material educativo.
  * **Supervisor / referente técnico:** Revisa contenidos y criterios de orientación.
* **Condiciones de uso y stack exigido:**
  * Lenguaje **Kotlin**, **Android Studio**, **Jetpack Compose**, **Material Design 3** y arquitectura **MVVM**.
  * Almacenamiento local en **Room / SQLite** y **DataStore**.
  * Uso de **notificaciones** para recordatorios preventivos y **cámara** opcional para identificar dispositivos o adjuntar evidencia ficticia.
* **Límites y restricciones:**
  * No se registrarán incidentes reales, datos sensibles, información de menores ni antecedentes personales.
  * Marketplace, pagos, videollamadas, geolocalización operativa y asignación real de especialistas quedan fuera del alcance obligatorio.
