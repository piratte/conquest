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

package conquest.engine;


import java.awt.Point;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import conquest.engine.robot.InternalRobot;
import conquest.engine.robot.ProcessRobot;
import conquest.game.GameMap;
import conquest.game.Player;
import conquest.game.RegionData;
import conquest.game.ContinentData;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.MoveResult;
import conquest.game.move.PlaceArmiesMove;
import conquest.game.world.Region;
import conquest.game.world.Continent;
import conquest.view.GUI;


//import org.bson.types.ObjectId;
//import com.mongodb.MongoClient;
//import com.mongodb.MongoException;
//import com.mongodb.WriteConcern;
//import com.mongodb.DB;
//import com.mongodb.DBObject;
//import com.mongodb.DBCollection;
//import com.mongodb.BasicDBObject;
//import com.mongodb.DBCursor;
//import com.mongodb.ServerAddress;

public class RunGame
{
	
	public static class Config {
		
		/**
		 * Non-negative seed => use concrete seed.
		 * Negative seed => pick random seed.
		 */
		public int seed = -1;
		
		public boolean fullyObservableGame = true;
		
		public String gameId = "GAME";
		
		public String playerName1 = "PLR1";
		public String playerName2 = "PLR2";
		
		public String bot1Id = "Bot1";
		public String bot2Id = "Bot2";
		
		public String bot1Init;
		public String bot2Init;
		
		public long botCommandTimeoutMillis = 2000;
		
		public boolean visualize = true;
		
		public int startingArmies = 5;
		public int maxGameRounds = 500;
		
	}
	
	Config config;
	
	LinkedList<MoveResult> fullPlayedGame;
	LinkedList<MoveResult> player1PlayedGame;
	LinkedList<MoveResult> player2PlayedGame;
	int gameIndex = 1;

	Engine engine;
	
	public RunGame(Config config)
	{
		this.config = config;		
	}

	private void go() throws IOException, InterruptedException
	{
		System.out.println("starting game " + config.gameId);
		
		GameMap initMap, map;
		Player player1, player2;
		Robot robot1, robot2;
		
		//setup the bots: bot1, bot2
		robot1 = setupRobot(config.playerName1, config.bot1Init);
		robot2 = setupRobot(config.playerName2, config.bot2Init);
				
		player1 = new Player(config.playerName1, robot1, config.startingArmies);
		player2 = new Player(config.playerName2, robot2, config.startingArmies);

		//setup the map
		initMap = makeInitMap();
		map = setupMap(initMap);

		// setup GUI
		GUI gui = null;
		if (config.visualize) {
			gui = new GUI(config.playerName1, config.playerName2);
		}
		
		//start the engine
		this.engine = new Engine(map, player1, player2, gui, config.fullyObservableGame, config.botCommandTimeoutMillis, config.seed);
		
		//send the bots the info they need to start
		robot1.writeInfo("settings your_bot " + player1.getName());
		robot1.writeInfo("settings opponent_bot " + player2.getName());
		robot2.writeInfo("settings your_bot " + player2.getName());
		robot2.writeInfo("settings opponent_bot " + player1.getName());
		sendSetupMapInfo(player1.getBot(), initMap);
		sendSetupMapInfo(player2.getBot(), initMap);
		this.engine.distributeStartingRegions(); //decide the player's starting regions
		this.engine.recalculateStartingArmies(); //calculate how much armies the players get at the start of the round (depending on owned SuperRegions)
		this.engine.sendAllInfo();
		
		//play the game
		while(this.engine.winningPlayer() == null && this.engine.getRoundNr() <= config.maxGameRounds)
		{
			robot1.addToDump("Round " + this.engine.getRoundNr() + "\n");
			robot2.addToDump("Round " + this.engine.getRoundNr() + "\n");
			this.engine.playRound();
		}

		fullPlayedGame = this.engine.getFullPlayedGame();
		player1PlayedGame = this.engine.getPlayer1PlayedGame();
		player2PlayedGame = this.engine.getPlayer2PlayedGame();

		finish(robot1, robot2);
	}

	private Robot setupRobot(String playerName, String botInit) throws IOException {
		if (botInit.startsWith("process:")) {
			String cmd = botInit.substring(8);
			return new ProcessRobot(playerName, cmd);
		}
		if (botInit.startsWith("internal:")) {
			String botFQCN = botInit.substring(9);
			return new InternalRobot(playerName, botFQCN);
		}
		throw new RuntimeException("Invalid init string for player '" + playerName + "', must start either with 'process:' or 'internal:' passed value was: " + botInit);
	}

	//aanpassen en een QPlayer class maken? met eigen finish
	private void finish(Robot bot1, Robot bot2) throws InterruptedException
	{
		try {
			bot1.finish();
		} catch (Exception e) {			
		}
		Thread.sleep(200);

		try {
			bot2.finish();
		} catch (Exception e) {			
		}
		Thread.sleep(200);

		Thread.sleep(200);

		// write everything
		// String outputFile = this.writeOutputFile(this.gameId, this.engine.winningPlayer());
		this.saveGame(bot1, bot2);

        System.exit(0);
	}

	//tijdelijk handmatig invoeren
	private GameMap makeInitMap()
	{
		GameMap map = new GameMap();
		
		// INIT SUPER REGIONS

//		ORIGINAL CODE		
//		SuperRegion northAmerica = new SuperRegion(1, 5);
//		SuperRegion southAmerica = new SuperRegion(2, 2);
//		SuperRegion europe       = new SuperRegion(3, 5);
//		SuperRegion afrika       = new SuperRegion(4, 3);
//		SuperRegion azia         = new SuperRegion(5, 7);
//		SuperRegion australia    = new SuperRegion(6, 2);
		
		Map<Continent, ContinentData> continents = new TreeMap<Continent, ContinentData>(new Comparator<Continent>() {
			@Override
			public int compare(Continent o1, Continent o2) {
				return o1.id - o2.id;
			}			
		});
		
		for (Continent continent : Continent.values()) {
			ContinentData continentData = new ContinentData(continent, continent.id, continent.reward);
			continents.put(continent, continentData);
		}
		
		// INIT REGIONS

//		ORIGINAL CODE
//		Region region1 = new Region(1, northAmerica);
//		Region region2 = new Region(2, northAmerica);
//		Region region3 = new Region(3, northAmerica);
//		Region region4 = new Region(4, northAmerica);
//		Region region5 = new Region(5, northAmerica);
//		Region region6 = new Region(6, northAmerica);
//		Region region7 = new Region(7, northAmerica);
//		Region region8 = new Region(8, northAmerica);
//		Region region9 = new Region(9, northAmerica);
//		
//		Region region10 = new Region(10, southAmerica);
//		Region region11 = new Region(11, southAmerica);
//		Region region12 = new Region(12, southAmerica);
//		Region region13 = new Region(13, southAmerica);
//		
//		Region region14 = new Region(14, europe);
//		Region region15 = new Region(15, europe);
//		Region region16 = new Region(16, europe);
//		Region region17 = new Region(17, europe);
//		Region region18 = new Region(18, europe);
//		Region region19 = new Region(19, europe);
//		Region region20 = new Region(20, europe);
//		
//		Region region21 = new Region(21, afrika);
//		Region region22 = new Region(22, afrika);
//		Region region23 = new Region(23, afrika);
//		Region region24 = new Region(24, afrika);
//		Region region25 = new Region(25, afrika);
//		Region region26 = new Region(26, afrika);
//		
//		Region region27 = new Region(27, azia);
//		Region region28 = new Region(28, azia);
//		Region region29 = new Region(29, azia);
//		Region region30 = new Region(30, azia);
//		Region region31 = new Region(31, azia);
//		Region region32 = new Region(32, azia);
//		Region region33 = new Region(33, azia);
//		Region region34 = new Region(34, azia);
//		Region region35 = new Region(35, azia);
//		Region region36 = new Region(36, azia);
//		Region region37 = new Region(37, azia);
//		Region region38 = new Region(38, azia);
//		
//		Region region39 = new Region(39, australia);
//		Region region40 = new Region(40, australia);
//		Region region41 = new Region(41, australia);
//		Region region42 = new Region(42, australia);
		
		Map<Region, RegionData> regions = new TreeMap<Region, RegionData>(new Comparator<Region>() {
			@Override
			public int compare(Region o1, Region o2) {
				return o1.id - o2.id;
			}
		});
		
		for (Region region : Region.values()) {
			RegionData regionData = new RegionData(region, region.id, continents.get(region.continent));
			regions.put(region, regionData);
		}
		
		// INIT NEIGHBOURS
		
//		ORIGINAL CODE
//		region1.addNeighbor(region2);  region1.addNeighbor(region4);  region1.addNeighbor(region30);
//		region2.addNeighbor(region4);  region2.addNeighbor(region3);  region2.addNeighbor(region5);
//		region3.addNeighbor(region5);  region3.addNeighbor(region6);  region3.addNeighbor(region14);
//		region4.addNeighbor(region5);  region4.addNeighbor(region7);
//		region5.addNeighbor(region6);  region5.addNeighbor(region7);  region5.addNeighbor(region8);  
//		region6.addNeighbor(region8);
//		region7.addNeighbor(region8);  region7.addNeighbor(region9);
//		region8.addNeighbor(region9);
//		region9.addNeighbor(region10);
//		region10.addNeighbor(region11);region10.addNeighbor(region12);
//		region11.addNeighbor(region12);region11.addNeighbor(region13);
//		region12.addNeighbor(region13);region12.addNeighbor(region21);
//		region14.addNeighbor(region15);region14.addNeighbor(region16);
//		region15.addNeighbor(region16);region15.addNeighbor(region18);
//		region15.addNeighbor(region19);region16.addNeighbor(region17);
//		region17.addNeighbor(region19);region17.addNeighbor(region20);region17.addNeighbor(region27);region17.addNeighbor(region32);region17.addNeighbor(region36);
//		region18.addNeighbor(region19);region18.addNeighbor(region20);region18.addNeighbor(region21);
//		region19.addNeighbor(region20);
//		region20.addNeighbor(region21);region20.addNeighbor(region22);region20.addNeighbor(region36);
//		region21.addNeighbor(region22);region21.addNeighbor(region23);region21.addNeighbor(region24);
//		region22.addNeighbor(region23);region22.addNeighbor(region36);
//		region23.addNeighbor(region24);region23.addNeighbor(region25);region23.addNeighbor(region26);region23.addNeighbor(region36);
//		region24.addNeighbor(region25);
//		region25.addNeighbor(region26);
//		region27.addNeighbor(region28);region27.addNeighbor(region32);region27.addNeighbor(region33);		
//		region28.addNeighbor(region29);region28.addNeighbor(region31);region28.addNeighbor(region33);region28.addNeighbor(region34);
//		region29.addNeighbor(region30);region29.addNeighbor(region31);
//		region30.addNeighbor(region31);region30.addNeighbor(region34);region30.addNeighbor(region35);
//		region31.addNeighbor(region34);
//		region32.addNeighbor(region33);region32.addNeighbor(region36);region32.addNeighbor(region37);
//		region33.addNeighbor(region34);region33.addNeighbor(region37);region33.addNeighbor(region38);
//		region34.addNeighbor(region35);
//		region36.addNeighbor(region37);
//		region37.addNeighbor(region38);
//		region38.addNeighbor(region39);
//		region39.addNeighbor(region40);region39.addNeighbor(region41);
//		region40.addNeighbor(region41);region40.addNeighbor(region42);
//		region41.addNeighbor(region42);
		
		for (Region regionName : Region.values()) {
			RegionData region = regions.get(regionName);
			for (Region neighbour : regionName.getForwardNeighbours()) {
				region.addNeighbor(regions.get(neighbour));
			}
		}
		
		// ADD REGIONS TO THE MAP
		
//		ORIGINAL CODE		
//		map.add(region1); map.add(region2); map.add(region3);
//		map.add(region4); map.add(region5); map.add(region6);
//		map.add(region7); map.add(region8); map.add(region9);
//		map.add(region10); map.add(region11); map.add(region12);
//		map.add(region13); map.add(region14); map.add(region15);
//		map.add(region16); map.add(region17); map.add(region18);
//		map.add(region19); map.add(region20); map.add(region21);
//		map.add(region22); map.add(region23); map.add(region24);
//		map.add(region25); map.add(region26); map.add(region27);
//		map.add(region28); map.add(region29); map.add(region30);
//		map.add(region31); map.add(region32); map.add(region33);
//		map.add(region34); map.add(region35); map.add(region36);
//		map.add(region37); map.add(region38); map.add(region39);
//		map.add(region40); map.add(region41); map.add(region42);
		
		for (RegionData region : regions.values()) {
			map.add(region);
		}
		
		// ADD SUPER REGIONS TO THE MAP

//		ORIGINAL CODE	
//		map.add(northAmerica);
//		map.add(southAmerica);
//		map.add(europe);
//		map.add(afrika);
//		map.add(azia);
//		map.add(australia);
		
		for (ContinentData superRegion : continents.values()) {
			map.add(superRegion);
		}

		return map;
	}
	
	//Make every region neutral with 2 armies to start with
	private GameMap setupMap(GameMap initMap)
	{
		GameMap map = initMap;
		for(RegionData region : map.regions)
		{
			region.setPlayerName("neutral");
			region.setArmies(2);
		}
		return map;
	}
	
	private void sendSetupMapInfo(Robot bot, GameMap initMap)
	{
		String setupSuperRegionsString, setupRegionsString, setupNeighborsString;
		setupSuperRegionsString = getSuperRegionsString(initMap);
		setupRegionsString = getRegionsString(initMap);
		setupNeighborsString = getNeighborsString(initMap);
		
		bot.writeInfo(setupSuperRegionsString);
		// System.out.println(setupSuperRegionsString);
		bot.writeInfo(setupRegionsString);
		// System.out.println(setupRegionsString);
		bot.writeInfo(setupNeighborsString);
		// System.out.println(setupNeighborsString);
	}
	
	private String getSuperRegionsString(GameMap map)
	{
		String superRegionsString = "setup_map super_regions";
		for(ContinentData superRegion : map.continents)
		{
			int id = superRegion.getId();
			int reward = superRegion.getArmiesReward();
			superRegionsString = superRegionsString.concat(" " + id + " " + reward);
		}
		return superRegionsString;
	}
	
	private String getRegionsString(GameMap map)
	{
		String regionsString = "setup_map regions";
		for(RegionData region : map.regions)
		{
			int id = region.getId();
			int superRegionId = region.getContinentData().getId();
			regionsString = regionsString.concat(" " + id + " " + superRegionId);
		}
		return regionsString;
	}
	
	//beetje inefficiente methode, maar kan niet sneller wss
	private String getNeighborsString(GameMap map)
	{
		String neighborsString = "setup_map neighbors";
		ArrayList<Point> doneList = new ArrayList<Point>();
		for(RegionData region : map.regions)
		{
			int id = region.getId();
			String neighbors = "";
			for(RegionData neighbor : region.getNeighbors())
			{
				if(checkDoneList(doneList, id, neighbor.getId()))
				{
					neighbors = neighbors.concat("," + neighbor.getId());
					doneList.add(new Point(id,neighbor.getId()));
				}
			}
			if(neighbors.length() != 0)
			{
				neighbors = neighbors.replaceFirst(","," ");
				neighborsString = neighborsString.concat(" " + id + neighbors);
			}
		}
		return neighborsString;
	}
	
	private Boolean checkDoneList(ArrayList<Point> doneList, int regionId, int neighborId)
	{
		for(Point p : doneList)
			if((p.x == regionId && p.y == neighborId) || (p.x == neighborId && p.y == regionId))
				return false;
		return true;
	}

	private String getPlayedGame(Player winner, String gameView)
	{
		StringBuffer out = new StringBuffer();

		LinkedList<MoveResult> playedGame;
		if(gameView.equals("player1"))
			playedGame = player1PlayedGame;
		else if(gameView.equals("player2"))
			playedGame = player2PlayedGame;
		else
			playedGame = fullPlayedGame;
			
		playedGame.removeLast();
		int roundNr = 2;
		out.append("map " + playedGame.getFirst().getMap().getMapString() + "\n");
		out.append("round 1" + "\n");
		for(MoveResult moveResult : playedGame)
		{
			if(moveResult != null)
			{
				if(moveResult.getMove() != null)
				{
					try {
						PlaceArmiesMove plm = (PlaceArmiesMove) moveResult.getMove();
						out.append(plm.getString() + "\n");
					}
					catch(Exception e) {
						AttackTransferMove atm = (AttackTransferMove) moveResult.getMove();
						out.append(atm.getString() + "\n");
					}
				out.append("map " + moveResult.getMap().getMapString() + "\n");
				}
			}
			else
			{
				out.append("round " + roundNr + "\n");
				roundNr++;
			}
		}
		
		if(winner != null)
			out.append(winner.getName() + " won\n");
		else
			out.append("Nobody won\n");

		return out.toString();
	}

	private String compressGZip(String data, String outFile)
	{
		try {
			FileOutputStream fos = new FileOutputStream(outFile);
			GZIPOutputStream gzos = new GZIPOutputStream(fos);

			byte[] outBytes = data.getBytes("UTF-8");
			gzos.write(outBytes, 0, outBytes.length);

			gzos.finish();
			gzos.close();

			return outFile;
		}
		catch(IOException e) {
			System.out.println(e);
			return "";
		}
	}

	/*
	 * MongoDB connection functions
	 */

	public void saveGame(Robot bot1, Robot bot2) {
		Player winner = this.engine.winningPlayer();

		//can do stuff here optionally:
		
		if (winner == null) {
			System.out.println("WINNER: NULL");
		} else {
			int score = this.engine.getRoundNr() - 1;
			System.out.println("WINNER: " + winner.getName() + " " + score);
		}
	}
	
	public static void main(String args[]) throws Exception
	{	
		// TEST ARGUMENTS
		//args = new String[] {"0", "0", "0", "process:java bot.BotStarter", "process:java bot.BotStarter" }; // NOT WORKING
		//args = new String[] {"0", "0", "0", "internal:bot.BotStarter", "internal:bot.BotStarter" };
		//args = new String[] {"0", "0", "0", "process:java bot.BotStarter", "internal:bot.BotStarter" };
		
		Config config = new Config();
		
		config.bot1Init = "internal:conquest.bot.BotStarter";
		config.bot2Init = "internal:conquest.bot.BotStarter";
		
		config.botCommandTimeoutMillis = 24*60*60*1000;
		
		config.visualize = true;
		
		RunGame run = new RunGame(config);
		run.go();
	}
	

}
