rm -rf bin
find . -type f \( -iname "*.java" \) > sources.txt
mkdir bin
javac -d bin @sources.txt
rm sources.txt
mkdir bin/conquest/view/resources/images
cp src/conquest/view/resources/images/* bin/conquest/view/resources/images/

# GameID, Bot1Id, Bot2Id, Command to run Bot1, Command to run Bot2
java -cp bin conquest.engine.RunGame 0 0 0 "internal:conquest.bot.BotStarter" "internal:conquest.bot.BotStarter"
