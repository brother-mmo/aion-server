@echo off
setlocal EnableExtensions

cd /d "%~dp0"

echo Stopping local Java server processes...
echo Closing local server console windows...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$patterns = @('*Aion Login Server*', '*Aion Game Server*', '*Aion Chat Server*'); Get-Process cmd -ErrorAction SilentlyContinue | Where-Object { $title = $_.MainWindowTitle; foreach ($pattern in $patterns) { if ($title -like $pattern) { return $true } }; return $false } | Stop-Process -Force -ErrorAction SilentlyContinue"

echo Stopping local MariaDB container...
docker compose -f docker-compose.local.yml stop >nul 2>&1

echo Local stack stopped.
exit /b 0
