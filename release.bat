@echo off
setlocal enabledelayedexpansion

REM Script pour créer une nouvelle version et la pousser vers GitHub

echo Création d'une nouvelle version de Raven bS+
echo ============================================

REM Vérifier si Git est installé
where git >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Git n'est pas installé ou n'est pas dans le PATH.
    echo Veuillez installer Git depuis https://git-scm.com/downloads
    exit /b 1
)

REM Vérifier si le dépôt est configuré
if not exist .git (
    echo Ce dossier n'est pas un dépôt Git.
    echo Veuillez initialiser le dépôt avec:
    echo git init
    echo git remote add origin https://github.com/VOTRE_NOM_UTILISATEUR/RavenBS-Plus.git
    exit /b 1
)

REM Lire la version actuelle
for /f "tokens=3" %%i in ('findstr "version =" gradle.properties') do (
    set CURRENT_VERSION=%%i
)

echo Version actuelle: %CURRENT_VERSION%
set /p NEW_VERSION=Entrez la nouvelle version (format x.y.z): 

REM Mettre à jour les fichiers avec la nouvelle version
echo Mise à jour des fichiers avec la version %NEW_VERSION%...

REM Mettre à jour gradle.properties
powershell -Command "(Get-Content gradle.properties) -replace 'version = %CURRENT_VERSION%', 'version = %NEW_VERSION%' | Set-Content gradle.properties"

REM Mettre à jour README.md
powershell -Command "(Get-Content README.md) -replace 'Version actuelle: %CURRENT_VERSION%', 'Version actuelle: %NEW_VERSION%' | Set-Content README.md"

REM Mettre à jour CONTRIBUTING.md
powershell -Command "(Get-Content CONTRIBUTING.md) -replace 'La version actuelle du mod est \*\*%CURRENT_VERSION%\*\*', 'La version actuelle du mod est **%NEW_VERSION%**' | Set-Content CONTRIBUTING.md"

REM Compiler le projet
echo Compilation du projet...
call gradle build --no-daemon
if %ERRORLEVEL% neq 0 (
    echo Erreur lors de la compilation du projet.
    exit /b 1
)

REM Committer les changements
echo Commit des changements...
git add gradle.properties README.md CONTRIBUTING.md
git commit -m "Mise à jour de la version vers %NEW_VERSION%"

REM Créer un tag
echo Création du tag v%NEW_VERSION%...
git tag v%NEW_VERSION%

REM Demander confirmation pour pousser vers GitHub
set /p CONFIRM=Voulez-vous pousser les changements vers GitHub? (O/N): 
if /i "%CONFIRM%" == "O" (
    echo Poussée des changements vers GitHub...
    git push origin main
    git push origin v%NEW_VERSION%
    
    echo Attente de la fin du build GitHub Actions...
    echo Une fois le build terminé, vous pourrez trouver la release à l'adresse:
    echo https://github.com/VOTRE_NOM_UTILISATEUR/RavenBS-Plus/releases
    
    echo Version %NEW_VERSION% créée et poussée avec succès!
) else (
    echo Les changements ont été committés localement mais n'ont pas été poussés vers GitHub.
    echo Pour pousser manuellement, utilisez:
    echo git push origin main
    echo git push origin v%NEW_VERSION%
)

echo Terminé!
