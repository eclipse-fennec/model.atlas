@echo off
setlocal enabledelayedexpansion
rem ==========================================================================
rem  Copyright (c) 2026 Contributors to the Eclipse Foundation.
rem
rem  This program and the accompanying materials are made
rem  available under the terms of the Eclipse Public License 2.0
rem  which is available at https://www.eclipse.org/legal/epl-2.0/
rem
rem  SPDX-License-Identifier: EPL-2.0
rem
rem  Contributors:
rem      Data In Motion - initial API and implementation
rem ==========================================================================
rem  Generate the Eclipse Dash "DEPENDENCIES" file for this bnd workspace.
rem
rem  Windows counterpart of tools/dash-licenses.sh. Uses the new
rem  `bnd repo deps` subcommand (bnd 7.4.0-SNAPSHOT or newer) to export the
rem  Maven GAVs of every artifact the workspace resolves, then feeds that list
rem  to the Eclipse Dash License Tool (dash-licenses) to produce DEPENDENCIES.
rem
rem  Usage (from a normal cmd.exe):
rem    tools\dash-licenses.bat                       regenerate DEPENDENCIES
rem    tools\dash-licenses.bat --review --project technology.fennec
rem
rem  The exit code is the number of "restricted" dependencies (0 = all approved).
rem ==========================================================================

rem ---- defaults ------------------------------------------------------------
if not defined BND_VERSION    set "BND_VERSION=7.4.0-SNAPSHOT"
if not defined BND_SNAPSHOTS  set "BND_SNAPSHOTS=https://bndtools.jfrog.io/bndtools/libs-snapshot-local"
if not defined DASH_VERSION   set "DASH_VERSION=1.1.0"
if not defined DASH_RELEASES  set "DASH_RELEASES=https://repo.eclipse.org/content/repositories/dash-licenses"

set "REVIEW=false"
set "TOKEN=%DASH_IPLAB_TOKEN%"
set "PROJECT=%DASH_PROJECT_ID%"
set "SUMMARY="

rem ---- arg parsing ---------------------------------------------------------
:parse
if "%~1"=="" goto endparse
if /I "%~1"=="--review"       ( set "REVIEW=true" & shift & goto parse )
if /I "%~1"=="--token"        ( set "TOKEN=%~2" & shift & shift & goto parse )
if /I "%~1"=="--project"      ( set "PROJECT=%~2" & shift & shift & goto parse )
if /I "%~1"=="--summary"      ( set "SUMMARY=%~2" & shift & shift & goto parse )
if /I "%~1"=="--bnd-version"  ( set "BND_VERSION=%~2" & shift & shift & goto parse )
if /I "%~1"=="--dash-version" ( set "DASH_VERSION=%~2" & shift & shift & goto parse )
if /I "%~1"=="-h"             goto usage
if /I "%~1"=="--help"         goto usage
echo Unknown option: %~1 1>&2
goto usage
:endparse

rem ---- locate workspace ----------------------------------------------------
for /f "delims=" %%i in ('git rev-parse --show-toplevel') do set "ROOT=%%i"
if not defined ROOT ( echo ERROR: not inside a git repository. 1>&2 & exit /b 2 )
set "ROOT=%ROOT:/=\%"
set "CACHE=%ROOT%\cnf\cache\dash-licenses"
if not exist "%CACHE%" mkdir "%CACHE%"
if not defined SUMMARY set "SUMMARY=%ROOT%\DEPENDENCIES"
set "DEPS=%CACHE%\deps.txt"

rem ---- resolve & download the bnd CLI --------------------------------------
rem  Detect a -SNAPSHOT version: stripping "-SNAPSHOT" changes the string.
if not "%BND_VERSION%"=="%BND_VERSION:-SNAPSHOT=%" (
  echo ^>^> Resolving latest %BND_VERSION% snapshot of the bnd CLI ...
  set "META=%CACHE%\bnd-metadata.xml"
  curl -fsSL -o "!META!" "%BND_SNAPSHOTS%/biz/aQute/bnd/biz.aQute.bnd/%BND_VERSION%/maven-metadata.xml" || exit /b 1
  set "TS="
  set "BN="
  for /f "tokens=2 delims=<> " %%v in ('findstr "<timestamp>" "!META!"') do if not defined TS set "TS=%%v"
  for /f "tokens=2 delims=<> " %%v in ('findstr "<buildNumber>" "!META!"') do if not defined BN set "BN=%%v"
  set "BASE=%BND_VERSION:-SNAPSHOT=%"
  set "BND_JAR_VERSION=!BASE!-!TS!-!BN!"
  set "BND_URL=%BND_SNAPSHOTS%/biz/aQute/bnd/biz.aQute.bnd/%BND_VERSION%/biz.aQute.bnd-!BND_JAR_VERSION!.jar"
) else (
  set "BND_JAR_VERSION=%BND_VERSION%"
  set "BND_URL=https://repo.maven.apache.org/maven2/biz/aQute/bnd/biz.aQute.bnd/%BND_VERSION%/biz.aQute.bnd-%BND_VERSION%.jar"
)
set "BND_JAR=%CACHE%\biz.aQute.bnd-!BND_JAR_VERSION!.jar"
if not exist "!BND_JAR!" (
  echo ^>^> Downloading bnd CLI !BND_JAR_VERSION! ...
  curl -fsSL -o "!BND_JAR!" "!BND_URL!" || exit /b 1
)

rem ---- download dash-licenses ----------------------------------------------
set "DASH_JAR=%CACHE%\org.eclipse.dash.licenses-%DASH_VERSION%.jar"
if not exist "!DASH_JAR!" (
  echo ^>^> Downloading dash-licenses %DASH_VERSION% ...
  curl -fsSL -o "!DASH_JAR!" "%DASH_RELEASES%/org/eclipse/dash/org.eclipse.dash.licenses/%DASH_VERSION%/org.eclipse.dash.licenses-%DASH_VERSION%.jar" || exit /b 1
)

rem ---- 1) export the dependency list ---------------------------------------
echo ^>^> Exporting dependency GAVs with 'bnd repo deps' ...
pushd "%ROOT%"
java -jar "!BND_JAR!" repo deps -o "%DEPS%"
set "RC=%ERRORLEVEL%"
popd
if not "%RC%"=="0" ( echo ERROR: 'bnd repo deps' failed. 1>&2 & exit /b %RC% )

rem ---- 2) run dash-licenses ------------------------------------------------
if /I "%REVIEW%"=="true" (
  if "%TOKEN%"=="" ( echo ERROR: --review needs --token / DASH_IPLAB_TOKEN and --project / DASH_PROJECT_ID. 1>&2 & exit /b 2 )
  if "%PROJECT%"=="" ( echo ERROR: --review needs --token / DASH_IPLAB_TOKEN and --project / DASH_PROJECT_ID. 1>&2 & exit /b 2 )
  echo ^>^> Running dash-licenses in review mode for project '%PROJECT%' ...
  java -jar "!DASH_JAR!" "%DEPS%" -summary "%SUMMARY%" -review -token "%TOKEN%" -project "%PROJECT%"
) else (
  echo ^>^> Running dash-licenses ...
  java -jar "!DASH_JAR!" "%DEPS%" -summary "%SUMMARY%"
)
set "RC=%ERRORLEVEL%"

echo ^>^> DEPENDENCIES written to %SUMMARY%
if not "%RC%"=="0" echo ^>^> %RC% dependency/dependencies are restricted ^(need IP review^). See output above.
exit /b %RC%

:usage
echo.
echo Generate the Eclipse Dash DEPENDENCIES file for this bnd workspace.
echo.
echo Usage:
echo   tools\dash-licenses.bat [options]
echo.
echo Options:
echo   --review              Open IP review issues in the Eclipse GitLab IP Lab.
echo                         Requires --token and --project (or the env vars).
echo   --token ^<token^>       GitLab API token        (env: DASH_IPLAB_TOKEN)
echo   --project ^<id^>        Eclipse project id       (env: DASH_PROJECT_ID)
echo   --summary ^<file^>      DEPENDENCIES output file (default: repo-root\DEPENDENCIES)
echo   --bnd-version ^<v^>     bnd snapshot version     (default: 7.4.0-SNAPSHOT)
echo   --dash-version ^<v^>    dash-licenses version    (default: 1.1.0)
echo   -h, --help            Show this help.
exit /b 0
