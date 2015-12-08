package conquest.game.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum Continent {
	
	North_America(1, 5),
	South_America(2, 2),
	Europe(3, 5),
	Africa(4, 3),	
	Asia(5, 7),
	Australia(6, 2);
	
	public final int id;
	public final int reward;
	
	private Set<Region> regions = null;

	private Continent(int id, int reward) {
		this.id = id;	
		this.reward = reward;				
	}
	
	public Set<Region> getRegions() {
		if (regions == null) {
			synchronized(this) {
				if (regions == null) {
					Set<Region> regions = new HashSet<Region>();
					for (Region regionName : Region.values()) {
						if (regionName.continent == this) {
							regions.add(regionName);
						}
					}
					this.regions = regions;
				}
			}
		}
		return regions;
	}
	
	private static Map<Integer, Continent> id2Continent = null;
	
	public static Continent forId(int id) {
		if (id2Continent == null) {
			id2Continent = new HashMap<Integer, Continent>();
			for (Continent continent : Continent.values()) {
				id2Continent.put(continent.id, continent);
			}
		}
		return id2Continent.get(id);
	}

}
