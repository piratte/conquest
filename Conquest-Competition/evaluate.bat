del results\replays\%1-*.replay
del results\replays\*-%1-*.replay
del results\fights\%1-*.csv"
del results\fights\*-%1.csv"
java -cp "..\Conquest\bin\;..\Conquest-Tournament\bin;..\Conquest-Tournament\lib\*" conquest.tournament.ConquestFightConsole -s 426587 -o "GAME;PLR1;PLR2;x;x;false;false;-1;true;5000;5;100;CONTINUAL_1_1_A60_D70" -g 5 -r true -e "%1" -f "batch-fight.properties"