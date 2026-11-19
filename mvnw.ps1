# PowerShell Maven Runner
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$mvn = "C:\Users\hp\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd"

if (Test-Path $mvn) {
    & $mvn $args
} else {
    Write-Error "Maven binary not found at $mvn"
}
