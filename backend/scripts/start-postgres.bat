@echo off
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..\..
set PGSQL_BIN=%PROJECT_ROOT%\pgsql\pgsql\bin
set PGSQL_DATA=%PROJECT_ROOT%\pgsql\data
set PGSQL_LOG=%PROJECT_ROOT%\pgsql\server.log

echo ========================================================
echo Starting PostgreSQL 17 for VoyaAI...
echo ========================================================

if not exist "%PGSQL_DATA%" (
    echo Initializing database cluster at %PGSQL_DATA%...
    "%PGSQL_BIN%\initdb.exe" -D "%PGSQL_DATA%" -U postgres -E UTF8 -A trust
    "%PGSQL_BIN%\pg_ctl.exe" -D "%PGSQL_DATA%" -l "%PGSQL_LOG%" start
    timeout /t 3 >nul
    "%PGSQL_BIN%\createdb.exe" -U postgres voyaai
    echo Created database: voyaai
) else (
    "%PGSQL_BIN%\pg_ctl.exe" -D "%PGSQL_DATA%" -l "%PGSQL_LOG%" start
)

echo PostgreSQL is running on localhost:5432 (Database: voyaai)
