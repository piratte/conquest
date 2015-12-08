This project adds an interactive visual map to see the outcome
of each round. Just add your own bots as per usual.

Forked from: https://github.com/skylogic004/conquest-engine-gui

![alt tag](https://github.com/kefik/conquest-engine-gui/raw/master/Conquest/screenshot.png)
------------------------------------------------------------

Compile as follows:

Windows (from cmd):

    [go to the directory containing the .java files]
    rmdir /s /q bin
    mkdir bin
    dir /s /B *.java > sources.txt
    javac -d bin -cp bin @sources.txt
    mkdir bin/conquest/view/resources/images
    copy src/conquest/view/resources/images/* bin/conquest/view/resources/images/
    del /Q sources.txt
    copy images\* bin\
    java -cp bin main.RunGame 0 0 0 "internal:conquest.bot.BotStarter" "internal:conquest.bot.BotStarter"

Linux:

    rm -rf bin
    find . -type f \( -iname "*.java" \) > sources.txt
    mkdir bin
    javac -d bin @sources.txt
    rm sources.txt
    mkdir bin/conquest/view/resources/images
    cp src/conquest/view/resources/images/* bin/conquest/view/resources/images/
    cd bin/
    mkdir errors
    java -cp bin main.RunGame 0 0 0 "internal:conquest.bot.BotStarter" "internal:conquest.bot.BotStarter"
