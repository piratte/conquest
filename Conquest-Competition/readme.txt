======================
HOW TO RUN THE EXAMPLE
======================

1) Compile Conquest and Conquest-Bots projects first (run respective compile.bat files)

2) Check batch-fight.properties files if bot init commands are correct for you environment

3) evaluate-AggressiveBot.bat => AggressiveBot vs all others within batch-fight.properties (BotStarter right now)

4) evaluate-BotStarter.bat => BotStarter vs all others within batch-fight.properties (BotStarter right now)

IMPORTANT: All bat files must be run from within Conquest-Competition directory, and
you must have the whole repository cloned. E.g., we rely on the fact that "../Conquest" relative
path resolves correctly from within Conquest-Competition directory.

5) summarize.bat => will produce a summary of results out of ./results/fights CSV files

==========
HOW TO USE
==========

1) you have some new bot you want to test against other bots within batch-fight.properties

2) make sure it is fully working (manually run it to check if it works)

3) edit batch-fight.properties and
3.1) add a new line in the form <bot-id>=<bot-init>
3.2) <bot-id> must be unique (there cannot be two lines within the file beginning with the same bot-id)
3.3) <bot-init> must be in the form as recognized by RunGame class, see Conquest project, RunGame.setupRobot(...) method for up-to-date details

4) call evaluate.bat <bot-id> to run your bot against all other bots within batch-fight.properties

In case you have troubles, you may contact me directly at: jakub.gemrot@gmail.com

============
DESIGN NOTES
============

1) evaluate.bat is purposefully designed for "reevaluation", i.e., it deletes all previous results from results/fights before starting new one
-- beware that previous fights are not deleted from results/all-results.csv

2) intended use is to evaluate "one-bot" at a time (i.e. add new bot into batch-fight.properties and evaluate it)
-- if you fill it at once then
---- a) a lot of fights will happen twice
---- b) there is higher chance you will make a mistake along the way ruining the evaluation
