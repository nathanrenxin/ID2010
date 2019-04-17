@REM -- 2018-08-27/fki Lab 2 version 13
@echo off
set BAT=%~dp0.
Call "%BAT%\httpdCopy.bat" dist\Bailiff.jar
Call "%BAT%\httpdCopy.bat" dist\Bailiff-dl.jar
