@echo off
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..\..
set PGSQL_BIN=%PROJECT_ROOT%\pgsql\pgsql\bin
set PGSQL_DATA=%PROJECT_ROOT%\pgsql\data

echo ========================================================
echo Stopping PostgreSQL 17...
echo ========================================================

"%PGSQL_BIN%\pg_ctl.exe" -D "%PGSQL_DATA%" stop
echo PostgreSQL server stopped.
