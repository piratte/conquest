Warlight AI Challenge engine (http://theaigames.com/competitions/warlight-ai-challenge) + interactive visual map + replays.

Forked from: https://github.com/skylogic004/conquest-engine-gui

Heavily refactored (wrt. original Conquest codebase) ~ packages renamed, some name changes, etc., but protocol is still the same so it should work with original bots.

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
    java -cp bin conquest.Conquest 100 5000 "internal:conquest.bot.BotStarter" "process:java -cp bin conquest.bot.BotStarter" true replay.log

Linux (from bash):

    rm -rf bin
    mkdir bin
    find . -type f \( -iname "*.java" \) > sources.txt
    javac -d bin -cp bin @sources.txt
    rm sources.txt
    mkdir bin/conquest/view/resources/images
    cp src/conquest/view/resources/images/* bin/conquest/view/resources/images/
    java -cp bin conquest.Conquest 100 5000 "internal:conquest.bot.BotStarter" "process:java -cp bin conquest.bot.BotStarter" true replay.log
