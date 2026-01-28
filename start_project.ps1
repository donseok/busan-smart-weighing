$ErrorActionPreference = "Stop"

Write-Host "Starting Busan Smart Weighing System..." -ForegroundColor Cyan

$scriptPath = $PSScriptRoot

# Backend
$backendPath = Join-Path $scriptPath "backend"
if (Test-Path $backendPath) {
    Write-Host "Launching Backend (Gradle)..." -ForegroundColor Green
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k cd /d `"$backendPath`" && gradlew.bat bootRun"
} else {
    Write-Error "Backend directory not found at $backendPath"
}

# Frontend
$frontendPath = Join-Path $scriptPath "frontend"
if (Test-Path $frontendPath) {
    Write-Host "Launching Frontend (Vite)..." -ForegroundColor Green
    # Ensure dependencies are installed if node_modules doesn't exist
    if (-not (Test-Path (Join-Path $frontendPath "node_modules"))) {
        Write-Host "Installing frontend dependencies..." -ForegroundColor Yellow
        Start-Process -FilePath "cmd.exe" -ArgumentList "/c cd /d `"$frontendPath`" && npm install" -Wait
    }
    
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k cd /d `"$frontendPath`" && npm run dev"
} else {
    Write-Error "Frontend directory not found at $frontendPath"
}

# Explain what just happened
Write-Host "Backend and Frontend processes have been started in new windows." -ForegroundColor Yellow
Write-Host "Please wait for them to initialize..." -ForegroundColor Yellow
Write-Host "Backend usually listens on port 8080."
Write-Host "Frontend usually listens on port 3000."

Start-Sleep -Seconds 3

# Open Browser
Write-Host "Opening Browser..." -ForegroundColor Cyan
Start-Process "http://localhost:3000"

Write-Host "Done." -ForegroundColor Cyan
