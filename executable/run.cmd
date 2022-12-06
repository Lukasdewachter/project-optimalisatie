@echo off
setlocal enabledelayedexpansion 
set list[0]=TOY-20-10.json
set list[1]=A-100-30.json
set list[2]=A-200-30.json
set list[3]=A-400-90.json
set list[4]=B-100-30.json
set list[5]=B-200-30.json
set list[6]=B-400-90.json
:a
echo 0 TOY-20-10
echo 1 A-100-30
echo 2 A-200-30
echo 3 A-400-90
echo 4 B-100-30
echo 5 B-200-30
echo 6 B-400-90
set /p fileNumber="Enter File Number: "
set /p seeds="Enter seeds time threads: "

echo (!list[%fileNumber%]!)
java -jar project.jar !list[%fileNumber%]! new-sol-!list[%fileNumber%]! %seeds%
python validator.py -i !list[%fileNumber%]! -s new-sol-!list[%fileNumber%]!
echo press any key to re-run
pause > nul
cls 
goto a
#Created By Lukas Dewachter
