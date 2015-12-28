Warlight AI Challenge engine (http://theaigames.com/competitions/warlight-ai-challenge) + interactive visual map + replays + internal bots (can be hijacked) + human players.

Forked from: https://github.com/skylogic004/conquest-engine-gui

Heavily refactored (wrt. original Conquest codebase) ~ packages renamed, some classes renamed, etc., but communication protocol is still the same so it should work with original bots.

FEATURES

1) possible to play with "internal" players (i.e. bots directly on java classpath), which allows you to perform better Java bot debugging 

-- you can hijack controls of internal players when the game is visualized (press 'H' to toggle PLR1 hijack, press 'J' to toggle PLR2 hijack)

2) may output replay log into the file

3) may perform replay (parses replay file and replays the match) ~ fully working

4) human player (you can play against your AI!), use "human" as bot init command

-- beware, some buttons can be "right clicked" to "reverse the effect" (e.g. when placing armies left click -> plus, right click -> minus or when moving armies left click -> OK, right click -> cancel)

AI vs AI

![alt tag](https://github.com/kefik/conquest-engine-gui/raw/master/Conquest/screenshot.png)

HUMAN vs AI

![alt tag](https://github.com/kefik/conquest-engine-gui/raw/master/Conquest/screenshot-human.png)
------------------------------------------------------------

Compile Conquest project and run as follows (from within Conquest directory):

Windows (from cmd):

    rmdir /s /q bin
    mkdir bin
    dir /s /B *.java > sources.txt
    javac -d bin -cp bin @sources.txt
    mkdir bin\conquest\view\resources\images
    copy src\conquest\view\resources\images\* bin\conquest\view\resources\images\
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
