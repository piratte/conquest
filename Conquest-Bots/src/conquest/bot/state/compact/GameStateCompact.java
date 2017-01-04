package conquest.bot.state.compact;

import conquest.bot.state.GameState;
import conquest.bot.state.GameState.ContinentState;
import conquest.bot.state.GameState.RegionState;
import conquest.game.Player;
import conquest.game.world.Continent;
import conquest.game.world.Region;

/**
 * Compact game state suitable for game-tree search. All data hidden within int flags of {@link #regions} and {@link #continents}.
 * 
 * It could be made more slim by kicking out the {@link Continent} cache (we store who controls what there in order to compute {@link #placeArmies(Player)} faster).  
 * 
 * @author Jimmy
 */
public class GameStateCompact implements Cloneable {
	
	public static final int SOME_PLAYER_FLAG = Player.ME.playerFlag | Player.OPPONENT.playerFlag | Player.NEUTRAL.playerFlag;
	
	/**
	 * First two bits (from the right) is {@link Player#playerFlag}, the rest of the bits is "NUMBER OF ARMIES" there.
	 * Indexed via {@link Region#id}.
	 * Length: max {@link Region#id}.
	 */
	public int[] regions;
	
	/**
	 * Contains {@link Player#playerFlag} of the continent owner; -1 means "uncomputed yet"
	 * Indexed via {@link Continent#id}.
	 * Length: max {@link Continent#id}.
	 */
	public int[] continents;
	
	// =======
	// GETTERS
	// =======
	
	/**
	 * PLAYER FLAG of the 'continent' owner.
	 * @param continent
	 * @return
	 */
	public int ownedByFlag(Continent continent) {
		if (continents[continent.id] < 0) {
			return continents[continent.id] = computeContinentOwner(continent).playerFlag;
		}
		return continents[continent.id]; 
	}
	
	/**
	 * Computes who controls the 'continent' out of {@link #regions}.
	 * @param continent
	 * @return
	 */
	public Player computeContinentOwner(Continent continent) {
		Player owner = Player.ME;
		boolean first = true;
		for (Region region : continent.getRegions()) {
			switch (ownedBy(region)) {
			case ME: 
				if (owner != Player.ME) {
					return Player.NEUTRAL;
				}
				break;
			case OPPONENT: 
				if (first) {
					owner = Player.OPPONENT;
				} else { 
					if (owner != Player.OPPONENT) {
						return Player.NEUTRAL;
					}
				}
				break;
			case NEUTRAL:
				return Player.NEUTRAL;
			default: 
				throw new RuntimeException("Unhandled Player: " + ownedBy(region));
			}
			
			first = false;
		}
		return owner;
	}

	
	/**
	 * Who owns the continent?
	 * @param continent
	 * @return
	 */
	public Player ownedBy(Continent continent) {
		return Player.fromFlag(ownedByFlag(continent)); 
	}
	
	/**
	 * Whether 'region' is owned by 'player'.
	 * @param region
	 * @param player
	 * @return
	 */
	public boolean owned(Region region, Player player) {
		return ownedByFlag(region) == player.playerFlag;
	}
	
	/**
	 * PLAYER FLAG of the 'region' owner.
	 * @param region
	 * @return
	 */
	public int ownedByFlag(Region region) {
		return regions[region.id] & SOME_PLAYER_FLAG;
	}
	
	/**
	 * Who owns the region?
	 * @param region
	 * @return
	 */
	public Player ownedBy(Region region) {
		return Player.fromFlag(ownedByFlag(region));
	}
	
	/**
	 * Whether 'continent' is owned by 'player'.
	 * @param continent
	 * @param player
	 * @return
	 */
	public boolean owned(Continent continent, Player player) {
		return ownedByFlag(continent) == player.playerFlag;
	}
	
	/**
	 * How many armies are at 'region'?
	 * @param region
	 * @return
	 */
	public int armiesAt(Region region) {
		return regions[region.id] >> Player.LAST_ID;
	}
	
	/**
	 * How many total armies does 'player' have? WARNING: time O(n)!
	 * @param player
	 * @return
	 */
	public int totalArmies(Player player) {
		int result = 0;
		for (Region region : Region.values()) {
			if (owned(region, player)) result += armiesAt(region);
		}
		return result;
	}
	
	/**
	 * How many armies does/will 'player' have to place in this/next turn given this state? WARNING: time O(n)!
	 * @param player
	 * @return
	 */
	public int placeArmies(Player player) {
		int result = 0;
		for (Continent continent : Continent.values()) {
			if (owned(continent, player)) result += continent.reward;
		}
		return 5+result;
	}
	
	// =======================
	// COMMAND SUPPORT METHODS
	// =======================
	
	/**
	 * Set number of 'armies' into 'region'.
	 * @param region
	 * @param armies
	 */
	public void setArmies(Region region, int armies) {
		int ownerPlayerFlag = regions[region.id] & SOME_PLAYER_FLAG;
		regions[region.id] = (armies << Player.LAST_ID) | ownerPlayerFlag;
	}
	
	/**
	 * Change the number of armies within 'region' with 'armiesDelta'.
	 * @param region
	 * @param armiesDelta
	 */
	public void setArmiesDelta(Region region, int armiesDelta) {
		int armiesNow = armiesAt(region);
		setArmies(region, armiesNow + armiesDelta);
	}
	
	/**
	 * Changes ownership of the region; nullifies continents[region.continent.id] as the ownership could have changed by this.
	 * @param region
	 * @param player
	 */
	public void setOwner(Region region, Player player) {
		regions[region.id] >>= Player.LAST_ID;
		regions[region.id] <<= Player.LAST_ID;
		regions[region.id] |= player.playerFlag;
		continents[region.continent.id] = -1;
	}
	
	// ===============
	// UTILITY METHODS
	// ===============
	
	public GameStateCompact clone() {
		GameStateCompact result = new GameStateCompact();
		result.regions = new int[Region.LAST_ID+1];
		result.continents = new int[Continent.LAST_ID+1];
		System.arraycopy(regions, 0, result.regions, 0, regions.length);
		System.arraycopy(continents, 0, result.continents, 0, continents.length);		
		return result;
	}
	
	public GameState toGameState() {
		return new GameState(this);
	}

	public static GameStateCompact fromGameState(GameState state) {
		
		GameStateCompact result = new GameStateCompact();
		
		result.regions = new int[Region.LAST_ID+1];
		
		for (RegionState region : state.regions) {
			int regionCompact = region.armies;
			regionCompact <<= Player.LAST_ID;
			regionCompact |= region.owner.player.playerFlag;
			result.regions[region.region.id] = regionCompact;
		}
		
		result.continents = new int[Continent.LAST_ID+1];
		
		for (ContinentState continent : state.continents) {
			int continentCompact = continent.owner.playerFlag;
			result.continents[continent.continent.id] = continentCompact;
		}
		
		// WE'RE DONE!	
		return result;
	}
	
}
