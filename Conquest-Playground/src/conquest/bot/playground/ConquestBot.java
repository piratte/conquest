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
import conquest.bot.state.compact.GameStateCompact;
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
import java.util.stream.Collectors;


import static java.util.Collections.*;

/**
 * Copy-paste of {@link AggressiveBot}. Feel free to fool around!
 * And be sure to check {@link FightSimulation} that provides fight-victory-probabilities for this bot.
 */
public class ConquestBot extends GameBot
{
    private static final int DEPTH_LIMIT = 1;
    private FightAttackersResults aRes;
	private FightDefendersResults dRes;

	private SmartBot smartBot = new SmartBot();
	
	public ConquestBot() {
        System.err.println("---==[ BLABLA BOT INITIALIZING ]==---");
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
        double bestScore = Integer.MIN_VALUE;
        for (List<PlaceCommand> commands : options) {
            double score = evaluatePlaceCommands(new GameState(GameStateCompact.fromGameState(state)), 0, commands);
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

    private double computeBestArmiesPlacement(GameState state, int depth) {
	    // TODO: add alpha-beta prunning

        // explore all the various placement options: all to one; all equally; two equally ==> 7 options
        List<List<PlaceCommand>> options = getAllPlacementOpts(state, getInterestingRegions(state, 2));

        //List<PlaceCommand> bestCommands = null;
        double bestScore = Integer.MIN_VALUE;
        for (List<PlaceCommand> commands : options) {
            double score = evaluatePlaceCommands(state, depth, commands);
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
        options.add(createUniformAmrsCommands(state, bestRegions));
        if (bestRegions.size()>2) {
            options.addAll(createAllArmsToSubsetRegCommands(state, bestRegions));
        }
        return options;
    }

    private List<RegionState> getTopRegions(List<RegionState> myRegions, int NUMBER_OF_REGIONS) {
        List<RegionState> bestRegions;
        if (myRegions.size()>NUMBER_OF_REGIONS) {
            bestRegions = new ArrayList<>(myRegions.subList(0, NUMBER_OF_REGIONS));
        } else {
            bestRegions = new ArrayList<>(myRegions.size());
            bestRegions.addAll(myRegions);
        }
        return bestRegions;
    }

    private double evaluatePlaceCommands(final GameState state, int depth, List<PlaceCommand> placeCommands) {
	    // applying place commands here, since there's no relevance in their order and we need to know their effect
        // so we can move the newly placed armies

        for (PlaceCommand cmd : placeCommands) {
            state.apply(cmd);
        }

        // evaluate
        double result = computeBestArmiesMovement(state, depth);

        for (PlaceCommand cmd : placeCommands) {
            state.revert(cmd);
        }

        return result;
    }

    private List<List<PlaceCommand>> createAllArmsToSubsetRegCommands(final GameState state, List<RegionState> bestRegions) {
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

    private List<PlaceCommand> createUniformAmrsCommands(final GameState state, List<RegionState> bestRegions) {
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
            while (armiesLeft >= 3) {
                commands.add(new PlaceCommand(bestRegion.region, 3));
                armiesLeft -= 3;
            }
            commands.add(new PlaceCommand(bestRegion.region, armiesLeft));
            result.add(commands);
        }
        return result;
    }

    private int getRegionScore(RegionState regionToScore) {
	    final int NEUTRAL_WEIGHT = 2;
	    final int ENEMY_WEIGHT = 5;
	    final double ARMY_STRENGTH_WEIGHT = 0.5;
	    final double OWN_ARMY_STRENGTH_WEIGHT = 0.5;
		int result = 0;
		
		for (Region reg : regionToScore.region.getNeighbours()) {
			result += (state.region(reg).owned(Player.NEUTRAL) ? 1 : 0) * NEUTRAL_WEIGHT;
			result += (state.region(reg).owned(Player.OPPONENT) ? 1 : 0) * ENEMY_WEIGHT *
                                            Math.round(1 + ARMY_STRENGTH_WEIGHT * state.region(reg).armies);
		}
		result += regionToScore.armies * OWN_ARMY_STRENGTH_WEIGHT;
		
		return result;
	}

	// =============
	// MOVING ARMIES
	// =============

	@Override
	public List<MoveCommand> moveArmies(long timeout) {
        List<RegionState> hotRegions = getInterestingRegions(state, 3);

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
        double bestScore = Integer.MIN_VALUE;
        for (List<MoveCommand> commands : options) {
            double score = evaluateMoveCommands(new GameState(GameStateCompact.fromGameState(state)), 0, commands);
            if (score > bestScore) {
                bestScore = score;
                bestCommands = commands;
            }
        }
        return bestCommands;
	}

    private double computeBestArmiesMovement(final GameState state, int depth) {
        List<RegionState> hotRegions = getInterestingRegions(state, 3);

        List<List<MoveCommand>> options = generateMoveOptions(hotRegions);

        // move all latent armies to closest hot region
        List<RegionState> coldRegions = new ArrayList<>(state.me.regions.values());
        coldRegions.removeAll(hotRegions);
        List<MoveCommand> transferCommands = generateTransferCommands(coldRegions, hotRegions);
        // each option must also bare the transfer commands
        if (!transferCommands.isEmpty()) {
            for (List<MoveCommand> option : options) {
                option.addAll(transferCommands);
            }
        }

        // return the best score achievable
        //List<MoveCommand> bestCommands = null;
        double bestScore = Integer.MIN_VALUE;
        for (List<MoveCommand> commands : options) {
            double score = evaluateMoveCommands(state, depth, commands);
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
            if (availableArmies < 2) continue;
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
            int halfArmy = Math.floorDiv(availableArmies, 2);
            if (halfArmy > 2) {
                for (int regIndex = 0; regIndex < neighbours.size()-1; regIndex++) {
                    for (int restIndex = regIndex+1; restIndex < neighbours.size(); restIndex++) {
                        regionOptions.add(Arrays.asList(
                                new MoveCommand(myRegion.region, neighbours.get(regIndex).region, halfArmy),
                                new MoveCommand(myRegion.region, neighbours.get(restIndex).region, halfArmy)
                        ));
                    }
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
            //MoveCommand newCommand = moveToFront(coldRegion);
            MoveCommand newCommand = moveToFront(coldRegion, hotRegions);
            if (newCommand == null) {
                System.err.println("MoveToFront method failed to find way to front!");
            } else {
                result.add(newCommand);
            }
        }
        //System.err.println(String.format("Returning %d transfer commands", result.size()));
        return result;
    }

    private void generatePermutations(List<List<List<MoveCommand>>> Lists, List<List<MoveCommand>> result, int depth, List<MoveCommand> current)
    {

        if(depth == Lists.size()) {
            result.add(new ArrayList<>(current));
            return;
        }

        for(int i = 0; i < Lists.get(depth).size(); ++i) {
            current.addAll(Lists.get(depth).get(i));
            generatePermutations(Lists, result, depth + 1, current);
            current.removeAll(Lists.get(depth).get(i));
        }
    }

    private double evaluateMoveCommands(final GameState state, int depth, List<MoveCommand> moveCommands) {
        return computeOpponentMove(state, depth, moveCommands);
    }

    private double computeOpponentMove(final GameState state, int depth, List<MoveCommand> moveCommands) {

	    GameState stateForOpp = new GameState(GameStateCompact.fromGameState(state));
	    stateForOpp.swapPlayers();

        List<List<PlaceCommand>> placementOpts = getAllPlacementOpts(stateForOpp, getInterestingRegions(stateForOpp, 2));

        //List<PlaceCommand> bestCommands = null;
        double bestScore = Integer.MAX_VALUE;
        for (List<PlaceCommand> placeOption : placementOpts) {
            for (PlaceCommand placeCmd : placeOption) stateForOpp.apply(placeCmd);

            List<RegionState> hotRegions = getInterestingRegions(stateForOpp, 3);

            List<List<MoveCommand>> moveOptions = generateMoveOptions(hotRegions);

            // move all latent armies to closest hot region
            List<RegionState> coldRegions = new ArrayList<>(stateForOpp.me.regions.values());
            coldRegions.removeAll(hotRegions);
            List<MoveCommand> transferCommands = generateTransferCommands(coldRegions, hotRegions);
            // each option must also bare the transfer commands
            if (!transferCommands.isEmpty()) {
                for (List<MoveCommand> option : moveOptions) {
                    option.addAll(transferCommands);
                }
            }

            for (List<MoveCommand> moveOption : moveOptions) {
                double score = runSimulator(state, depth, moveCommands, placeOption,
                        moveOption.stream().filter(moveCommand -> moveCommand.armies > 0).collect(Collectors.toList()));
                if (score < bestScore)
                    bestScore = score;
            }

            for (PlaceCommand placeCmd : placeOption) stateForOpp.revert(placeCmd);
        }
        return bestScore;
    }

    /*
    private double computeOpponentMove(final GameState state, int depth, List<MoveCommand> moveCommands) {
        // simulate opponent move
        final int IGNORED_TIMEOUT = 0;
        GameState stateForOpp = new GameState(GameStateCompact.fromGameState(state));
        stateForOpp.swapPlayers();
        smartBot.enforceState(stateForOpp);
        List<PlaceCommand> oponentPlace = smartBot.placeArmies(IGNORED_TIMEOUT);
        List<MoveCommand> oponentMove = smartBot.moveArmies(IGNORED_TIMEOUT);
        /*
        stateForOpp = new GameState(GameStateCompact.fromGameState(state));
        stateForOpp.swapPlayers();
        for (PlaceCommand cmd : oponentPlace) stateForOpp.apply(cmd);
        for (MoveCommand cmd : oponentMove) stateForOpp.apply(cmd);
        for (MoveCommand cmd : oponentMove) stateForOpp.revert(cmd);
        for (PlaceCommand cmd : oponentPlace) stateForOpp.revert(cmd);
        stateForOpp.swapPlayers();
        if (!state.toString().equalsIgnoreCase(stateForOpp.toString()))
            System.err.println("Opponent commands cannot be inverted");

        // simulate simulator computation
        return runSimulator(state, depth, moveCommands, oponentPlace, oponentMove);
    }*/


    private double runSimulator(GameState state, int depth, List<MoveCommand> moveCommands,
                             List<PlaceCommand> oponentPlace, List<MoveCommand> oponentMove) {
        state = new GameState(GameStateCompact.fromGameState(state));
        Map<Region, List<MoveCommand>> destinationRegMap = new HashMap<>();
        List<MoveCommand> toBeRemovedOpp = new ArrayList<>();
        List<MoveCommand> toBeRemovedMe = new ArrayList<>();
	    // check for clashing commands
        for (MoveCommand myMove : moveCommands) {
            // get commands where order matters
            boolean myMoveClashes = false;
            for (MoveCommand opoMove : oponentMove) {
                if (state.region(myMove.to) == state.region(opoMove.to)) {
                    myMoveClashes = true;
                    if (!destinationRegMap.containsKey(myMove.to)) {
                        destinationRegMap.put(myMove.to, new ArrayList<>());
                        destinationRegMap.get(myMove.to).add(myMove);
                    }
                    destinationRegMap.get(myMove.to).add(opoMove);
                    // remove clashing commands from their respective collections
                    toBeRemovedOpp.add(opoMove);
                }
            }
            if (myMoveClashes)
                toBeRemovedMe.add(myMove);
        }
        moveCommands.removeAll(toBeRemovedMe);
        toBeRemovedMe.clear();
        oponentMove.removeAll(toBeRemovedOpp);
        toBeRemovedOpp.clear();

        ArrayList<Pair<BattleResult, BattleResult>> battleResults = new ArrayList<>((moveCommands.size() + oponentMove.size())*2);
        for (MoveCommand myMove : moveCommands) {
            // get commands that lead to battles on my side
            if (!state.region(myMove.to).owned(Player.ME)) {
                // create various attack command (= battle results)
                battleResults.add(createBattleResults(state, myMove));
                toBeRemovedMe.add(myMove);
            }
        }
        moveCommands.removeAll(toBeRemovedMe);

        // same for the opponent
        for (MoveCommand opoMove : oponentMove) {
            if (!state.region(opoMove.to).owned(Player.OPPONENT)) {
                battleResults.add(createBattleResults(state, opoMove));
                toBeRemovedOpp.add(opoMove);
            }
        }
        oponentMove.removeAll(toBeRemovedOpp);

        // get a pair of BattleResults for each region with clashing commands
        for (Map.Entry<Region, List<MoveCommand>> regionWithCmds : destinationRegMap.entrySet()) {
            battleResults.add(computeResultOfClashingCommands(state, regionWithCmds));
        }

        // apply non-clashing commands
        for (PlaceCommand placeCmd : oponentPlace) state.apply(placeCmd);
        for (MoveCommand myMove : moveCommands) state.apply(myMove);
        for (MoveCommand opoMove : oponentMove) state.apply(opoMove);

        double stateValue = 0;
        for (int battleResCode = 0; battleResCode < Math.pow(2, battleResults.size()); battleResCode++) {
            double probability = 1;
            //GameState curState = new GameState(GameStateCompact.fromGameState(state));

            // applying all attack commands to project the battle results
            for (int battleIndex = 0; battleIndex < battleResults.size(); battleIndex++) {
                List<AttackCommand> cmdsToApply;
                if (!getNthBit(battleResCode, battleIndex)) {
                    cmdsToApply = battleResults.get(battleIndex).getFirst().getResultCommand();
                    probability *= battleResults.get(battleIndex).getFirst().getProbability();
                } else {
                    cmdsToApply = battleResults.get(battleIndex).getSecond().getResultCommand();
                    probability *= battleResults.get(battleIndex).getSecond().getProbability();
                }
                for (int i = 0; i < cmdsToApply.size(); i++) {
                    state.apply(cmdsToApply.get(i));
                }
            }

            if (depth > DEPTH_LIMIT) {
                //System.err.println("Depth limit reached, " + battleResCode);
                stateValue += probability * evaluateState(state);
            } else {
                if (depth == 0)
                    System.err.println("Code: " + battleResCode);
                //System.err.println("Continuing into recursion");
                stateValue += probability * computeBestArmiesPlacement(state, depth+1);
            }

            // revert applied commands
            for (int battleIndex = battleResults.size()-1; battleIndex >= 0; battleIndex--) {
                if (!getNthBit(battleResCode, battleIndex)) {
                    revertAttackCmds(state, battleResults.get(battleIndex).getFirst().getResultCommand());
                } else {
                    revertAttackCmds(state, battleResults.get(battleIndex).getSecond().getResultCommand());
                }
            }
        }

        for (MoveCommand opoMove : oponentMove) state.revert(opoMove);
        for (MoveCommand myMove : moveCommands) state.revert(myMove);
        for (PlaceCommand placeCmd : oponentPlace) state.revert(placeCmd);

        return stateValue;
    }

    private void revertAttackCmds(GameState state, List<AttackCommand> resultCommand) {
        for (int cmdIndex = resultCommand.size()-1; cmdIndex >= 0; cmdIndex--) {
            state.revert(resultCommand.get(cmdIndex));
        }
    }

    private double evaluateState(GameState state) {
	    final int CONTINENT_MULTIPLIER = 1000;
        final int ARMY_MULTIPLIER = 10;
	    int result = 0;

	    for (GameState.ContinentState continent : state.player(Player.ME).continents.values()) {
	        if (continent.ownedBy(Player.ME)) { // not sure if this if is necessary
	            result += continent.continent.reward * CONTINENT_MULTIPLIER;
            }
        }

        result += state.player(Player.ME).totalArmies * ARMY_MULTIPLIER;

        return result;
    }

    private boolean getNthBit(int number, int bitNumber) {
        return ((number >> bitNumber) & 1) == 1;
    }

    private Pair<BattleResult, BattleResult> computeResultOfClashingCommands(GameState state, Map.Entry<Region, List<MoveCommand>> regionWithCmds) {
	    int meArmies = 0;
	    int opoArmies = 0;
	    int neutralArmies = 0;

	    for (MoveCommand cmd : regionWithCmds.getValue()) {
	        if (getIssuer(state, cmd) == Player.ME) {
	            meArmies += cmd.armies;
            } else {
	            opoArmies += cmd.armies;
            }
        }

        // heuristic to incorporate destination region ownership
        switch (state.region(regionWithCmds.getKey()).owner.player) {
            case ME:
                meArmies += state.region(regionWithCmds.getKey()).armies;
                break;
            case OPPONENT:
                opoArmies += state.region(regionWithCmds.getKey()).armies;
                break;
            case NEUTRAL:
                neutralArmies = state.region(regionWithCmds.getKey()).armies;
                break;
        }

        // from here we will be pessimistic and assume that the opponent will be the defending
        int meWinDeaths = (int) Math.round(aRes.getExpectedAttackersDeaths(meArmies, opoArmies + neutralArmies));
        if (meWinDeaths >= meArmies) meWinDeaths = meArmies -1;
        int defWinDeaths = (int) Math.round(dRes.getExpectedDefendersDeaths(meArmies, opoArmies + neutralArmies));
        if (defWinDeaths >= opoArmies) defWinDeaths = opoArmies -1;

        return new Pair<>(
                new BattleResult(aRes.getAttackersWinChance(meArmies, opoArmies),
                        createBattleScenario(state, regionWithCmds, meWinDeaths, opoArmies, Player.ME, Player.OPPONENT)),
                new BattleResult(dRes.getDefendersWinChance(meArmies, opoArmies),
                        createBattleScenario(state, regionWithCmds, defWinDeaths, meArmies, Player.OPPONENT, Player.ME))
        );
    }

    private List<AttackCommand> createBattleScenario(GameState state, Map.Entry<Region, List<MoveCommand>> regionWithCmds, int winnerDeaths, int looserArmies, Player winner, Player looser) {
        List<AttackCommand> result = new ArrayList<>(regionWithCmds.getValue().size());
        boolean firstOpponentCmd = true;
        List<MoveCommand> winnerMoves = new ArrayList<>(regionWithCmds.getValue().size());
        for (MoveCommand cmd : regionWithCmds.getValue()) {
            if (getIssuer(state, cmd) == winner) {
                winnerMoves.add(cmd);
                continue;
            }
            // prvnim tahem zmenime vlastnictvi pro oponenta bez ztrat
            if (firstOpponentCmd) {
                firstOpponentCmd = false;
                result.add(new AttackCommand(
                        cmd.from,
                        looser,
                        regionWithCmds.getKey(),
                        state.region(regionWithCmds.getKey()).owner.player,
                        cmd.armies,
                        0,
                        state.region(regionWithCmds.getKey()).armies
                ));
            } else {
                // dale natahame vsechny jeho armady do regionu bez ztrat
                result.add(new AttackCommand(
                        cmd.from,
                        looser,
                        regionWithCmds.getKey(),
                        looser,
                        cmd.armies,
                        0,0
                ));
            }
        }

        int winnerDeathsSoFar = 0;
        for (MoveCommand cmd : winnerMoves) {
            // aplikuju vsechny moje commandy neuspesne, dokud se me ztraty nenaplni, posledni z nich s plnymi ztratami nepritele
            if (winnerDeathsSoFar < winnerDeaths) {
                int cmdDeaths = cmd.armies;
                winnerDeathsSoFar += cmdDeaths;
                int defDeaths = 0;
                if (winnerDeathsSoFar > winnerDeaths) {
                    cmdDeaths -= winnerDeathsSoFar - winnerDeaths;
                    defDeaths = looserArmies;
                }
                result.add(new AttackCommand(
                        cmd.from,
                        winner,
                        regionWithCmds.getKey(),
                        looser,
                        cmd.armies,
                        cmdDeaths,
                        defDeaths
                ));
            } else {
                // aplikuju vsechny me zbyvajici commandy bez mych ztrat
                result.add(new AttackCommand(
                        cmd.from,
                        winner,
                        regionWithCmds.getKey(),
                        winner,
                        cmd.armies,
                        0,0
                ));
            }
        }

        debugTryToApply(state, result);
        return result;
    }

    private void debugTryToApply(GameState state, List<AttackCommand> commands) {
        return;/*
	    GameState originalState = new GameState(GameStateCompact.fromGameState(state));
        for (int i = 0; i < commands.size(); i++) {
            originalState.apply(commands.get(i));
        }
        for (int i = commands.size()-1; i >= 0 ; i--) {
            originalState.revert(commands.get(i));
        }
        if (!state.toString().equalsIgnoreCase(originalState.toString()))
	        System.err.println("Yay!");*/
    }

    private Player getIssuer(GameState state, MoveCommand cmd) {
	    return state.region(cmd.from).owner.player;
    }

    private Pair<BattleResult, BattleResult> createBattleResults(GameState state, MoveCommand moveCmd) {
        Region region = moveCmd.to;
	    int defArms = state.region(region).armies;
	    int attArms = moveCmd.armies;
	    //System.err.println(String.format("defArms: %d ; attArms: %d", defArms, attArms));

	    if (defArms < 1 || attArms < 1) {
	        // FIXME: this fires a lot, maybe fix somehow...
	        //System.err.println("O def or att arms");
        }

	    // compute probability of winning and loosing
        int attWinDeaths = (int) Math.round(aRes.getExpectedAttackersDeaths(attArms, defArms));
        if (attWinDeaths >= attArms) attWinDeaths = attArms-1;
        double attWinChance = aRes.getAttackersWinChance(attArms, defArms);

        int defWinDeaths = (int) Math.round(dRes.getExpectedDefendersDeaths(attArms, defArms));
        if (defWinDeaths >= defArms) defWinDeaths = defArms -1;

        Pair<BattleResult, BattleResult> result = new Pair<>(
                new BattleResult(attWinChance,
                        new AttackCommand(
                                moveCmd.from, state.region(moveCmd.from).owner.player,
                                moveCmd.to, state.region(moveCmd.to).owner.player,
                                moveCmd.armies,
                                attWinDeaths,
                                state.region(moveCmd.to).armies)),
                new BattleResult(1-attWinChance,
                        new AttackCommand(
                                moveCmd.from, state.region(moveCmd.from).owner.player,
                                moveCmd.to, state.region(moveCmd.to).owner.player,
                                moveCmd.armies,
                                attArms,
                                defWinDeaths
                        )));

        debugTryToApply(state, result.getFirst().getResultCommand());
        debugTryToApply(state, result.getSecond().getResultCommand());

        return result;
    }

    private MoveCommand transfer(RegionState from, RegionState to) {
        return new MoveCommand(from.region, to.region, from.armies-1);
	}


    private Region moveToFrontRegion;
    private MoveCommand moveToFront(RegionState from, List<RegionState> hotRegions) {
        RegionBFS<BFSNode> bfs = new RegionBFS<>();
        moveToFrontRegion = null;
        bfs.run(from.region, new RegionBFS.BFSVisitor<BFSNode>() {

            @Override
            public BFSVisitResult<BFSNode> visit(Region region, int level, BFSNode parent, BFSNode thisNode) {
                //System.err.println((parent == null ? "START" : parent.level + ":" + parent.region) + " --> " + level + ":" + region);
                if (isFrontalRegion(region, hotRegions)) {
                    moveToFrontRegion = region;
                    return new BFSVisitResult<>(BFSVisitResultType.TERMINATE, thisNode == null ? new BFSNode() : thisNode);
                }
                return new BFSVisitResult<>(thisNode == null ? new BFSNode() : thisNode);
            }

            private boolean isFrontalRegion(Region from, List<RegionState> hotRegions) {
                for (RegionState hotRegState : hotRegions) {
                    if (hotRegState.region == from) {
                        return true;
                    }
                }
                return false;
            }

        });

        if (moveToFrontRegion != null) {
            //List<Region> path = fw.getPath(from.getRegion(), moveToFrontRegion);
            List<Region> path = bfs.getAllPaths(moveToFrontRegion).get(0);
            Region moveTo = path.get(1);

            boolean first = true;
            for (Region region : path) {
                if (first) first = false;
                //else System.err.print(" --> ");
                //System.err.print(region);
            }
            //System.err.println();
            return transfer(from, state.region(moveTo));
        }
        return null;
    }

	
	@SuppressWarnings("WeakerAccess")
    public static void runInternal() {
		Config config = new Config();
		//config.bot1Init = "internal:conquest.bot.playground.ConquestBot";
		config.bot1Init = "internal:" + ConquestBot.class.getCanonicalName();
		System.err.println(config.bot1Init);
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
