package conquest.bot.custom;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import conquest.bot.BotParser;
import conquest.bot.fight.FightSimulation.FightAttackersResults;
import conquest.bot.fight.FightSimulation.FightDefendersResults;
import conquest.bot.map.RegionBFS;
import conquest.bot.map.RegionBFS.BFSNode;
import conquest.bot.map.RegionBFS.BFSVisitResult;
import conquest.bot.map.RegionBFS.BFSVisitResultType;
import conquest.bot.map.RegionBFS.BFSVisitor;
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

/**
 * MFF UK AI1 LABS 2015 - BEST CONQUEST BOT
 *  
 * Latest AI1 labs: bit.ly/mff-uk-ai1-labs
 * 
 * @author Federico Forti (fe0437serio@gmail.com) 
 */
public class SmartBot extends GameBot 
{
	
	FightAttackersResults aRes;
	FightDefendersResults dRes;
	
	List<PlaceCommand> placeCommands;
	boolean start = true;
	
	
	public SmartBot() {		
		aRes = FightAttackersResults.loadFromFile(new File("FightSimulation-Attackers-A200-D200.obj"));
		dRes = FightDefendersResults.loadFromFile(new File("FightSimulation-Defenders-A200-D200.obj"));
		System.err.println("---==[ SMART BOT INITIALIZED ]==---");
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
		Collections.sort(choosable, new Comparator<Region>() {
			@Override
			public int compare(Region o1, Region o2) {
				int priority1 = getPrefferedContinentPriority(o1.continent);
				int priority2 = getPrefferedContinentPriority(o2.continent);				
				return priority1 - priority2;
			}
		});
		
		// REMOVE CONTINENT WE DO NOT WANT
		while (choosable.size() > m) choosable.remove(choosable.size()-1);
		
		// CREATE COMMANDS
		List<ChooseCommand> result = new ArrayList<ChooseCommand>(choosable.size());
		for (Region region : choosable) {
			result.add(new ChooseCommand(region));
		}
		
		return result;
	}
	
	public int getPrefferedContinentPriority(Continent continent) {
		switch (continent) {
		
		case South_America: return 1;
		case Africa:        return 2;
		case Australia:     return 3;
		case Europe:        return 4;
		case North_America: return 5;
		case Asia:          return 6;
		default:            return 7;
		}
	}

	// ==============
	// PLACING ARMIES
	// ==============
	
	@Override
	public List<PlaceCommand> placeArmies(long timeout) {
		List<PlaceCommand> result = new ArrayList<PlaceCommand>();
		
		// CLONE REGIONS OWNED BY ME
		List<RegionState> mine = new ArrayList<RegionState>(state.me.regions.values());
		
		// SORT THEM ACCORDING TO THEIR SCORE
		Collections.sort(mine, new Comparator<RegionState>() {

			@Override
			public int compare(RegionState o1, RegionState o2) {
				int regionScore1 = getRegionScore(o1);
				int regionScore2 = getRegionScore(o2);
				return regionScore2 - regionScore1;
			}

		});
		
		// DO NOT ADD SOLDIER TO REGIONS THAT HAS SCORE 0 (not perspective)
		int i = 0;
		while (i < mine.size() && getRegionScore(mine.get(i)) > 0) ++i;
		while (i < mine.size()) mine.remove(i);

		// DISTRIBUTE ARMIES
		int armiesLeft = state.me.placeArmies;
		
		int index = 0;
		
		float totScore=0;
		
		for(RegionState reg: mine){
			totScore += getRegionScore(reg);
		}
		
		while (armiesLeft > 0) {
			
			float score = getRegionScore(mine.get(index))/totScore;
			//courage
			score = score> 0.4 || start==true ? 1:score;
			int putArmies = 2+(int) (score*state.me.placeArmies);
			putArmies = putArmies>armiesLeft?armiesLeft:putArmies;
			
			result.add(new PlaceCommand(mine.get(index).region, putArmies));
			
			armiesLeft -= putArmies;
			++index;
			if (index >= mine.size()) index = 0;
		}
		start=false;
		
		placeCommands = result;
		return result;
	}
	
	private int getRegionScore(RegionState o1) {
		
		int result = 0;
		
		float enemies = 0;
		
		for (RegionState reg : o1.neighbours) {
			enemies += (reg.owned(Player.OPPONENT) ? reg.armies : 0);
			result += (reg.owned(Player.NEUTRAL) ? 1 : 0) * 5000;
			result += (reg.owned(Player.OPPONENT) ? 1 : 0) * (2000 + 4000/ (reg.armies > 0 ? reg.armies : 1));
		}
		
		float intermediate = result;
		
		//SET REGION PRIORITY:

		//BASED ON THE DIFFERENCE IN STRENGTH WITH THE OPPONENT
		float strengthPriority = (enemies>0? (enemies/(float)(0.7*(o1.armies > 0 ? o1.armies : 1))) : 1);
		
		//BASED ON THE CONTINENT EASIER TO FINISH
		Continent c1 = o1.region.continent;
		
		float continentPriority = getContinentPriority(c1);
		continentPriority = state.continent(c1).ownedBy(Player.ME)?1:continentPriority;
		
		intermediate *= continentPriority * strengthPriority;
		
		result = ((int)intermediate !=0 ? (int)intermediate : result);
		
		return result;
	}
	
	
	private float getContinentPriority(Continent c1){
		
		float continentPriority=0;

		for( Region reg: c1.getRegions()){
			continentPriority += (state.region(reg).owned(Player.OPPONENT) ? 1 : 0) * 5;
			continentPriority += (state.region(reg).owned(Player.NEUTRAL) ? 1 : 0) * 10;
			continentPriority += (state.region(reg).owned(Player.ME) ? 1 : 0) * 20;
		}
		float size = c1.getRegions().size();
		continentPriority /= size*size*size;
		
		return continentPriority;
	}
	
	private int getAttackRegionScore(RegionState o1) {
		
		float importance = 1;
		importance = (o1.owned(Player.OPPONENT)? 2 : 1);
		int breakContinent = state.opp.ownsContinent(o1.region.continent)? 5:0;
		return (int)(getContinentPriority(o1.region.continent)*20+(importance*o1.armies)+breakContinent);
		
	}
	

	// =============
	// MOVING ARMIES
	// =============

	@Override
	public List<MoveCommand> moveArmies(long timeout) {		
		List<MoveCommand> result = new ArrayList<MoveCommand>();
		
		// APPLY PLACE COMMANDS
		for (PlaceCommand comm : placeCommands){
			state.region(comm.region).armies += comm.armies;
		}
		
		// CAPTURE REGIONS
		for (RegionState from : state.me.regions.values()) {
			List<RegionState> neighbours = new ArrayList<RegionState>(Arrays.asList(from.neighbours));
			
			
			// SORT THEM ACCORDING TO THEIR SCORE
			Collections.sort(neighbours, new Comparator<RegionState>() {
				@Override
				public int compare(RegionState o1, RegionState o2) {
					int regionScore1 = getAttackRegionScore(o1);
					int regionScore2 = getAttackRegionScore(o2);
					return regionScore2 - regionScore1;
				}
			});
			

			float totalPriority=0;
			
			int i = 0;
			int enemies=0;
			while (i < neighbours.size()){
				if(!neighbours.get(i).owned(Player.ME)){
					if(neighbours.get(i).owned(Player.OPPONENT)) enemies++;
					totalPriority += getAttackRegionScore(neighbours.get(i));
					++i;
				}else{
					neighbours.remove(i);
				}
			}
			if (totalPriority < 0.0001) totalPriority = 0.0001f;
			
			for (RegionState to : neighbours) {
				
				// IF YOU HAVE ENOUGH ARMY TO WIN WITH 0.7
				if (shouldAttack(from, to, 0.7)) {					
					// => ATTACK
					MoveCommand cmd;
					if(enemies==1 || neighbours.size()==1){
						result.add(cmd = attackWithCourage(from, to));
						from.armies -= cmd.armies;
						break;
					}else{
						result.add(cmd = attack(from, to, 0.7));
						from.armies -= cmd.armies;
					}
					enemies--;
				} else 
				if(enemies==1 || getAttackRegionScore(to)/totalPriority>(0.35+(Math.random()*0.05))){
					break;
				}
			}
		}
		
		// MOVE LEFT OVERS CLOSER TO THE FRONT
		for (RegionState from : state.me.regions.values()) {
			if (hasOnlyMyNeighbours(from) && from.armies > 1) {
				MoveCommand cmd;
				result.add(cmd = moveToFront(from));
				from.armies -= cmd.armies;
			}
		}
		
		// REVERT STATE CHANGES
		// => allow opponent modeling
		for (PlaceCommand comm : placeCommands){
			state.region(comm.region).armies -= comm.armies;
		}
		for (MoveCommand comm : result) {
			state.region(comm.from).armies += comm.armies;
		}
		
		// RETURN RESULT
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
		int persp = to.owned(Player.OPPONENT)? 2 : 0;
		int defenders = to.armies+persp;
		
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
		MoveCommand result = new MoveCommand(from.region, to.region, getRequiredSoldiersToConquerRegion(from, to, winProbability));
		return result;
	}
	
	private MoveCommand attackWithCourage(RegionState from, RegionState to) {
		MoveCommand result = new MoveCommand(from.region, to.region, from.armies-1);
		return result;
	}
	
	private MoveCommand transfer(RegionState from, RegionState to) {
		MoveCommand result = new MoveCommand(from.region, to.region, from.armies-1);
		return result;
	}
	
	private Region moveToFrontRegion;
	
	private MoveCommand moveToFront(RegionState from) {
		RegionBFS<BFSNode> bfs = new RegionBFS<BFSNode>();
		moveToFrontRegion = null;
		bfs.run(from.region, new BFSVisitor<BFSNode>() {

			@Override
			public BFSVisitResult<BFSNode> visit(Region region, int level, BFSNode parent, BFSNode thisNode) {
				//System.err.println((parent == null ? "START" : parent.level + ":" + parent.region) + " --> " + level + ":" + region);
				if (!hasOnlyMyNeighbours(state.region(region))) {
					moveToFrontRegion = region;
					return new BFSVisitResult<BFSNode>(BFSVisitResultType.TERMINATE, thisNode == null ? new BFSNode() : thisNode);
				}
				return new BFSVisitResult<BFSNode>(thisNode == null ? new BFSNode() : thisNode);
			}
			
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
	
	
	public static void runInternal() {
		Config config = new Config();
		
		config.bot1Init = "internal:conquest.bot.custom.SmartBot";
		config.bot2Init = "internal:conquest.bot.custom.AggressiveBot";
		
		config.engine.botCommandTimeoutMillis = 24*60*60*1000;
		
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
		BotParser parser = new BotParser(new SmartBot());
		parser.setLogFile(new File("./SmartBot.log"));
		parser.run();
	}
	
	public static void main(String[] args)
	{
		//JavaBot.exec(new String[]{"conquest.bot.custom.SmartBot", "./SmartBot.log"});		
		runInternal();
	}

}
