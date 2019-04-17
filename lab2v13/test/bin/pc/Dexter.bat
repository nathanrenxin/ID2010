@echo off
rem -- ----------------------------------------------------------------
rem -- 2018-08-24/fki Lab 2 version 13
rem -- This file is for Windows.
rem -- This file starts a Dexter from its remote installation point.
rem -- ----------------------------------------------------------------

set CFG=%~dp0.\HTTPD_CFG.BAT

IF NOT EXIST "%CFG%" ECHO File not found %CFG% & Goto :eof

CALL "%CFG%"
SET HTTP=%CODEBASE%

set ROOT=%~dp0..\..

set LIB=%ROOT%/lib

set PCY=%LIB%/policy.all

set JRN=%LIB%/JarRunner.jar

set CBS=%HTTP%/Dexter.jar

set JAR=%HTTP%/Dexter.jar

set CLASSPATH=

set POL=-Djava.security.policy=%PCY%
set CBO=-Djava.rmi.server.codebase=%CBS%

java "%POL%" "%CBO%" -jar "%JRN%" "%JAR%" %*
