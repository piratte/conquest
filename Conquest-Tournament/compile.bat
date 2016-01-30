rmdir /s /q bin
mkdir bin
dir /s /B *.java > sources.txt
javac -d bin -cp ./bin;../Conquest/bin;./lib/jsap-2.1.jar @sources.txt
del /Q sources.txt