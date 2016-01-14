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

package conquest.game;

import java.util.LinkedList;

import conquest.game.world.Continent;
import conquest.game.world.Region;

public class GameMap {
	
	public LinkedList<RegionData> regions;
	public LinkedList<ContinentData> continents;
	
	public GameMap()
	{
		this.regions = new LinkedList<RegionData>();
		this.continents = new LinkedList<ContinentData>();
	}
	
	public GameMap(LinkedList<RegionData> regions, LinkedList<ContinentData> continents)
	{
		this.regions = regions;
		this.continents = continents;
	}

	/**
	 * add a Region to the map
	 * @param region : Region to be added
	 */
	public void add(RegionData region)
	{
		for(RegionData r : regions)
			if(r.getId() == region.getId())
			{
				System.err.println("Region cannot be added: id already exists.");
				return;
			}
		regions.add(region);
	}
	
	/**
	 * add a Continent to the map
	 * @param continent : Continent to be added
	 */
	public void add(ContinentData continent)
	{
		for(ContinentData s : continents)
			if(s.getId() == continent.getId())
			{
				System.err.println("Continent cannot be added: id already exists.");
				return;
			}
		continents.add(continent);
	}
	
	/**
	 * @return : a new Map object exactly the same as this one
	 */
	public GameMap getMapCopy() {
		GameMap newMap = new GameMap();
		for(ContinentData sr : continents) //copy continents
		{
			ContinentData newContinent = new ContinentData(Continent.forId(sr.getId()), sr.getId(), sr.getArmiesReward());
			newMap.add(newContinent);
		}
		for(RegionData r : regions) //copy regions
		{
			RegionData newRegion = new RegionData(Region.forId(r.getId()), r.getId(), newMap.getContinent(r.getContinentData().getId()), r.getPlayerName(), r.getArmies());
			newMap.add(newRegion);
		}
		for(RegionData r : regions) //add neighbors to copied regions
		{
			RegionData newRegion = newMap.getRegion(r.getId());
			for(RegionData neighbor : r.getNeighbors())
				newRegion.addNeighbor(newMap.getRegion(neighbor.getId()));
		}
		return newMap;
	}
	
	/**
	 * @return : the list of all Regions in this map
	 */
	public LinkedList<RegionData> getRegions() {
		return regions;
	}
	
	/**
	 * @return : the list of all Continents in this map
	 */
	public LinkedList<ContinentData> getContinents() {
		return continents;
	}
	
	/**
	 * @param id : a Region id number
	 * @return : the matching Region object
	 */
	public RegionData getRegion(int id)
	{
		for(RegionData region : regions)
			if(region.getId() == id)
				return region;
		System.err.println("Could not find region with id " + id);
		return null;
	}
	
	/**
	 * @param id : a Continent id number
	 * @return : the matching Continent object
	 */
	public ContinentData getContinent(int id)
	{
		for(ContinentData continent : continents)
			if(continent.getId() == id)
				return continent;
		System.err.println("Could not find continent with id " + id);
		return null;
	}
	
	public String getMapString()
	{
		String mapString = "";
		for(RegionData region : regions)
		{
			mapString = mapString.concat(region.getId() + ";" + region.getPlayerName() + ";" + region.getArmies() + " ");
		}
		return mapString;
	}
	
	//return all regions owned by given player
	public LinkedList<RegionData> ownedRegionsByPlayer(EnginePlayer player)
	{
		LinkedList<RegionData> ownedRegions = new LinkedList<RegionData>();
		
		for(RegionData region : this.getRegions())
			if(region.getPlayerName().equals(player.getName()))
				ownedRegions.add(region);

		return ownedRegions;
	}
	
	//fog of war
	//return all regions visible to given player
	public LinkedList<RegionData> visibleRegionsForPlayer(EnginePlayer player)
	{
		LinkedList<RegionData> visibleRegions = new LinkedList<RegionData>();
		LinkedList<RegionData> ownedRegions = ownedRegionsByPlayer(player);
		
		visibleRegions.addAll(ownedRegions);
		
		for(RegionData region : ownedRegions)	
			for(RegionData neighbor : region.getNeighbors())
				if(!visibleRegions.contains(neighbor))
					visibleRegions.add(neighbor);

		return visibleRegions;
	}
	
	public GameMap getVisibleMapCopyForPlayer(EnginePlayer player) {
		GameMap visibleMap = getMapCopy();
		LinkedList<RegionData> visibleRegions = visibleRegionsForPlayer(player);
		
		for(RegionData region : regions)
		{
			if(!visibleRegions.contains(region)){
				RegionData unknownRegion = visibleMap.getRegion(region.getId());
				unknownRegion.setPlayerName("unknown");
				unknownRegion.setArmies(0);
			}
		}
		
		return visibleMap;		
	}
	
}
