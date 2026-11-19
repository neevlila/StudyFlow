# StudyFlow Runner Script for PowerShell
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "             Starting StudyFlow Dashboard               " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

$java = "C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
if (-not (Test-Path $java)) {
    $java = "java"
}

$jar = "target\studyflow.jar"
if (-not (Test-Path $jar)) {
    $jar = "target\studyflow-1.0.0.jar"
}

Write-Host "Using Java: $java" -ForegroundColor Gray
Write-Host "Opening StudyFlow on http://localhost:8080" -ForegroundColor Green
Write-Host "Press Ctrl+C at any time to stop the server." -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

& $java -jar $jar
