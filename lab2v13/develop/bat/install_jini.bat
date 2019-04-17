@REM -- 2018-08-27/fki Lab 2 version 13
@echo off
set BAT=%~dp0.
Call "%BAT%\httpdCopy.bat" lib\jini-core.jar
Call "%BAT%\httpdCopy.bat" lib\jini-ext.jar
Call "%BAT%\httpdCopy.bat" lib\reggie-dl.jar
