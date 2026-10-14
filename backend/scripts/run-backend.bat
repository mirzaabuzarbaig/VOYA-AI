@echo off
set SCRIPT_DIR=%~dp0
set BACKEND_DIR=%SCRIPT_DIR%..
cd /d "%BACKEND_DIR%"

echo ========================================================
echo Starting VoyaAI Spring Boot Backend (Port 8080)...
echo ========================================================

call .\mvnw.cmd spring-boot:run
