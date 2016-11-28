Warlight AI Challenge engine (http://theaigames.com/competitions/warlight-ai-challenge) + interactive visual map + replays + internal bots (can be hijacked) + human players + automation.

Forked from: https://github.com/skylogic004/conquest-engine-gui

Heavily refactored (wrt. original Conquest codebase) ~ packages renamed, some classes renamed, etc., but communication protocol is still the same so it should work with original bots.

**FEATURES**

1) possible to play with "internal" players (i.e. bots directly on java classpath), which allows you to perform better Java bot debugging

-- you can hijack controls of internal players when the game is visualized (press 'H' to toggle PLR1 hijack, press 'J' to toggle PLR2 hijack)

2) may output replay log into the file

3) may perform replay (parses replay file and replays the match) ~ fully working

4) human player (you can play against your AI!), use "human" as bot init command

-- beware, some buttons can be "right clicked" to "reverse the effect" (e.g. when placing armies left click -> plus, right click -> minus or when moving armies left click -> OK, right click -> cancel)

5) better GameState abstraction than plain BotState provided, use GameBot as a base class for your bots

6) slim GameStateCompact representation of the game state that can be used for performance searches (not the best, but quite ok); use GameStateCompact.fromGameState(gameState) you have in your GameBot

7) Conquest-Tournament project can be used to automate matches between bots using command line tools (see ConquestFightConsole and ConquestTableConsole classes); see Conquest-Competition for example how to quickly setup tournament
batch files (be sure to stop by and read Conquest-Competition/readme.txt)

8) possibility to execute "process" player from within concrete directory, use "dir;process" as bot init command to specify the directory from which you would like the bot process to be executed

-- not a big feature but very handy for automation

-- if you deal with Java bots, you might want to run your Java bots as "external bots" using "dir;process" indirectly running JavaBot class instead of original bot class (i.e. make JavaBot to instantiate and run your tournament bot);
this will spare you the problems with "invalid" main of the bot you want to execute (as long as it has parameterless constructor)

**EXTRA GUI (KEYBOARD) CONTROLS**

+ 'N') skip to the next round

+ 'C') run continually (GUI won't wait for mouse click to advance to the next action, but auto-advance after some time)

+ '+' / '-') Control how much time GUI will wait before it automatically advance to the next action (when enabled via 'C')

**EXTRA GUI (MOUSE) CONTROLS**

+ Left click the map to advance to the next action.

+ Right click the map to fast forward.

**AI vs AI**

![alt tag](https://github.com/kefik/conquest-engine-gui/raw/master/Conquest/screenshot.png)

**HUMAN vs AI**

![alt tag](https://github.com/kefik/conquest-engine-gui/raw/master/Conquest/screenshot-human.png)

------------------------------------------------------------

Bot code (non-complete) example taken from AggressiveBot:

    // CAPTURE ALL REGIONS WE CAN
	for (RegionState from : state.me.regions.values()) {
    	for (RegionState to : from.neighbours) {
    		// DO NOT ATTACK OWN REGIONS
    		if (to.owned(Player.ME)) continue;
    		
    		// IF YOU HAVE ENOUGH ARMY TO WIN WITH 70%
    		if (shouldAttack(from, to, 0.7)) {
    			// => ATTACK
    			result.add(attack(from, to, 0.7));
    		}
    	}
    }

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
