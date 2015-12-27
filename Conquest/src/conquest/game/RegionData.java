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


public class RegionData {
	
	private Region region;
	private int id;
	private LinkedList<RegionData> neighbors;
	private ContinentData continent;
	private int armies;
	private String playerName;
	
	public RegionData(Region region, int id, ContinentData superRegion)
	{
		this.region = region;
		this.id = id;
		this.continent = superRegion;
		this.neighbors = new LinkedList<RegionData>();
		this.playerName = "unknown";
		this.armies = 0;
		if (superRegion != null) {
			superRegion.addSubRegion(this);
		}
	}
	
	public RegionData(Region region, int id, ContinentData superRegion, String playerName, int armies)
	{
		this.region = region;
		this.id = id;
		this.continent = superRegion;
		this.neighbors = new LinkedList<RegionData>();
		this.playerName = playerName;
		this.armies = armies;
		
		superRegion.addSubRegion(this);
	}
	
	public void addNeighbor(RegionData neighbor)
	{
		if(!neighbors.contains(neighbor))
		{
			neighbors.add(neighbor);
			neighbor.addNeighbor(this);
		}
	}
	
	/**
	 * @param region a Region object
	 * @return True if this Region is a neighbor of given Region, false otherwise
	 */
	public boolean isNeighbor(RegionData region)
	{
		if(neighbors.contains(region))
			return true;
		return false;
	}

	/**
	 * @param playerName A string with a player's name
	 * @return True if this region is owned by given playerName, false otherwise
	 */
	public boolean ownedByPlayer(String playerName)
	{
		if(playerName.equals(this.playerName))
			return true;
		return false;
	}
	
	/**
	 * @param armies Sets the number of armies that are on this Region
	 */
	public void setArmies(int armies) {
		this.armies = armies;
	}
	
	/**
	 * @param playerName Sets the Name of the player that this Region belongs to
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	/**
	 * @return The id of this Region
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return A list of this Region's neighboring Regions
	 */
	public LinkedList<RegionData> getNeighbors() {
		return neighbors;
	}

	/**
	 * @return The SuperRegion this Region is part of
	 */
	public ContinentData getContinentData() {
		return continent;
	}
	
	/**
	 * @return The number of armies on this region
	 */
	public int getArmies() {
		return armies;
	}
	
	/**
	 * @return A string with the name of the player that owns this region
	 */
	public String getPlayerName() {
			return playerName;
	}

	public Region getRegion() {
		return region;
	}

	public Continent getContinent() {
		return region.continent;
	}
	
	@Override
	public String toString() {
		return region.name() + "[" + playerName + "|" + armies + "]";
	}
}
