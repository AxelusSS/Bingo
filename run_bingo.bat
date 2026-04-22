@echo off
:loop
set SERVER_JAR=paper.jar
set RESET_FLAG=reset_map.txt

rem --- Vérification du flag de réinitialisation ---
if exist %RESET_FLAG% (
    echo [Bingo] Drapeaux de reinitialisation detecte !
    echo [Bingo] Suppression des mondes en cours...
    
    rem Suppression des dossiers de monde (silencieux)
    if exist world rd /s /q world
    if exist world_nether rd /s /q world_nether
    if exist world_the_end rd /s /q world_the_end
    
    rem Suppression du fichier flag
    del %RESET_FLAG%
    
    echo [Bingo] Monde supprime avec succes. Generating new world...
)

echo [Bingo] Demarrage du serveur...
java -Xms2G -Xmx4G -jar %SERVER_JAR% nogui

echo [Bingo] Le serveur s'est arrete.
echo [Bingo] Appuie sur une touche pour redemarrer manuellement ou ferme la fenêtre.
pause
goto loop
