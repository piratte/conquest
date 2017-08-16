package conquest.bot.playground;

import conquest.bot.BotParser;
import conquest.bot.custom.AggressiveBot;
import conquest.bot.fight.FightSimulation;
import conquest.bot.fight.FightSimulation.FightAttackersResults;
import conquest.bot.fight.FightSimulation.FightDefendersResults;
import conquest.bot.map.RegionBFS;
import conquest.bot.map.RegionBFS.BFSNode;
import conquest.bot.map.RegionBFS.BFSVisitResult;
import conquest.bot.map.RegionBFS.BFSVisitResultType;
import conquest.bot.state.ChooseCommand;
import conquest.bot.state.GameBot;
import conquest.bot.state.GameState.RegionState;
import conquest.bot.state.MoveCommand;
import conquest.bot.state.PlaceCommand;
import conquest.engine.Engine.FightMode;
import conquest.engine.RunGame;
import conquest.engine.RunGame.Config;
import conquest.engine.RunGame.GameResult;
import conquest.game.Player;
import conquest.game.world.Continent;
import conquest.game.world.Region;
import conquest.view.GUI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Copy-paste of {@link AggressiveBot}. Feel free to fool around!
 * And be sure to check {@link FightSimulation} that provides fight-victory-probabilities for this bot.
 */
public class ConquestBot extends GameBot
{
	
	FightAttackersResults aRes;
	FightDefendersResults dRes;
	
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
	    System.err.println("Place armies timeout: " + timeout);
		List<PlaceCommand> result = new ArrayList<>();
		
		// CLONE REGIONS OWNED BY ME
		List<RegionState> mine = new ArrayList<>(state.me.regions.values());
		
		// SORT THEM ACCORDING TO THEIR SCORE
		mine.sort((o1, o2) -> {
            int regionScore1 = getRegionScore(o1);
            int regionScore2 = getRegionScore(o2);
            return regionScore2 - regionScore1;
        });
		
		// DO NOT ADD SOLDIER TO REGIONS THAT HAS SCORE 0 (not perspective)
		int i = 0;
		while (i < mine.size() && getRegionScore(mine.get(i)) > 0) ++i;
		while (i < mine.size()) mine.remove(i);

		// DISTRIBUTE ARMIES
		int armiesLeft = state.me.placeArmies;
		
		int index = 0;
		
		while (armiesLeft > 0) {
			result.add(new PlaceCommand(mine.get(index).region, 3));
			armiesLeft -= 3;
			++index;
			if (index >= mine.size()) index = 0;
		}
		
		return result;
	}
	
	private int getRegionScore(RegionState o1) {
		int result = 0;
		
		for (Region reg : o1.region.getNeighbours()) {
			result += (state.region(reg).owned(Player.NEUTRAL) ? 1 : 0) * 5;
			result += (state.region(reg).owned(Player.OPPONENT) ? 1 : 0) * 2;
		}
		
		return result;
	}

	// =============
	// MOVING ARMIES
	// =============

	@Override
	public List<MoveCommand> moveArmies(long timeout) {
        System.err.println("Move armies timeout: " + timeout);
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
	}
	
	private boolean hasOnlyMyNeighbours(RegionState from) {
		for (RegionState region : from.neighbours) {
			if (!region.owned(Player.ME)) return false;
		}
		return true;
	}

	private int getRequiredSoldiersToConquerRegion(RegionState from, RegionState to, double winProbability) {
		int attackers = from.armies - 1;
		int defenders = to.armies;
		
		for (int a = defenders; a <= attackers; ++a) {
			double chance = aRes.getAttackersWinChance(a, defenders);
			if (chance >= winProbability) {
				return a;
			}
		}
		
		return Integer.MAX_VALUE;
	}
		
	private boolean shouldAttack(RegionState from, RegionState to, double winProbability) {	
		return from.armies > getRequiredSoldiersToConquerRegion(from, to, winProbability);
	}
	
	private MoveCommand attack(RegionState from, RegionState to, double winProbability) {
        return new MoveCommand(from.region, to.region, getRequiredSoldiersToConquerRegion(from, to, winProbability));
	}
	
	private MoveCommand transfer(RegionState from, RegionState to) {
        return new MoveCommand(from.region, to.region, from.armies-1);
	}
	
	private Region moveToFrontRegion;
	
	private MoveCommand moveToFront(RegionState from) {
		RegionBFS<BFSNode> bfs = new RegionBFS<>();
		moveToFrontRegion = null;
		bfs.run(from.region, (region, level, parent, thisNode) -> {
            //System.err.println((parent == null ? "START" : parent.level + ":" + parent.region) + " --> " + level + ":" + region);
            if (!hasOnlyMyNeighbours(state.region(region))) {
                moveToFrontRegion = region;
                return new BFSVisitResult<>(BFSVisitResultType.TERMINATE, thisNode == null ? new BFSNode() : thisNode);
            }
            return new BFSVisitResult<>(thisNode == null ? new BFSNode() : thisNode);
        });
		
		if (moveToFrontRegion != null) {
			//List<Region> path = fw.getPath(from.getRegion(), moveToFrontRegion);
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
