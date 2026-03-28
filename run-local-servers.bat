@echo off
setlocal EnableExtensions

cd /d "%~dp0"

set "ROOT=%CD%"
set "LOGIN_TITLE=Aion Login Server"
set "GAME_TITLE=Aion Game Server"
set "LOGIN_MAIN=com.aionemu.loginserver.LoginServer"
set "GAME_MAIN=com.aionemu.gameserver.GameServer"
set "LOGIN_ZIP=%ROOT%\login-server\target\login-server.zip"
set "GAME_ZIP=%ROOT%\game-server\target\game-server.zip"
set "RUNTIME_ROOT=%ROOT%\.local\runtime"
set "LOGIN_DIR=%RUNTIME_ROOT%\login-server"
set "GAME_DIR=%RUNTIME_ROOT%\game-server"

if exist "C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin" (
	set "PATH=C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin;%PATH%"
)

where java >nul 2>&1
if errorlevel 1 (
	echo Could not find java in PATH.
	echo Install JDK 25 or reopen the terminal after installation.
	pause
	exit /b 1
)

docker compose version >nul 2>&1
if errorlevel 1 (
	echo Docker Compose is not available. Start Docker Desktop and try again.
	pause
	exit /b 1
)

echo Ensuring local MariaDB container is running...
docker compose -f docker-compose.local.yml up -d
if errorlevel 1 (
	echo Failed to start Docker services.
	echo Try running this script from an elevated terminal or with "Run as administrator".
	pause
	exit /b 1
)

echo Waiting for database health check...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$deadline = (Get-Date).AddMinutes(2); do { $status = docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' aion-local-db 2>$null; if ($status -eq 'healthy') { exit 0 }; Start-Sleep -Seconds 2 } while ((Get-Date) -lt $deadline); exit 1"
if errorlevel 1 (
	echo Database did not become healthy in time.
	pause
	exit /b 1
)

if not exist "%LOGIN_ZIP%" goto build
if not exist "%GAME_ZIP%" goto build
goto prepare

:build
echo Missing packaged server archives. Building with Maven...
if exist "C:\ProgramData\chocolatey\bin\mvn.cmd" (
	call "C:\ProgramData\chocolatey\bin\mvn.cmd" package
) else (
	call mvn package
)
if errorlevel 1 (
	echo Maven build failed.
	pause
	exit /b 1
)

:prepare
echo Preparing runtime folders...
if not exist "%RUNTIME_ROOT%" mkdir "%RUNTIME_ROOT%"

powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference = 'Stop'; $dirs = @('%LOGIN_DIR%', '%GAME_DIR%'); foreach ($dir in $dirs) { if (Test-Path $dir) { Remove-Item -LiteralPath $dir -Recurse -Force } }; Expand-Archive -LiteralPath '%LOGIN_ZIP%' -DestinationPath '%RUNTIME_ROOT%' -Force; Expand-Archive -LiteralPath '%GAME_ZIP%' -DestinationPath '%RUNTIME_ROOT%' -Force"
if errorlevel 1 (
	echo Failed to extract packaged servers.
	pause
	exit /b 1
)

if exist "%ROOT%\login-server\config\myls.properties" copy /y "%ROOT%\login-server\config\myls.properties" "%LOGIN_DIR%\config\myls.properties" >nul
if exist "%ROOT%\game-server\config\mygs.properties" copy /y "%ROOT%\game-server\config\mygs.properties" "%GAME_DIR%\config\mygs.properties" >nul
if exist "%ROOT%\chat-server\config\mycs.properties" if exist "%RUNTIME_ROOT%\chat-server\config" copy /y "%ROOT%\chat-server\config\mycs.properties" "%RUNTIME_ROOT%\chat-server\config\mycs.properties" >nul

echo.
echo Starting login server...
start "%LOGIN_TITLE%" "%ComSpec%" /k "cd /d ""%LOGIN_DIR%"" && call start.bat"

echo Waiting for login server bootstrap...
timeout /t 6 /nobreak >nul

echo Starting game server...
start "%GAME_TITLE%" "%ComSpec%" /k "cd /d ""%GAME_DIR%"" && call start.bat"

echo.
echo Local stack started.
echo - DB: docker compose -f docker-compose.local.yml ps
echo - Login window title: %LOGIN_TITLE%
echo - Game window title: %GAME_TITLE%
echo - Stop command: stop-local-servers.bat
exit /b 0
