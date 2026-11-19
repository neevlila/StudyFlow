@echo off
title StudyFlow - Student Study Planner
echo ========================================================
echo               Starting StudyFlow Dashboard               
echo ========================================================
echo.

set JAVA_CMD=java

REM Check if java is available on system PATH
where java >nul 2>nul
if %ERRORLEVEL% equ 0 (
    goto :RUN
)

REM Check common local JDK locations
if exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
    goto :RUN
)

if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    set JAVA_CMD="C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
    goto :RUN
)

if exist "C:\Users\%USERNAME%\.jdk\jdk-25\bin\java.exe" (
    set JAVA_CMD="C:\Users\%USERNAME%\.jdk\jdk-25\bin\java.exe"
    goto :RUN
)

echo [ERROR] Java executable was not found on your system PATH or standard folders.
echo Please install Java 21 or set JAVA_HOME.
pause
exit /b 1

:RUN
echo Using Java: %JAVA_CMD%
echo.
echo Application starting on http://localhost:8080
echo Press Ctrl+C at any time in this window to stop the server.
echo ========================================================
echo.

if exist "target\studyflow.jar" (
    %JAVA_CMD% -jar target\studyflow.jar
) else if exist "target\studyflow-1.0.0.jar" (
    %JAVA_CMD% -jar target\studyflow-1.0.0.jar
) else (
    echo [ERROR] Application JAR not found in target folder.
    echo Please run 'mvn package' first.
    pause
    exit /b 1
)

if %ERRORLEVEL% neq 0 (
    echo.
    echo Application exited or encountered an error.
    pause
)
