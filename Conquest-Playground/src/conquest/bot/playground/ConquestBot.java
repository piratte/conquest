package conquest.bot.playground;

import conquest.bot.BotParser;
import conquest.bot.custom.AggressiveBot;
import conquest.bot.custom.SmartBot;
import conquest.bot.fight.FightSimulation;
import conquest.bot.fight.FightSimulation.FightAttackersResults;
import conquest.bot.fight.FightSimulation.FightDefendersResults;
import conquest.bot.map.RegionBFS;
import conquest.bot.map.RegionBFS.BFSNode;
import conquest.bot.map.RegionBFS.BFSVisitResult;
import conquest.bot.map.RegionBFS.BFSVisitResultType;
import conquest.bot.state.*;
import conquest.bot.state.GameState.RegionState;
import conquest.engine.Engine.FightMode;
import conquest.engine.RunGame;
import conquest.engine.RunGame.Config;
import conquest.engine.RunGame.GameResult;
import conquest.game.Player;
import conquest.game.world.Continent;
import conquest.game.world.Region;
import conquest.view.GUI;

import java.io.File;
import java.util.*;

import static java.util.Collections.*;

/**
 * Copy-paste of {@link AggressiveBot}. Feel free to fool around!
 * And be sure to check {@link FightSimulation} that provides fight-victory-probabilities for this bot.
 */
public class ConquestBot extends GameBot
{
	
	private FightAttackersResults aRes;
	private FightDefendersResults dRes;

	private SmartBot smartBot = new SmartBot();
	
	public ConquestBot() {
		aRes = FightAttackersResults.loadFromFile(new File("Conquest-Playground/FightSimulation-Attackers-A200-D200.obj"));
		dRes = FightDefendersResults.loadFromFile(new File("Conquest-Playground/FightSimulation-Defenders-A200-D200.obj"));
		System.err.println("---==[ BLABLA BOT INITIALIZED ]==---");
	}
	
	@Override
	public void setGUI(GUI gui) {
	}
	
	// ================
	// CHOOSING REGIONS
	// ================
	
	@Override
	public List<ChooseCommand> chooseRegions(List<Region> choosable, long timeout) {
		int m = 6;
		
		// SORT PICKABLE REGIONS ACCORDING TO THE PRIORITY
		choosable.sort((o1, o2) -> {
            int priority1 = getPrefferedContinentPriority(o1.continent);
            int priority2 = getPrefferedContinentPriority(o2.continent);
            return priority1 - priority2;
        });
		
		// REMOVE CONTINENT WE DO NOT WANT
		while (choosable.size() > m) choosable.remove(choosable.size()-1);
		
		// CREATE COMMANDS
		List<ChooseCommand> result = new ArrayList<>(choosable.size());
		for (Region region : choosable) {
			result.add(new ChooseCommand(region));
		}
		
		return result;
	}
	
	private int getPrefferedContinentPriority(Continent continent) {
		switch (continent) {
		case Australia:     return 1;
		case South_America: return 2;
		case North_America: return 3;
		case Europe:        return 4;
		case Africa:        return 5;
		case Asia:          return 6;
		default:            return 7;
		}
	}

	// ==============
	// PLACING ARMIES
	// ==============
	
	@Override
	public List<PlaceCommand> placeArmies(long timeout) {
        List<List<PlaceCommand>> options = getAllPlacementOpts(state, getInterestingRegions(state, 3));
        List<PlaceCommand> bestCommands = null;
        int bestScore = Integer.MIN_VALUE;
        for (List<PlaceCommand> commands : options) {
            int score = evaluatePlaceCommands(state, commands);
            if (score > bestScore) {
                bestScore = score;
                bestCommands = commands;
            }
        }

		// DISTRIBUTE ARMIES
        return bestCommands;
	}

    private void removeUninterestingRegions(List<RegionState> myRegions) {
        int i = 0;
        while (i < myRegions.size() && getRegionScore(myRegions.get(i)) > 0) ++i;
        while (i < myRegions.size()) myRegions.remove(i);
    }

    private Comparator<RegionState> getRegionNeighbouringComparator() {
        return (o1, o2) -> {
            int regionScore1 = getRegionScore(o1);
            int regionScore2 = getRegionScore(o2);
            return regionScore2 - regionScore1;
            };
    }

    private int computeBestArmiesPlacement(GameState state) {
        // explore all the various placement options: all to one; all equally; two equally ==> 7 options
        List<List<PlaceCommand>> options = getAllPlacementOpts(state, getInterestingRegions(state, 3));

        //List<PlaceCommand> bestCommands = null;
        int bestScore = Integer.MIN_VALUE;
        for (List<PlaceCommand> commands : options) {
            int score = evaluatePlaceCommands(state, commands);
            if (score > bestScore) {
                bestScore = score;
                //bestCommands = commands;
            }
        }
        // return the best one
        return bestScore;
    }

    private List<RegionState> getInterestingRegions(GameState state, int numOfRegions) {
        List<RegionState> myRegions = new ArrayList<>(state.me.regions.values());
        myRegions.sort(getRegionNeighbouringComparator());
        removeUninterestingRegions(myRegions);

        return getTopRegions(myRegions, numOfRegions);
    }

    private List<List<PlaceCommand>> getAllPlacementOpts(GameState state, List<RegionState> bestRegions) {
        List<List<PlaceCommand>> options = new ArrayList<>(7);
        options.addAll(createAllArmsToOneRegCommands(bestRegions,state));
        options.add(createUniformAmrsCommands(bestRegions,state));
        if (bestRegions.size()>2) {
            options.addAll(createAllArmsToSubsetRegCommands(bestRegions,state));
        }
        return options;
    }

    private List<RegionState> getTopRegions(List<RegionState> myRegions, int NUMBER_OF_REGIONS) {
        List<RegionState> bestRegions;
        if (myRegions.size()>NUMBER_OF_REGIONS) {
            bestRegions = new ArrayList<>(myRegions.subList(0, NUMBER_OF_REGIONS));
        } else {
            bestRegions = new ArrayList<>(NUMBER_OF_REGIONS);
            bestRegions.addAll(myRegions);
        }
        return bestRegions;
    }

    private int evaluatePlaceCommands(GameState state, List<PlaceCommand> placeCommands) {
        /* for (PlaceCommand cmd : placeCommands) {
            state.apply(cmd);
        }*/

        // evaluate
        int result = computeBestArmiesMovement(state, placeCommands);

        /*for (PlaceCommand cmd : placeCommands) {
            state.revert(cmd);
        }*/

        return result;
    }

    private List<List<PlaceCommand>> createAllArmsToSubsetRegCommands(List<RegionState> bestRegions, GameState state) {
	    List<List<PlaceCommand>> result = new ArrayList<>(3);

        for (int ignoreIndex = 0; ignoreIndex <bestRegions.size(); ignoreIndex++) {
            List<PlaceCommand> commands = new ArrayList<>();
            int armiesLeft = state.me.placeArmies;
            int index = 0;
            while (armiesLeft > 0) {
                if (index != ignoreIndex) {
                    commands.add(new PlaceCommand(bestRegions.get(index).region, 1));
                    armiesLeft -= 3;
                }
                ++index;
                if (index >= bestRegions.size()) index = 0;
            }
            result.add(commands);
        }

        return result;
    }

    private List<PlaceCommand> createUniformAmrsCommands(List<RegionState> bestRegions, GameState state) {
        List<PlaceCommand> result = new ArrayList<>();
        int armiesLeft = state.me.placeArmies;
        int index = 0;
        while (armiesLeft > 0) {
            result.add(new PlaceCommand(bestRegions.get(index).region, 1));
            armiesLeft -= 3;
            ++index;
            if (index >= bestRegions.size()) index = 0;
        }
        return result;
    }

    private List<List<PlaceCommand>> createAllArmsToOneRegCommands(List<RegionState> bestRegions, GameState state) {
        List<List<PlaceCommand>> result = new ArrayList<>(3);
        for (RegionState bestRegion : bestRegions) {
            int armiesLeft = state.me.placeArmies;
            List<PlaceCommand> commands = new ArrayList<>(armiesLeft / 3 + 1);
            while (armiesLeft > 0) {
                commands.add(new PlaceCommand(bestRegion.region, 3));
                armiesLeft -= 3;
            }
            result.add(commands);
        }
        return result;
    }

    private int getRegionScore(RegionState o1) {
	    final int NEUTRAL_WEIGHT = 2;
	    final int ENEMY_WEIGHT = 5;
	    final double ARMY_STRENGTH_WEIGHT = 0.5;
		int result = 0;
		
		for (Region reg : o1.region.getNeighbours()) {
			result += (state.region(reg).owned(Player.NEUTRAL) ? 1 : 0) * NEUTRAL_WEIGHT;
			result += (state.region(reg).owned(Player.OPPONENT) ? 1 : 0) * ENEMY_WEIGHT *
                                            Math.round(1 + ARMY_STRENGTH_WEIGHT * state.region(reg).armies);
		}
		
		return result;
	}

	// =============
	// MOVING ARMIES
	// =============

	@Override
	public List<MoveCommand> moveArmies(long timeout) {
        List<RegionState> hotRegions = getInterestingRegions(state, 5);

        List<List<MoveCommand>> options = generateMoveOptions(hotRegions);

        // move all latent armies to closest hot region
        List<RegionState> coldRegions = new ArrayList<>(state.me.regions.values());
        coldRegions.removeAll(hotRegions);
        List<MoveCommand> transferCommands = generateTransferCommands(coldRegions, hotRegions);
        // each option must also bare the transfer commands
        for (List<MoveCommand> option :options) {
            option.addAll(transferCommands);
        }

        List<MoveCommand> bestCommands = null;
        int bestScore = Integer.MIN_VALUE;
        for (List<MoveCommand> commands : options) {
            int score = evaluateMoveCommands(state, EMPTY_LIST, commands);
            if (score > bestScore) {
                bestScore = score;
                bestCommands = commands;
            }
        }
        return bestCommands;
        /*
		List<MoveCommand> result = new ArrayList<>();
		
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
		
		// MOVE LEFT OVERS CLOSER TO THE FRONT
		for (RegionState from : state.me.regions.values()) {
			if (hasOnlyMyNeighbours(from) && from.armies > 1) {
				result.add(moveToFront(from));
			}
		}
		
		return result;
		*/
	}

    private int computeBestArmiesMovement(GameState state, List<PlaceCommand> placeCommands) {
        List<RegionState> hotRegions = getInterestingRegions(state, 5);

        List<List<MoveCommand>> options = generateMoveOptions(hotRegions);

        // move all latent armies to closest hot region
        List<RegionState> coldRegions = new ArrayList<>(state.me.regions.values());
        coldRegions.removeAll(hotRegions);
        List<MoveCommand> transferCommands = generateTransferCommands(coldRegions, hotRegions);
        // each option must also bare the transfer commands
        for (List<MoveCommand> option :options) {
            option.addAll(transferCommands);
        }

        // return the best score achievable
        //List<MoveCommand> bestCommands = null;
        int bestScore = Integer.MIN_VALUE;
        for (List<MoveCommand> commands : options) {
            int score = evaluateMoveCommands(state, placeCommands,commands);
            if (score > bestScore) {
                bestScore = score;
                //bestCommands = commands;
            }
        }

        return bestScore;
    }

    private List<List<MoveCommand>> generateMoveOptions(List<RegionState> hotRegions) {
        List<List<List<MoveCommand>>> allRegionOptions = new ArrayList<>(hotRegions.size());
        // evaluate them
        for (RegionState myRegion : hotRegions) {
            List<RegionState> neighbours = new ArrayList<>(myRegion.neighbours.length);
            List<List<MoveCommand>> regionOptions = new ArrayList<>(myRegion.neighbours.length*2);
            final int availableArmies = myRegion.armies - 1;
            for (RegionState to : myRegion.neighbours) {
                // DO NOT ATTACK OWN REGIONS
                if (to.owned(Player.ME)) continue;
                neighbours.add(to);
            }
            // make commands for regions and doubles of regs
            // singles
            for (RegionState neighbour : neighbours) {
                regionOptions.add(singletonList(new MoveCommand(myRegion.region, neighbour.region, availableArmies)));
            }

            // doubles
            for (int regIndex = 0; regIndex < neighbours.size()-1; regIndex++) {
                for (int restIndex = regIndex+1; restIndex < neighbours.size(); restIndex++) {
                    regionOptions.add(Arrays.asList(
                            new MoveCommand(myRegion.region, neighbours.get(regIndex).region, Math.round(availableArmies/2)),
                            new MoveCommand(myRegion.region, neighbours.get(restIndex).region, Math.round(availableArmies/2))
                    ));
                }
            }
            allRegionOptions.add(regionOptions);
        }

        // make all combinations of commands for all regs
        List<List<MoveCommand>> options = new ArrayList<>();
        generatePermutations(allRegionOptions, options, 0, new ArrayList<>());
        return options;
    }

    private List<MoveCommand> generateTransferCommands(List<RegionState> coldRegions, List<RegionState> hotRegions) {
        List<MoveCommand> result = new ArrayList<>();
	    for (RegionState coldRegion : coldRegions) {
	        // find closest hot regions
            MoveCommand newCommand = moveToFront(coldRegion, hotRegions);
            if (newCommand == null) {
                System.err.println("MoveToFront method failed to find way to front!");
            } else {
                result.add(newCommand);
            }
        }
        return result;
    }

    private void generatePermutations(List<List<List<MoveCommand>>> Lists, List<List<MoveCommand>> result, int depth, List<MoveCommand>current)
    {
        if(depth == Lists.size()) {
            result.add(current);
            return;
        }

        for(int i = 0; i < Lists.get(depth).size(); ++i) {
            current.addAll(Lists.get(depth).get(i));
            generatePermutations(Lists, result, depth + 1, current);
        }
    }

    private int evaluateMoveCommands(GameState state, List<PlaceCommand> placeCommands, List<MoveCommand> moveCommands) {
        /*for (MoveCommand cmd : moveCommands) {
            state.apply(cmd);
        }*/

        // evaluate
        int result = computeOpponentMove(state, placeCommands, moveCommands);

        /*for (MoveCommand cmd : moveCommands) {
            state.revert(cmd);
        }*/

        return result;
    }

    private int computeOpponentMove(GameState state, List<PlaceCommand> placeCommands, List<MoveCommand> moveCommands) {
        // simulate opponent move
        final int IGNORED_TIMEOUT = 0;
        smartBot.enforceState(state);
        List<PlaceCommand> oponentPlace = smartBot.placeArmies(IGNORED_TIMEOUT);
        List<MoveCommand> oponentMove = smartBot.moveArmies(IGNORED_TIMEOUT);

        // simulate simulator computation
        int result = runSimulator(state, placeCommands, moveCommands, oponentPlace, oponentMove);

        return result;
    }

    private int runSimulator(GameState state,
                             List<PlaceCommand> placeCommands, List<MoveCommand> moveCommands,
                             List<PlaceCommand> oponentPlace, List<MoveCommand> oponentMove) {
	    // TODO

	    // check for clashing commands

        // apply non-clashing commands

        // branch on clashing commands

        // branch on battles that could have two outcomes

        // weight results for one outcome

        // ADD END OF RECURSION

        // revert applied commands

        return 0;
    }

	private MoveCommand transfer(RegionState from, RegionState to) {
        return new MoveCommand(from.region, to.region, from.armies-1);
	}
	
	private Region moveToFrontRegion;
	
	private MoveCommand moveToFront(RegionState from, List<RegionState> front) {
		RegionBFS<BFSNode> bfs = new RegionBFS<>();
		moveToFrontRegion = null;
		bfs.run(from.region, (Region region, int level, BFSNode parent, BFSNode thisNode) -> {
            //System.err.println((parent == null ? "START" : parent.level + ":" + parent.region) + " --> " + level + ":" + region);
            if (front.contains(state.region(region))) {
                moveToFrontRegion = region;
                return new BFSVisitResult<>(BFSVisitResultType.TERMINATE, thisNode == null ? new BFSNode() : thisNode);
            }
            return new BFSVisitResult<>(thisNode == null ? new BFSNode() : thisNode);
        });
		
		if (moveToFrontRegion != null) {
			List<Region> path = bfs.getAllPaths(moveToFrontRegion).get(0);
			Region moveTo = path.get(1);

			boolean first = true;
			for (Region region : path) {
				if (first) first = false;
				else System.err.print(" --> ");
				System.err.print(region);
			}
			System.err.println();
			return transfer(from, state.region(moveTo));
		}
		return null;
	}
	
	
	@SuppressWarnings("WeakerAccess")
    public static void runInternal() {
		Config config = new Config();
		
		config.bot1Init = "internal:conquest.bot.playground.ConquestBot";
		//config.bot1Init = "dir;process:../Conquest-Bots;java -cp ./bin;../Conquest/bin conquest.bot.external.JavaBot conquest.bot.playground.ConquestBot ./ConquestBot.log";
		config.bot2Init = "internal:conquest.bot.BotStarter";
        //config.bot2Init = "internal:conquest.bot.custom.SmartBot";
		//config.bot2Init = "human";
		
		config.engine.botCommandTimeoutMillis = 24*60*60*1000;
		//config.engine.botCommandTimeoutMillis = 20 * 1000;
		
		config.engine.maxGameRounds = 200;
		
		config.engine.fight = FightMode.CONTINUAL_1_1_A60_D70;
		
		config.visualize = true;
		config.forceHumanVisualization = true; // prepare for hijacking bot controls
		
		config.replayLog = new File("./replay.log");
		
		RunGame run = new RunGame(config);
		GameResult result = run.go();
		
		System.exit(0);
	}
	
	public static void runExternal() {
		BotParser parser = new BotParser(new ConquestBot());
		parser.setLogFile(new File("./ConquestBot.log"));
		parser.run();
	}

	public static void main(String[] args)
	{
		//JavaBot.exec(new String[]{"conquest.bot.custom.AggressiveBot", "./AggressiveBot.log"});
		
		runInternal();
	}

}
