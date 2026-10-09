param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$PostgresBin = 'C:\Program Files\PostgreSQL\18\bin',
    [int]$WebPort = 8080,
    [int]$DatabasePort = 55432
)
$ErrorActionPreference = 'Stop'
$taskProjectRoot = Split-Path $PSScriptRoot -Parent
$taskDatabasePath = Join-Path $taskProjectRoot 'tmp/appointment-pg'
$taskDatabaseLog = Join-Path $taskProjectRoot 'tmp/appointment-pg.log'
if (-not $JavaHome) {
    $taskJdk = Get-ChildItem 'C:/Program Files/Java' -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName 'bin/java.exe') } | Sort-Object Name -Descending | Select-Object -First 1
    if ($taskJdk) { $JavaHome = $taskJdk.FullName }
}
if (-not $JavaHome -or -not (Test-Path (Join-Path $JavaHome 'bin/java.exe'))) { throw 'Set JAVA_HOME to your JDK 17 or newer.' }
if (-not (Test-Path (Join-Path $PostgresBin 'pg_ctl.exe'))) { throw 'Provide -PostgresBin for your installed PostgreSQL bin folder, or use Docker Compose.' }
$env:JAVA_HOME = $JavaHome
$env:PATH = (Join-Path $JavaHome 'bin') + ';' + $env:PATH
New-Item -ItemType Directory -Force -Path (Join-Path $taskProjectRoot 'tmp') | Out-Null
if (-not (Test-Path (Join-Path $taskDatabasePath 'PG_VERSION'))) {
    # Isolated development cluster, accepts only loopback connections when started below.
    & (Join-Path $PostgresBin 'initdb.exe') -D $taskDatabasePath -U appointment_dev --auth=trust --encoding=UTF8 --locale=C
    if ($LASTEXITCODE -ne 0) { throw 'Could not initialize the isolated development database.' }
}
& (Join-Path $PostgresBin 'pg_ctl.exe') -D $taskDatabasePath status *> $null
if ($LASTEXITCODE -ne 0) {
    & (Join-Path $PostgresBin 'pg_ctl.exe') -D $taskDatabasePath -l $taskDatabaseLog -o "-p $DatabasePort -h 127.0.0.1" start
    if ($LASTEXITCODE -ne 0) { throw 'Could not start development PostgreSQL. Check the port and database log.' }
}
$taskExists = & (Join-Path $PostgresBin 'psql.exe') -h 127.0.0.1 -p $DatabasePort -U appointment_dev -d postgres -tAc "select 1 from pg_database where datname='appointment_dev'"
if ($LASTEXITCODE -ne 0) { throw 'Could not connect to development PostgreSQL.' }
if ($taskExists -notmatch '1') {
    & (Join-Path $PostgresBin 'createdb.exe') -h 127.0.0.1 -p $DatabasePort -U appointment_dev appointment_dev
    if ($LASTEXITCODE -ne 0) { throw 'Could not create development database.' }
}
Push-Location $taskProjectRoot
try {
    & mvn.cmd -f code/pom.xml '-DskipTests' package
    if ($LASTEXITCODE -ne 0) { throw 'Build failed.' }
    Write-Host "Open http://localhost:$WebPort/appointment-create.html (Ctrl+C to stop the app)."
    & (Join-Path $JavaHome 'bin/java.exe') -jar code/target/petclinic-0.0.1-SNAPSHOT.jar `
        "--server.port=$WebPort" "--spring.datasource.url=jdbc:postgresql://127.0.0.1:$DatabasePort/appointment_dev" `
        --spring.datasource.username=appointment_dev --spring.datasource.password= --spring.jpa.show-sql=false
} finally { Pop-Location }
