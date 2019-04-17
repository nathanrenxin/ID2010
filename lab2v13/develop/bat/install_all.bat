@REM -- 2018-08-27/fki Lab 2 version 13
@echo off
rem --
rem -- This file will run all the install scripts in one go.
rem --
set BAT=%~dp0.
call "%BAT%\install_jini"
call "%BAT%\install_bailiff"
call "%BAT%\install_dexter"
