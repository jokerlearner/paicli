@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
set "PAICLI_RENDERER=inline"
java --enable-native-access=ALL-UNNAMED -Dfile.encoding=UTF-8 -jar ".\target\paicli-1.0-SNAPSHOT.jar"
set "paicliExitCode=%ERRORLEVEL%"
endlocal & exit /b %paicliExitCode%
