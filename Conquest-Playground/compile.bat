rmdir /s /q bin
mkdir bin
dir /s /B *.java > sources.txt
javac -d bin -cp ./bin;../Conquest/bin;../Conquest-Bots/bin @sources.txt
del /Q sources.txt