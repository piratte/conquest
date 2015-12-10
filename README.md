This project adds an interactive visual map to see the outcome
of each round.

Heavily refactored (wrt. original Conquest codebase) ~ packages renamed, some name changes, etc., but protocol is still the same so it should work with original bots.

Forked from: https://github.com/skylogic004/conquest-engine-gui

FEATURES

1) possible to play with "internal" players (i.e. bots directly on java classpath), which allows you to perform better Java bot debugging 
2) may output replay log into the file
3) may perform replay (parses replay file and replays the match) ~ fully working

![alt tag](https://github.com/kefik/conquest-engine-gui/raw/master/Conquest/screenshot.png)
------------------------------------------------------------

Compile as follows:

Windows (from cmd):

    rmdir /s /q bin
    mkdir bin
    dir /s /B *.java > sources.txt
    javac -d bin -cp bin @sources.txt
    mkdir bin/conquest/view/resources/images
    copy src/conquest/view/resources/images/* bin/conquest/view/resources/images/
    del /Q sources.txt
    java -cp bin main.RunGame 0 0 0 "internal:conquest.bot.BotStarter" "internal:conquest.bot.BotStarter"

Linux:

    rm -rf bin
    mkdir bin
    find . -type f \( -iname "*.java" \) > sources.txt
    javac -d bin -cp bin @sources.txt
    rm sources.txt
    mkdir bin/conquest/view/resources/images
    cp src/conquest/view/resources/images/* bin/conquest/view/resources/images/
    java -cp bin main.RunGame 0 0 0 "internal:conquest.bot.BotStarter" "internal:conquest.bot.BotStarter"
