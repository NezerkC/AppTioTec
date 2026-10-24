# Reglas de Desarrollo y Control de Versiones (Git Workflow)

## 1. Estrategia de Ramas (Branching)
- **Prohibido trabajar directo en `main`**: Todo trabajo asociado a una issue debe realizarse en una rama dedicada con nomenclatura estándar:
  - `feature/<id-issue>-<descripcion-corta>` (ej: `feature/issue-8-datastore-animaciones`)
  - `fix/<id-issue>-<descripcion-corta>`
- La rama se integra a `main` únicamente tras validación local de compilación/tests y revisión.

## 2. Backlog Explícito por Issue
- Antes de iniciar la implementación de una issue:
  1. Analizar los criterios de aceptación y requerimientos oficiales.
  2. Desglosar las acciones en un backlog/checklist técnico numerado.
  3. Actualizar el estado de cada ítem a medida que se completa.

## 3. Commits Atómicos
- Cada commit debe representar un cambio único, coherente y autocontenido.
- Todo commit debe compilar y no romper el estado de la aplicación.
- **Idioma de los commits: 100% en español** (descripción clara y en tiempo presente/imperativo).
- Formato estricto de Conventional Commits (sin menciones de autoría de IA):
  - `feat(<scope>): <mensaje descriptivo en español>` (ej: `feat(data): implementar PreferenciasDataStore`)
  - `fix(<scope>): <mensaje descriptivo en español>` (ej: `fix(ui): corregir contraste en el semáforo`)
  - `refactor(<scope>): <mensaje descriptivo en español>`
  - `test(<scope>): <mensaje descriptivo en español>`
  - `docs(<scope>): <mensaje descriptivo en español>`
  - `build(<scope>): <mensaje descriptivo en español>` (ej: `build(deps): agregar dependencia datastore preferences`)
  - `chore(<scope>): <mensaje descriptivo en español>`

## 4. Pushes Atómicos e Incrementales
- **Prohibido acumular múltiples tareas o issues para un mega-push**:
  - Cada rama de feature (`feature/<id-issue>-...`) debe empujarse al repositorio remoto de forma independiente:
    ```bash
    git push origin feature/<id-issue>-<nombre>
    ```
  - Una vez validado el merge a `main`, el avance de esa issue específica se publica inmediatamente antes de comenzar a trabajar en la siguiente issue:
    ```bash
    git push origin main
    ```
- Esto garantiza que cada ejecución de GitHub Actions, cada cierre automático de issue y cada actualización en GitHub Projects refleje fielmente el avance paso a paso del ciclo de vida del software.
