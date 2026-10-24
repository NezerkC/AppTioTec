# Estándar de Desarrollo: Git y GitHub CLI (`gh`)

## 1. Autenticación y Configuración de Herramientas
- **Herramienta principal para GitHub**: Se prioriza el uso de GitHub CLI (`gh`) para interactuar con la plataforma (issues, PRs, proyectos y autenticación).
- **Helper de credenciales Git**: La autenticación de `git` se enlaza con `gh`:
  ```bash
  gh auth login -w -p https
  gh auth setup-git
  ```

## 2. Estrategia de Ramas (Branching)
- **Prohibido trabajar directo en `main`**: Todo trabajo asociado a una issue debe realizarse en una rama dedicada:
  - `feature/<id-issue>-<descripcion-corta>` (ej: `feature/issue-8-datastore-animaciones`)
  - `fix/<id-issue>-<descripcion-corta>`
- Las ramas se integran a `main` únicamente tras validación local de compilación y pruebas (`assembleDebug`, `testDebugUnitTest`).

## 3. Backlog Técnico por Issue
- Antes de iniciar la implementación de una issue:
  1. Inspeccionar la issue con `gh issue view <id>`.
  2. Desglosar las tareas en un backlog numerado.
  3. Validar los criterios de aceptación al completar cada paso.

## 4. Commits Atómicos (100% en Español)
- Cada commit representa un cambio único, funcional y coherente que compila.
- Formato estricto Conventional Commits en español:
  - `feat(<scope>): <mensaje descriptivo en español>`
  - `fix(<scope>): <mensaje descriptivo en español>`
  - `refactor(<scope>): <mensaje descriptivo en español>`
  - `test(<scope>): <mensaje descriptivo en español>`
  - `docs(<scope>): <mensaje descriptivo en español>`
  - `build(<scope>): <mensaje descriptivo en español>`
  - `chore(<scope>): <mensaje descriptivo en español>`
- Sin menciones de autoría de IA ni "Co-Authored-By".

## 5. Pushes y Pull Requests Atómicos
- **Prohibido acumular múltiples tareas para un mega-push**:
  - Cada rama de feature se sube de forma individual:
    ```bash
    git push origin <nombre-de-rama>
    ```
  - Se puede crear y fusionar el Pull Request vía `gh`:
    ```bash
    gh pr create --fill
    gh pr merge --merge --delete-branch
    ```
  - Una vez validado en `main`, el avance se sincroniza inmediatamente:
    ```bash
    git push origin main
    ```
