rm -rf bin
find . -type f \( -iname "*.java" \) > sources.txt
mkdir bin
javac -d bin @sources.txt
rm sources.txt
cp images/* bin/
cd bin/
mkdir errors

# GameID, Bot1Id, Bot2Id, Command to run Bot1, Command to run Bot2
java -cp bin main.RunGame 0 0 0 "java bot.BotStarter" "java bot.BotStarter"
