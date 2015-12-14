rmdir /s /q bin
mkdir bin
dir /s /B *.java > sources.txt
javac -d bin -cp bin @sources.txt
mkdir bin\conquest\view\resources\images
copy src\conquest\view\resources\images\* bin\conquest\view\resources\images\
del /Q sources.txt