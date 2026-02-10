# ========================================
# Script para cargar variables de entorno desde .env
# Uso: .\load-env.ps1
# ========================================

Write-Host "Cargando variables de entorno desde .env..." -ForegroundColor Cyan

# Verificar si existe el archivo .env
if (-not (Test-Path ".env")) {
    Write-Host "Error: Archivo .env no encontrado" -ForegroundColor Red
    Write-Host "Ejecuta: Copy-Item .env.example .env" -ForegroundColor Yellow
    exit 1
}

# Contador de variables cargadas
$count = 0

# Leer y procesar el archivo .env
Get-Content .env | ForEach-Object {
    $line = $_.Trim()
    
    # Ignorar lineas vacias y comentarios
    if ($line -and -not $line.StartsWith("#")) {
        if ($line -match "^([^=]+)=(.*)$") {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            
            # Establecer variable de entorno
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
            $count++
            
            # Mostrar variable cargada (ocultar passwords)
            if ($name -like "*PASSWORD*" -or $name -like "*SECRET*") {
                Write-Host "  OK $name = ********" -ForegroundColor Green
            } else {
                Write-Host "  OK $name = $value" -ForegroundColor Green
            }
        }
    }
}

Write-Host ""
Write-Host "$count variables de entorno cargadas exitosamente" -ForegroundColor Green
Write-Host ""
Write-Host "Proximo paso: .\gradlew.bat bootRun" -ForegroundColor Cyan
