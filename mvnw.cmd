@echo off
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
    ) else if exist "C:\Users\%USERNAME%\.jdk\jdk-25\bin\java.exe" (
        set "JAVA_HOME=C:\Users\%USERNAME%\.jdk\jdk-25"
    )
)

set MVN_BIN=C:\Users\hp\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd

if exist "%MVN_BIN%" (
    call "%MVN_BIN%" %*
) else (
    echo [ERROR] Maven not found at %MVN_BIN%
    echo Please install Maven or add it to PATH.
    exit /b 1
)
