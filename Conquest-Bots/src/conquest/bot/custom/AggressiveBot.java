// Copyright 2014 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package conquest.bot.custom;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import conquest.bot.Bot;
import conquest.bot.BotParser;
import conquest.bot.BotStarter;
import conquest.bot.BotState;
import conquest.bot.fight.FightSimulation.FightAttackersResults;
import conquest.bot.fight.FightSimulation.FightDefendersResults;
import conquest.engine.Engine.FightMode;
import conquest.engine.RunGame;
import conquest.engine.RunGame.Config;
import conquest.engine.RunGame.GameResult;
import conquest.game.GameMap;
import conquest.game.RegionData;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.PlaceArmiesMove;
import conquest.game.world.Continent;


public class AggressiveBot implements Bot 
{
	FightAttackersResults aRes;
	FightDefendersResults dRes;
	
	public AggressiveBot() {
		aRes = FightAttackersResults.loadFromFile(new File("FightSimulation-Attackers-A200-D200.obj"));
		dRes = FightDefendersResults.loadFromFile(new File("FightSimulation-Defenders-A200-D200.obj"));
	}
	
	/**
	 * A method used at the start of the game to decide which player start with what Regions. 6 Regions are required to be returned.
	 * This example randomly picks 6 regions from the pickable starting Regions given by the engine.
	 * @return : a list of m (m=6) Regions starting with the most preferred Region and ending with the least preferred Region to start with 
	 */
	@Override
	public ArrayList<RegionData> getPreferredStartingRegions(BotState state, Long timeOut)
	{
		int m = 6;
		
		// GET ALL PICKABLE STARTING REGIONS
		ArrayList<RegionData> preferredStartingRegions = new ArrayList<RegionData>();
		preferredStartingRegions.addAll(state.getPickableStartingRegions());
		
		// SORT THEM ACCORDING TO THE PRIORITY ... see: getPrefferedContinentPriority(Continent)
		Collections.sort(preferredStartingRegions, new Comparator<RegionData>() {
			@Override
			public int compare(RegionData o1, RegionData o2) {
				int priority1 = getPrefferedContinentPriority(o1.getContinent());
				int priority2 = getPrefferedContinentPriority(o2.getContinent());
				
				return priority1 - priority2;
			}
		});
		
		
		// REMOVE CONTINENT WE DO NOT WANT
		while (preferredStartingRegions.size() > m) preferredStartingRegions.remove(preferredStartingRegions.size()-1);
				
		return preferredStartingRegions;
	}
	
	public int getPrefferedContinentPriority(Continent continent) {
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
	
	Set<RegionData> my      = new HashSet<RegionData>();
	Set<RegionData> opp     = new HashSet<RegionData>();
	Set<RegionData> neutral = new HashSet<RegionData>();
	
	String myName;
	String oppName;
	GameMap map;
	
	private void updateInfos(BotState state) {
		this.map = state.getMap();
		myName = state.getMyPlayerName();
		oppName = state.getOpponentPlayerName();
		
		my.clear();
		opp.clear();
		neutral.clear();
		
		for (RegionData region : state.getMap().getRegions()) {
			if (region.ownedByPlayer(myName)) my.add(region);
			else
			if (region.ownedByPlayer(oppName)) opp.add(region);
			else
			  neutral.add(region);
		}
	}
	
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	@Override
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{
		ArrayList<PlaceArmiesMove> result = new ArrayList<PlaceArmiesMove>();
		
		updateInfos(state);
		
		List<RegionData> myReg = new ArrayList<RegionData>(my);
		
		Collections.sort(myReg, new Comparator<RegionData>() {

			@Override
			public int compare(RegionData o1, RegionData o2) {
				int regionScore1 = getRegionScore(o1);
				int regionScore2 = getRegionScore(o2);
				return regionScore2 - regionScore1;
			}

		});
		
		
		// DO NOT ADD SOLDIER TO REGIONS THAT HAS SCORE 0 (not perspective)
		int i = 0;
		while (i < myReg.size() && getRegionScore(myReg.get(i)) > 0) ++i;
		while (i < myReg.size()) myReg.remove(i);
		
		int armiesLeft = state.getStartingArmies();
		
		int index = 0;
		
		while (armiesLeft > 0) {
			result.add(new PlaceArmiesMove(myName, myReg.get(index), 3));
			armiesLeft -= 3;
			++index;
			if (index >= myReg.size()) index = 0;
		}
		
		return result;
	}
	
	private int getRegionScore(RegionData o1) {
		int result = 0;
		
		for (RegionData reg : o1.getNeighbors()) {
			result += (neutral.contains(reg) ? 1 : 0) * 5;
			result += (opp.contains(reg) ? 1 : 0) * 2;
		}
		
		return result;
	}

	

	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	@Override
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) 
	{
		ArrayList<AttackTransferMove> result = new ArrayList<AttackTransferMove>();
		
		for (RegionData from : my) {
			for (RegionData to : from.getNeighbors()) {
				if (!to.ownedByPlayer(myName)) {
					if (shouldAttack(from, to)) {
						result.add(attack(state, from, to));
					}
				}
			}
		}
		
		return result;
	}	
	
	private int getRequiredSoldiersToConquerRegion(RegionData from, RegionData to) {
		int attackers = from.getArmies() - 1;
		int defenders = to.getArmies();
		
		for (int a = 1; a <= attackers; ++a) {
			double chance = aRes.getAttackersWinChance(a, defenders);
			if (chance > 0.7) {
				return a;
			}
		}
		
		return Integer.MAX_VALUE;
	}
		
	private boolean shouldAttack(RegionData from, RegionData to) {	
		return from.getArmies() > getRequiredSoldiersToConquerRegion(from, to);
	}
	
	private AttackTransferMove attack(BotState state, RegionData from, RegionData to) {
		AttackTransferMove result = new AttackTransferMove(state.getMyPlayerName(), from, to, getRequiredSoldiersToConquerRegion(from, to));
		return result;
	}

	public static void runInternal() {
		Config config = new Config();
		
		config.bot1Init = "internal:conquest.bot.custom.AggressiveBot";
		config.bot2Init = "internal:conquest.bot.BotStarter";
		//config.bot2Init = "human";
		
		config.engine.botCommandTimeoutMillis = 24*60*60*1000;
		
		config.engine.maxGameRounds = 100;
		
		config.engine.fight = FightMode.CONTINUAL_1_1_A60_D70;
		
		config.visualize = true;
		config.forceHumanVisualization = true; // prepare for hijacking bot controls
		
		config.replayLog = new File("./replay.log");
		
		RunGame run = new RunGame(config);
		GameResult result = run.go();
		
		System.exit(0);
	}
	
	public static void runExternal() {
		BotParser parser = new BotParser(new BotStarter());
		//parser.setLogFile(new File("./BotStarter.log"));
		parser.run();
	}

	public static void main(String[] args)
	{
		runInternal();
	}

}
