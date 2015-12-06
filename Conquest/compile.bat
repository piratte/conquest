rmdir /s /q bin
mkdir bin
dir /s /B *.java > sources.txt
javac -d bin -cp bin @sources.txt
del /Q sources.txt
copy images\* bin\