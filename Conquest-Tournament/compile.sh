rm -rf bin
find . -type f \( -iname "*.java" \) > sources.txt
mkdir bin
javac -d bin -cp ./bin;../Conquest/bin;./lib/jsap-2.1.jar @sources.txt
rm sources.txt
