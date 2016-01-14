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

package conquest.bot;

import java.util.ArrayList;

import conquest.game.GameMap;
import conquest.game.RegionData;
import conquest.game.ContinentData;
import conquest.game.world.Continent;
import conquest.game.world.Region;


public class BotState {
	
	private String myName = "";
	private String opponentName = "";
	
	private final GameMap fullMap = new GameMap(); //This map is known from the start, contains all the regions and how they are connected, doesn't change after initialization
	private GameMap visibleMap; //This map represents everything the player can see, updated at the end of each round.
	
	private ArrayList<RegionData> pickableStartingRegions; //2 randomly chosen regions from each continent are given, which the bot can chose to start with
	
	private int startingArmies; //number of armies the player can place on map
	
	private int roundNumber;
	
	public BotState()
	{
		pickableStartingRegions = new ArrayList<RegionData>();
		roundNumber = 0;
	}
	
	/**
	 * Useful to pretend "you are an opponent" for the purpose of computing the GameState from the perspective of your opponent.
	 */
	public void swapNames() {
		String name = myName;
		myName = opponentName;
		opponentName = name;
	}
	
	public void updateSettings(String key, String value)
	{
		if(key.equals("your_bot")) //bot's own name
			myName = value;
		else if(key.equals("opponent_bot")) //opponent's name
			opponentName = value;
		else if(key.equals("starting_armies")) 
		{
			startingArmies = Integer.parseInt(value);
			roundNumber++; //next round
		}
	}
	
	//initial map is given to the bot with all the information except for player and armies info
	public void setupMap(String[] mapInput)
	{
		int i, regionId, continentId, reward;
		
		if(mapInput[1].equals("super_regions"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					continentId = Integer.parseInt(mapInput[i]);
					i++;
					reward = Integer.parseInt(mapInput[i]);
					fullMap.add(new ContinentData(Continent.forId(continentId), continentId, reward));
				}
				catch(Exception e) {
					System.err.println("Unable to parse Continents");
				}
			}
		}
		else if(mapInput[1].equals("regions"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					regionId = Integer.parseInt(mapInput[i]);
					i++;
					continentId = Integer.parseInt(mapInput[i]);
					ContinentData continent = fullMap.getContinent(continentId);
					fullMap.add(new RegionData(Region.forId(regionId), regionId, continent));
				}
				catch(Exception e) {
					System.err.println("Unable to parse Regions " + e.getMessage());
				}
			}
		}
		else if(mapInput[1].equals("neighbors"))
		{
			for(i=2; i<mapInput.length; i++)
			{
				try {
					RegionData region = fullMap.getRegion(Integer.parseInt(mapInput[i]));
					i++;
					String[] neighborIds = mapInput[i].split(",");
					for(int j=0; j<neighborIds.length; j++)
					{
						RegionData neighbor = fullMap.getRegion(Integer.parseInt(neighborIds[j]));
						region.addNeighbor(neighbor);
					}
				}
				catch(Exception e) {
					System.err.println("Unable to parse Neighbors " + e.getMessage());
				}
			}
		}
	}
	
	//regions from wich a player is able to pick his preferred starting regions
	public void setPickableStartingRegions(String[] mapInput)
	{
		for(int i=2; i<mapInput.length; i++)
		{
			int regionId;
			try {
				regionId = Integer.parseInt(mapInput[i]);
				RegionData pickableRegion = fullMap.getRegion(regionId);
				pickableStartingRegions.add(pickableRegion);
			}
			catch(Exception e) {
				System.err.println("Unable to parse pickable regions " + e.getMessage());
			}
		}
	}
	
	//visible regions are given to the bot with player and armies info
	public void updateMap(String[] mapInput)
	{
		visibleMap = fullMap.getMapCopy();
		for(int i=1; i<mapInput.length; i++)
		{
			try {
				RegionData region = visibleMap.getRegion(Integer.parseInt(mapInput[i]));
				String playerName = mapInput[i+1];
				int armies = Integer.parseInt(mapInput[i+2]);
				
				region.setPlayerName(playerName);
				region.setArmies(armies);
				i += 2;
			}
			catch(Exception e) {
				System.err.println("Unable to parse Map Update " + e.getMessage());
			}
		}
		ArrayList<RegionData> unknownRegions = new ArrayList<RegionData>();
		
		//remove regions which are unknown.
		for(RegionData region : visibleMap.regions)
			if(region.getPlayerName().equals("unknown"))
				unknownRegions.add(region);
		for(RegionData unknownRegion : unknownRegions)
			visibleMap.getRegions().remove(unknownRegion);				
	}
	
	public String getMyPlayerName(){
		return myName;
	}
	
	public String getOpponentPlayerName(){
		return opponentName;
	}
	
	public int getStartingArmies(){
		return startingArmies;
	}
	
	public int getRoundNumber(){
		return roundNumber;
	}
	
	/**
	 * Map that is updated via observations.
	 * @return
	 */
	public GameMap getMap(){
		return visibleMap;
	}
	
	public GameMap getFullMap(){
		return fullMap;
	}
	
	public ArrayList<RegionData> getPickableStartingRegions(){
		return pickableStartingRegions;
	}

}
