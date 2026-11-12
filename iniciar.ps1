# Levanta PostgreSQL (si no esta corriendo) y Tomcat con el Asistente Virtual UTD.
# Uso:  & .\iniciar.ps1     (deje esta ventana abierta; Ctrl+C detiene Tomcat)
$scoop = Join-Path $env:USERPROFILE 'scoop'
$env:JAVA_HOME     = Join-Path $scoop 'apps\oraclejdk\current'
$env:CATALINA_HOME = Join-Path $scoop 'apps\tomcat\current'
$env:CATALINA_BASE = $env:CATALINA_HOME

$pgCtl  = Join-Path $scoop 'apps\postgresql\current\bin\pg_ctl.exe'
$pgData = Join-Path $scoop 'persist\postgresql\data'
& $pgCtl -D $pgData status *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Host 'Iniciando PostgreSQL...'
    & $pgCtl -D $pgData -l (Join-Path $pgData '..\postgres.log') start
}

Write-Host 'Iniciando Tomcat -> http://localhost:8080/asistente-virtual/'
& (Join-Path $env:CATALINA_HOME 'bin\catalina.bat') run
