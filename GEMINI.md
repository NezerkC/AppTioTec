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
- Formato estricto de Conventional Commits (sin menciones de autoría de IA):
  - `feat(<scope>): <mensaje>`
  - `fix(<scope>): <mensaje>`
  - `refactor(<scope>): <mensaje>`
  - `test(<scope>): <mensaje>`
  - `docs(<scope>): <mensaje>`
