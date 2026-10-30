@echo off
rem LibraryHub Maven bootstrap script for Windows.
rem Downloads the Apache Maven version named in .mvn\wrapper\maven-wrapper.properties
rem once into %USERPROFILE%\.m2\wrapper and then runs it with all the arguments you pass.
setlocal

set "BASE_DIR=%~dp0"
set "PROPS=%BASE_DIR%.mvn\wrapper\maven-wrapper.properties"
if not exist "%PROPS%" goto :missing_props

set "DIST_URL="
for /f "usebackq tokens=1,* delims==" %%A in (`findstr /b "distributionUrl=" "%PROPS%"`) do set "DIST_URL=%%B"
if "%DIST_URL%"=="" goto :missing_url

for %%F in ("%DIST_URL%") do set "ARCHIVE_NAME=%%~nxF"
set "DIST_NAME=%ARCHIVE_NAME:-bin.zip=%"
set "MVN_ROOT=%USERPROFILE%\.m2\wrapper\dists\%DIST_NAME%"
set "MVN_HOME=%MVN_ROOT%\%DIST_NAME%"

if exist "%MVN_HOME%\bin\mvn.cmd" goto :run

echo mvnw: downloading %DIST_NAME% ...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; New-Item -ItemType Directory -Force -Path '%MVN_ROOT%' | Out-Null; $zip=Join-Path '%MVN_ROOT%' 'maven.zip'; Invoke-WebRequest -UseBasicParsing -Uri '%DIST_URL%' -OutFile $zip; Expand-Archive -Force -Path $zip -DestinationPath '%MVN_ROOT%'; Remove-Item $zip"
if errorlevel 1 goto :download_failed

:run
call "%MVN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%

:missing_props
echo mvnw: missing %PROPS% 1>&2
exit /b 1

:missing_url
echo mvnw: distributionUrl is not set in %PROPS% 1>&2
exit /b 1

:download_failed
echo mvnw: could not download Maven. Check your internet connection or proxy settings. 1>&2
exit /b 1
