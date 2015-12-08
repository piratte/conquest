package conquest.game.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import conquest.game.GameMap;

public enum Region {
	
	// NORTH AMERICA
	Alaska("Alaska", 1, Continent.North_America, 2, 4, 30), 
	Northwest_Territory("Northwest Terr.", 2, Continent.North_America, 3, 4, 5),
	Greenland("Greenland", 3, Continent.North_America, 5, 6, 14),
	Alberta("Alberta", 4, Continent.North_America, 5, 7),
	Ontario("Ontario", 5, Continent.North_America, 6, 7, 8), 
	Quebec("Quebec", 6, Continent.North_America, 8), 
	Western_United_States("Western US", 7, Continent.North_America, 8, 9), 
	Eastern_United_States("Eastern US", 8, Continent.North_America, 9), 
	Central_America("Central America", 9, Continent.North_America, 10),
	
	// SOUTH AMERICA
	Venezuela("Venezuela", 10, Continent.South_America, 11, 12), 
	Peru("Peru", 11, Continent.South_America, 12, 13), 
	Brazil("Brazil", 12, Continent.South_America, 13, 21), 
	Argentina("Argentina", 13, Continent.South_America),
	
	// EUROPE
	Iceland("Iceland", 14, Continent.Europe, 15, 16), 
	Great_Britain("Great Britain", 15, Continent.Europe, 16, 18 ,19), 
	Scandinavia("Scandinavia", 16, Continent.Europe, 17), 
	Ukraine("Ukraine", 17, Continent.Europe, 19, 20, 27, 32, 36), 
	Western_Europe("West.Eur", 18, Continent.Europe, 19, 20, 21), 
	Northern_Europe("North.Eur", 19, Continent.Europe, 20), 
	Southern_Europe("South.Eur", 20, Continent.Europe, 21, 22, 36),
	
	// AFRIKA
	North_Africa("North Africa", 21, Continent.Africa, 22, 23, 24), 
	Egypt("Egypt", 22, Continent.Africa, 23, 36), 
	East_Africa("East Africa", 23, Continent.Africa, 24, 25, 26, 36), 
	Congo("Congo", 24, Continent.Africa, 25), 
	South_Africa("South Africa", 25, Continent.Africa, 26), 
	Madagascar("Madagascar", 26, Continent.Africa),
	
	// ASIA
	Ural("Ural", 27, Continent.Asia, 28, 32, 33), 
	Siberia("Siberia", 28, Continent.Asia, 29, 31, 33, 34), 
	Yakutsk("Yakutsk", 29, Continent.Asia, 30, 31), 
	Kamchatka("Kamchatka", 30, Continent.Asia, 31, 34, 35), 
	Irkutsk("Irkutsk", 31, Continent.Asia, 34), 
	Kazakhstan("Kazakhstan", 32, Continent.Asia, 33, 36, 37), 
	China("China", 33, Continent.Asia, 34, 37, 38), 
	Mongolia("Mongolia", 34, Continent.Asia, 35), 
	Japan("Japan", 35, Continent.Asia), 
	Middle_East("Middle East", 36, Continent.Asia, 37), 
	India("India", 37, Continent.Asia, 38), 
	Siam("Siam", 38, Continent.Asia, 39), 
	
	// AUSTRALIA
	Indonesia("Indonesia", 39, Continent.Australia, 40, 41), 
	New_Guinea("New Guinea", 40, Continent.Australia, 41, 42), 
	Western_Australia("West. Australia", 41, Continent.Australia, 42), 
	Eastern_Australia("East. Australia", 42, Continent.Australia);
			
	public final int id;
	public final Continent continent;
	public final String mapName;
	
	/**
	 * DO NOT USE, contains only "forward" neighbours. Use {@link #getNeighbours()} to obtain ALL neighbours.
	 * Used for {@link GameMap} initialization only.
	 */
	private final int[] forwardNeighbourIds;
	/**
	 * DO NOT USE, contains only "forward" neighbours. Use {@link #getNeighbours()} to obtain ALL neighbours.
	 * Used for {@link GameMap} initialization only.
	 */
	private List<Region> forwardNeighbours = null;
	
	/**
	 * List of all neighbour regions.
	 */
	private List<Region> allNeighbours = null;
	
	private Region(String mapName, int id, Continent superRegion, int... forwardNeighbourIds) {
		this.mapName = mapName;
		this.id = id;
		this.continent = superRegion;
		this.forwardNeighbourIds = forwardNeighbourIds;
	}
	
	/**
	 * All neighbour {@link Region}s.
	 * @return
	 */
	public List<Region> getNeighbours() {
		if (allNeighbours == null) {
			synchronized(this) {
				if (allNeighbours == null) {
					// FIND MY NEIGHBOUR
					List<Region> neighbours = new ArrayList<Region>();
					for (int i = 0; i < forwardNeighbourIds.length; ++i) {
						for (Region region : Region.values()) {
							if (region.id == forwardNeighbourIds[i]) {
								neighbours.add(region);
								break;
							}
						}
					}
					
					// FIND ME IN NEIGHBOURS OF OTHERS
					for (Region region : Region.values()) {
						for (int id : region.forwardNeighbourIds) {
							if (id == this.id) {
								neighbours.add(region);
								break;
							}
						}
					}
					
					// SET THE LIST
					this.allNeighbours = neighbours;
				}
			}	
		}
		return allNeighbours;
	}
	
	/**
	 * USED ONLY FOR THE MAP INITIALIZATION ... for the full list of neighbours use {@link #getNeighbours()}.
	 * @return
	 */
	public List<Region> getForwardNeighbours() {
		if (forwardNeighbours == null) {
			synchronized(this) {
				if (forwardNeighbours == null) {
					// FIND MY FORWARD NEIGHBOURS
					List<Region> neighbours = new ArrayList<Region>();
					for (int i = 0; i < forwardNeighbourIds.length; ++i) {
						for (Region region : Region.values()) {
							if (region.id == forwardNeighbourIds[i]) {
								neighbours.add(region);
								break;
							}
						}
					}
					
					this.forwardNeighbours = neighbours;
				}
			}	
		}
		return forwardNeighbours;
	}
	
	private static Map<Integer, Region> id2Region = null;
	
	public static Region forId(int id) {
		if (id2Region == null) {
			id2Region = new HashMap<Integer, Region>();
			for (Region region : Region.values()) {
				id2Region.put(region.id, region);
			}
		}
		return id2Region.get(id);
	}

}
