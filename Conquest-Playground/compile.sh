rm -rf bin
find . -type f \( -iname "*.java" \) > sources.txt
mkdir bin
javac -d bin -cp ./bin;../Conquest/bin;../Conquest-Bots/bin @sources.txt
rm sources.txt
