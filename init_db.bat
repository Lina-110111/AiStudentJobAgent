@echo off
chcp 65001 >nul
echo ==============================================
echo  Init job_agent database (schema + data + rbac)
echo ==============================================
cd /d "%~dp0"

set "MYSQL=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if not exist "%MYSQL%" goto :nomysql

set /p MYSQL_PWD=Enter MySQL root password then press Enter: 

"%MYSQL%" -uroot -p%MYSQL_PWD% < "deploy\sql\schema.sql"
if errorlevel 1 goto :err

"%MYSQL%" -uroot -p%MYSQL_PWD% job_agent < "deploy\sql\data.sql"
if errorlevel 1 goto :err

"%MYSQL%" -uroot -p%MYSQL_PWD% job_agent < "deploy\sql\rbac_schema.sql"
if errorlevel 1 goto :err

echo.
echo  [OK] Database initialized successfully! (job_agent: 8 tables + demo accounts)
goto :end

:nomysql
echo.
echo  [FAILED] mysql.exe not found at:
echo  C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
echo  Tell me your actual MySQL install path.

:err
echo.
echo  [FAILED] An error occurred. Please send the red error text to me.
echo  Tip: if it reports access denied, your root password is wrong - rerun and type it again.

:end
pause
