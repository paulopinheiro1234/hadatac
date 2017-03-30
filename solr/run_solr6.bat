echo off
set SOLR_INSTALL_DIR=%HADATAC_SOLR%\solr-6.5.0

:STEP 1

if EXIST "%SOLR_INSTALL_DIR%" GOTO STEP2

echo "%SOLR_INSTALL_DIR% not found! Please check the SOLR_INSTALL_DIR setting in your %0 script."
GOTO THEEND

:STEP2

set SOLR_ENV=%HADATAC_SOLR%\solr6.in.bat

if EXIST "%SOLR_ENV%" GOTO STEP3

echo "%SOLR_ENV% not found! Please check the SOLR_ENV setting in your %0 script."
GOTO THEEND

:STEP3

if "%1"=="stop" GOTO :STEP4
if "%1"=="start" GOTO :STEP4
if "%1"=="restart" GOTO :STEP4
if "%1"=="status" GOTO :STEP4

echo "Usage: %0 {start|stop|restart|status}"
GOTO THEEND

:STEP4

set SOLR_INCLUDE=%SOLR_ENV% 

echo Issuing command %SOLR_INSTALL_DIR%\bin\solr %1

%SOLR_INSTALL_DIR%\bin\solr %1

:THEEND
