This project adds an interactive visual map to see the outcome
of each round. Just add your own bots as per usual.

See compileAndRun.sh for example of compiling and running.

![alt tag](https://github.com/skylogic004/conquest-engine-gui/raw/master/screenshot.png)
------------------------------------------------------------

Compile as follows:
Windows (from cmd):

    [go to the directory containing the .java files]
    dir /b /s *.java>sources.txt
    md classes
    javac -d classes @sources.txt
    del sources.txt
	copy images/* classes/

Linux:

    [go to the directory containing the .java files]
    ls *.java > sources.txt
    mkdir classes
    javac -d classes @sources.txt
    rm sources.txt
	cp images/* classes/

Then to run:

    cd classes
    java main.RunGame 0 0 0 "java bot.BotStarter" "java bot.BotStarter" 2>err.txt 1>out.txt

change the bots according to your own bots, error log will be outputted to err.txt and out log will be outputted to out.txt
