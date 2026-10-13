# ==============================================================================
# AppTioTec - Asistente Local de Avance y Tareas (Unidad 2 - DSY1105)
# Uso en terminal de Android Studio: .\progreso.ps1
# ==============================================================================

$ErrorActionPreference = "SilentlyContinue"
$srcDir = Join-Path $PSScriptRoot "app\src\main\java\cl\duoc\tiotec"
$uiDir = Join-Path $srcDir "ui"
$docsDir = Join-Path $PSScriptRoot "docs"
$buildGradlePath = Join-Path $PSScriptRoot "app\build.gradle.kts"

$ktFiles = Get-ChildItem -Path $srcDir -Recurse -Filter "*.kt"
$allCode = ($ktFiles | ForEach-Object { Get-Content $_.FullName -Raw -Encoding UTF8 }) -join "`n"

$uiFiles = Get-ChildItem -Path $uiDir -Recurse -Filter "*.kt"
$navFile = Join-Path $srcDir "Navigation.kt"
$uiCode = (($uiFiles | ForEach-Object { Get-Content $_.FullName -Raw -Encoding UTF8 }) -join "`n") + "`n" + (Get-Content $navFile -Raw -Encoding UTF8)

$buildGradle = if (Test-Path $buildGradlePath) { Get-Content $buildGradlePath -Raw -Encoding UTF8 } else { "" }

$tasks = @(
    @{
        Id = "Tarea 01 (Guia 7)"
        Title = "Levantamiento de Requerimientos y Reglas de Negocio (TioTec)"
        Done = (Test-Path "$docsDir\01-Caso-Oficial-TioTec.md") -and (Test-Path "$docsDir\02-Requerimientos-y-Reglas-Negocio.md")
        Guide = "Documentar el caso oficial TioTec y las reglas RN-01 a RN-04 en docs/."
    },
    @{
        Id = "Tarea 02 (Guia 7)"
        Title = "Configuracion Inicial Android Studio, Compose y Carpetas MVVM"
        Done = (Test-Path "$srcDir\model") -and (Test-Path "$srcDir\viewmodel") -and (Test-Path "$srcDir\repository")
        Guide = "Crear los paquetes model/, repository/, viewmodel/ y ui/ dentro de cl.duoc.tiotec."
    },
    @{
        Id = "Tarea 03 (Guia 7)"
        Title = "Modelos de Dominio, Repositorio y TioTecViewModel (StateFlow)"
        Done = $allCode.Contains("class TioTecViewModel") -and $allCode.Contains("StateFlow<TioTecUiState>")
        Guide = "Implementar TioTecViewModel con MutableStateFlow y reglas RN-01 / RN-02."
    },
    @{
        Id = "Tarea 04 (Guia 8-9)"
        Title = "Dashboard Principal Material 3 y Semaforo de Riesgo"
        Done = $uiCode.Contains("NivelRiesgo") -and $uiCode.Contains("Card(")
        Guide = "En ui/main/MainScreen.kt, conectar TioTecViewModel y mostrar una Card Material 3 con el ultimo Nivel de Riesgo (Bajo/Medio/Alto), total de dispositivos y acciones pendientes."
    },
    @{
        Id = "Tarea 05 (Guia 10)"
        Title = "Navegacion Estructurada (Scaffold, TopAppBar y NavigationBar)"
        Done = $uiCode.Contains("Scaffold(") -and ($uiCode.Contains("NavigationBar(") -or $uiCode.Contains("NavigationBarItem(")) -and $uiCode.Contains("TopAppBar(")
        Guide = "Implementar Scaffold con TopAppBar institucional y NavigationBar inferior con 4 secciones: Inicio, Evaluar, Checklist y Perfil/Equipos."
    },
    @{
        Id = "Tarea 06 (Guia 11)"
        Title = "Formularios Validados de Perfil Ficticio y Dispositivos"
        Done = $uiCode.Contains("OutlinedTextField(") -and $uiCode.Contains("agregarDispositivo(") -and $uiCode.Contains("actualizarPerfil(")
        Guide = "Crear pantalla de Perfil y Dispositivos con OutlinedTextField y mostrar state.errorFormulario en tiempo real cuando falten datos (RN-03)."
    },
    @{
        Id = "Tarea 07 (Guia 11)"
        Title = "Pantalla Interactiva del Cuestionario Preventivo (4 Pilares)"
        Done = $uiCode.Contains("seleccionarRespuesta(") -and $uiCode.Contains("finalizarEvaluacion(")
        Guide = "Renderizar la lista state.preguntas en la UI con sus opciones y boton 'Finalizar Evaluacion' que invoque viewModel.finalizarEvaluacion()."
    },
    @{
        Id = "Tarea 08 (Guia 12)"
        Title = "Animaciones Declarativas y Persistencia con DataStore"
        Done = ($uiCode.Contains("AnimatedVisibility") -or $uiCode.Contains("animateColorAsState")) -and ($allCode.Contains("DataStore") -or $buildGradle.Contains("datastore"))
        Guide = "Agregar AnimatedVisibility / animateColorAsState al diagnostico y configurar Preferences DataStore para guardar el perfil ficticio."
    },
    @{
        Id = "Tarea 09 (Guia 13)"
        Title = "Recursos Nativos: Camara para Dispositivos y Notificaciones"
        Done = $uiCode.Contains("rememberLauncherForActivityResult") -and ($uiCode.Contains("TakePicturePreview") -or $uiCode.Contains("TakePicture") -or $uiCode.Contains("RequestPermission"))
        Guide = "Integrar rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) para capturar foto del dispositivo y alerta con NotificationCompat."
    },
    @{
        Id = "Tarea 10 (Act. 2.5)"
        Title = "Persistencia Local Relacional con Room Database (SQLite)"
        Done = $allCode.Contains("@Entity") -and $allCode.Contains("@Dao") -and $allCode.Contains("@Database")
        Guide = "Configurar Room (@Entity, @Dao, @Database) para almacenar evaluaciones, dispositivos y checklist en SQLite."
    }
)

$doneCount = ($tasks | Where-Object { $_.Done }).Count
$total = $tasks.Count
$pct = [math]::Round(($doneCount / $total) * 100)

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  ESTADO AUTOMATICO DEL PROYECTO APPTIOTEC: $pct% ($doneCount/$total Tareas)" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

$nextTask = $null
foreach ($t in $tasks) {
    if ($t.Done) {
        Write-Host "  [LISTO]     $($t.Id): $($t.Title)" -ForegroundColor Green
    } elseif ($null -eq $nextTask) {
        $nextTask = $t
        Write-Host "  [EN CURSO]  $($t.Id): $($t.Title)" -ForegroundColor Yellow
    } else {
        Write-Host "  [PENDIENTE] $($t.Id): $($t.Title)" -ForegroundColor DarkGray
    }
}

Write-Host ""
if ($null -ne $nextTask) {
    Write-Host "----------------------------------------------------------------------" -ForegroundColor Yellow
    Write-Host "  QUE TOCA HACER AHORA ($($nextTask.Id)):" -ForegroundColor Yellow
    Write-Host "  $($nextTask.Guide)" -ForegroundColor White
    Write-Host ""
    Write-Host "  Nota: Al hacer 'git push origin main', GitHub Actions cerrara la tarea" -ForegroundColor Gray
    Write-Host "        automaticamente y asignara el autor del push en GitHub Projects." -ForegroundColor Gray
    Write-Host "----------------------------------------------------------------------" -ForegroundColor Yellow
} else {
    Write-Host "  [EXITO] Todas las tareas de la Unidad 2 estan completadas!" -ForegroundColor Green
}

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  QUIEN HIZO QUE (HISTORIAL DE APORTES EN GIT)" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  Resumen de commits por integrante:" -ForegroundColor White
$authors = git -C $PSScriptRoot shortlog -sn --all
foreach ($line in $authors) {
    Write-Host "   -> $($line.Trim()) commits" -ForegroundColor Green
}
Write-Host ""
Write-Host "  Ultimos 5 avances registrados:" -ForegroundColor White
$recentCommits = git -C $PSScriptRoot log -n 5 --pretty=format:"   [%h] %ad | %an: %s" --date=short
foreach ($c in $recentCommits) {
    Write-Host $c -ForegroundColor Gray
}
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

