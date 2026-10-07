@echo off
chcp 65001 >nul
rem Finalise le renommage EduNiger -> Elim sur ce PC :
rem supprime l'ancien dossier de package com\ninotech\eduniger (les fichiers
rem Java sont deja copies dans com\naniger\elim), puis nettoie le build Gradle.
cd /d "%~dp0"
set NEW=eduniger_native\src\main\java\com\naniger\elim
set OLD=eduniger_native\src\main\java\com\ninotech
if not exist "%NEW%\controleur\activity\MainActivity.java" (
  echo ERREUR : le nouveau package %NEW% est introuvable. Rien n'a ete supprime.
  pause
  exit /b 1
)
if exist "%OLD%" (
  echo Suppression de l'ancien package %OLD% ...
  rmdir /s /q "%OLD%"
) else (
  echo L'ancien package est deja supprime.
)
echo Nettoyage Gradle ...
call gradlew.bat clean
echo.
echo Termine. Lancez maintenant : gradlew.bat assembleDebug
pause
