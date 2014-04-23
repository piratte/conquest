rm -rf classes
find . -type f \( -iname "*.java" \) > sources.txt
mkdir classes
javac -d classes @sources.txt
rm sources.txt
cp images/* classes/
cd classes/
mkdir errors

java main.RunGame 0 0 0 "java bot.BotStarter" "java bot.BotStarter"
