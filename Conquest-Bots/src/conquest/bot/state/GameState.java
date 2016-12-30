package conquest.bot.state;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import conquest.bot.BotState;
import conquest.bot.state.GameState.RegionState;
import conquest.bot.state.compact.GameStateCompact;
import conquest.game.Player;
import conquest.game.RegionData;
import conquest.game.world.Continent;
import conquest.game.world.Region;
import conquest.utils.HashMapInt;

public class GameState {
	
	public static class RegionState {
		
		/**
		 * What {@link Region} state this object describes. 
		 */
		public final Region region;
		
		/**
		 * Who owns this {@link #region}.
		 */
		public PlayerState owner;
		
		/**
		 * How many armies are in this {@link #region}.
		 */
		public int armies;		
		
		/**
		 * What neighbours this {@link #region} has.
		 */
		public RegionState[] neighbours;
		
		public RegionState(Region region) {
			this.region = region;
			armies = 0;
		}
				
		/**
		 * Is {@link #region} owned by 'player'?
		 * @param player
		 * @return
		 */
		public boolean owned(Player player) {
			return owner != null && owner.player == player;
		}
		
		@Override
		public String toString() {
			return (region == null ? "RegionState" : region.name()) + "[" + (owner == null ? "null" : owner.player.name()) + "|" + armies + "]";
		}
		
	}
		
	public static class ContinentState {
		/**
		 * What {@link Continent} state this object describes.
		 */
		public final Continent continent;
		
		/**
		 * Who owns this {@link #continent}.
		 */
		public Player owner;
		
		/**
		 * All {@link Region} states of this {@link #continent}.
		 */
		public Map<Region, RegionState> regions;
		
		/**
		 * How many regions particular {@link Player} controls within this continent.
		 */
		public HashMapInt<Player> owned;
		
		public ContinentState(Continent continent) {
			this.continent = continent;
			owner = Player.NEUTRAL;
			owned = new HashMapInt<Player>();
			regions = new HashMap<Region, RegionState>();
		}
				
		/**
		 * Returns {@link RegionState} of the {@link #continent}'s {@link Region}.
		 * @param region
		 * @return
		 */
		public RegionState region(Region region) {
			return regions.get(region);
		}
		
		/**
		 * Is this {@link #continent} owned by 'player'?
		 * @param player
		 * @return
		 */
		public boolean ownedBy(Player player) {
			return owner == player;
		}
		
		/**
		 * Returns how many {@link Region}s given 'player' controls in this {@link #continent}.
		 * @param player
		 * @return
		 */
		public int regionsOwnedBy(Player player) {
			return owned.get(player);
		}
		
		@Override
		public String toString() {
			return (continent == null ? "ContinentState" : continent.name())
					  + "[" + (owner == null ? "null" : owner.name()) 
					  + "|ME=" + (owned == null ? "N/A" : owned.get(Player.ME))
					  + "|OPP=" + (owned == null ? "N/A" : owned.get(Player.OPPONENT)) 
					  + "|NEU=" + (owned == null ? "N/A" : owned.get(Player.NEUTRAL))
					  + "]";
		}

		/**
		 * ME becomes OPPONENT and vice versa, OPPONENT becomes ME.
		 */
		protected void swapPlayer() {
			owner = Player.swapPlayer(owner);
			
			int newOppOwned = owned.get(Player.ME);
			int newMeOwned = owned.get(Player.OPPONENT);
			
			owned.put(Player.OPPONENT, newOppOwned);
			owned.put(Player.ME, newMeOwned);
		}
		
	}
	
	public static class PlayerState {
		
		/**
		 * What player this object describes.
		 */
		public Player player;
		
		/**
		 * What {@link Region} {@link #player} owns.
		 */
		public Map<Region, RegionState> regions;
		
		/**
		 * What {@link Continent} {@link #player} owns.
		 */
		public Map<Continent, ContinentState> continents;
		
		/**
		 * How many armies this {@link #player} has in total in all controlled regions.
		 */
		public int totalArmies;
		
		/**
		 * How many armies this player will be placing next round.
		 */
		public int placeArmies;
		
		public PlayerState(Player player) {
			this.player = player;
			regions = new HashMap<Region, RegionState>();
			continents = new HashMap<Continent, ContinentState>();
			totalArmies = 0;
			placeArmies = (player == Player.NEUTRAL ? 0 : 5);
		}
		
		/**
		 * Does this {@link #player} own 'region'? 
		 * @param region
		 * @return
		 */
		public boolean ownsRegion(Region region) {
			return regions.containsKey(region);
		}
		
		/**
		 * Returns {@link RegionState} of the 'region' controlled by this {@link #player}.
		 * @param region
		 * @return
		 */
		public RegionState region(Region region) {
			return regions.get(region);
		}
		
		/**
		 * Does this {@link #player} own 'continent'? 
		 * @param region
		 * @return
		 */
		public boolean ownsContinent(Continent continent) {
			return continents.containsKey(continent);
		}
		
		/**
		 * Returns {@link ContinentState} of the 'continent' controlled by this {@link #player}.
		 * @param region
		 * @return
		 */
		public ContinentState continent(Continent continent) {
			return continents.get(continent);
		}
		
		@Override
		public String toString() {
			return (player == null ? "PlayerState" : player.name()) + "[#continents=" + (continents == null ? "null" : continents.size()) + "|#regions=" + (regions == null ? "null" : regions.size()) + "|totalArmies=" + totalArmies + "|placeArmies=" + placeArmies + "]";
		}
	}
	
	/**
	 * Region state can be found under index 'Region.id'.
	 * @return
	 */
	public RegionState[] regions;
	
	/**
	 * Continent state can be found under index 'Continent.id'.
	 * @return
	 */
	public ContinentState[] continents;
	
	/**
	 * Player state can be found under index 'Player.id'.
	 * @return
	 */
	public PlayerState[] players;
	
	/**
	 * Mine state.
	 */
	public PlayerState me;
	
	/**
	 * Opponent state.
	 */
	public PlayerState opp;
	
	public GameState(BotState state) {
		reset(state);		
	}
	
	public GameState(GameStateCompact gameStateCompact) {
		reset(gameStateCompact);
	}

	/**
	 * Recreates all STATE objects.
	 */
	private void reset() {
		// INIT CONTINENTS
		continents = new ContinentState[Continent.values().length+1];
		for (int i = 1; i <= Continent.values().length; ++i) {
			continents[i] = new ContinentState(Continent.values()[i-1]);
		}
		
		// INIT REGIONS
		regions = new RegionState[Region.values().length+1];
		for (int i = 1; i <= Region.values().length; ++i) {
			regions[i] = new RegionState(Region.values()[i-1]);
		}
		
		// INIT REGION NEIGHBOURS
		for (int i = 1; i <= Region.values().length; ++i) {
			regions[i].neighbours = new RegionState[Region.values()[i-1].getNeighbours().size()];
			for (int j = 0; j < Region.values()[i-1].getNeighbours().size(); ++j) {				
				regions[i].neighbours[j] = region(Region.values()[i-1].getNeighbours().get(j));
			}			
		}
		
		// INIT PLAYER STATES
		players = new PlayerState[Player.values().length+1];
		for (int i = 1; i <= Player.values().length; ++i) {
			players[i] = new PlayerState(Player.values()[i-1]);
		}
		
		// INIT PLAYERS
		this.me  = players[Player.ME.id];
		this.opp = players[Player.OPPONENT.id];
	}
	
	/**
	 * Fully (re)creates the state out of the {@link GameStateCompact}, throwing out all existing objects it has.
	 * @param gameStateCompact
	 */
	private void reset(GameStateCompact gameStateCompact) {
		reset();

		// FILL IN STATES
		for (Region region : Region.values()) {
			// REGION
			RegionState regionState = region(region);
			regionState.armies = gameStateCompact.armiesAt(region);
			regionState.owner = player(gameStateCompact.ownedBy(region));
			
			// CONTINENT
			ContinentState continentState = continent(regionState.region);
			continentState.regions.put(regionState.region, regionState);
			continentState.owned.inc(gameStateCompact.ownedBy(region));
			
			// PLAYER STATE
			PlayerState playerState = regionState.owner;
			playerState.regions.put(regionState.region, regionState);
			playerState.totalArmies += regionState.armies;
		}
		
		// UPDATE CONTINENT OWNERSHIPS & PLACE ARMIES
		for (Continent continent : Continent.values()) {
			ContinentState continentState = continent(continent);
			for (Player player : Player.values()) {
				if (continentState.owned.get(player) == continentState.regions.size()) {
					continentState.owner = player;
					PlayerState playerState = player(player);
					playerState.continents.put(continent, continentState);		
					if (player != Player.NEUTRAL) {
						playerState.placeArmies += continent.reward;
					}
				}
			}
		}
	}

	/**
	 * Fully (re)creates the state out of the {@link BotState}, throwing out all existing objects it has.
	 * @param state
	 */
	private void reset(BotState state) {
		reset();

		// FILL IN STATES
		for (RegionData data : state.getMap().regions) {
			// REGION
			RegionState regionState = region(data.getRegion());
			regionState.armies = data.getArmies();
			Player owner = getRegionOwner(state, data.getPlayerName());
			regionState.owner = player(owner);
			
			// CONTINENT
			ContinentState continentState = continent(regionState.region);
			continentState.regions.put(regionState.region, regionState);
			continentState.owned.inc(regionState.owner.player);
			
			// PLAYER STATE
			PlayerState playerState = regionState.owner;
			playerState.regions.put(regionState.region, regionState);
			playerState.totalArmies += regionState.armies;
		}
		
		// UPDATE CONTINENT OWNERSHIPS & PLACE ARMIES
		for (Continent continent : Continent.values()) {
			ContinentState continentState = continent(continent);
			for (Player player : Player.values()) {
				if (continentState.owned.get(player) == continentState.regions.size()) {
					continentState.owner = player;
					PlayerState playerState = player(player);
					playerState.continents.put(continent, continentState);		
					if (player != Player.NEUTRAL) {
						playerState.placeArmies += continent.reward;
					}
				}
			}
		}
	}
	
	/**
	 * Update this {@link GameState} according to the information from given {@link BotState}.
	 * @param state
	 */
	public void update(BotState state) {
		// RESET TOTAL ARMIES & PLACE ARMIES
		for (int i = 1; i < players.length; ++i) {
			PlayerState player = players[i];
			player.totalArmies = 0;
		}
		
		Set<Continent> regionOwnershipChanged = new HashSet<Continent>();
		
		// UPDATE REGION STATES
		for (RegionData data : state.getMap().regions) {
			// REGION
			RegionState regionState = region(data.getRegion());
			// CONTINENT
			ContinentState continentState = continent(regionState.region);
			// PLAYER STATE
			PlayerState oldOwnerState = regionState.owner;
			
			regionState.armies = data.getArmies();
			
			Player newOwner = getRegionOwner(state, data.getPlayerName());
			PlayerState newOwnerState = player(newOwner);
			
			if (newOwner != regionState.owner.player) {
				// OWNER CHANGED
				regionOwnershipChanged.add(regionState.region.continent);
				
				continentState.owned.dec(regionState.owner.player);
				continentState.owned.inc(newOwner);
				
				oldOwnerState.regions.remove(regionState.region);
				newOwnerState.regions.put(regionState.region, regionState);
				
				regionState.owner = newOwnerState;
			}
			
			newOwnerState.totalArmies += regionState.armies;
		}
		
		// UPDATE CONTINENT OWNERSHIPS & PLACE ARMIES
		for (Continent continent : regionOwnershipChanged) {
			ContinentState continentState = continent(continent);
			updateContinentOwnership(continentState);
		}

	}
	
	protected void updateContinentOwnership(Continent continent) {
		updateContinentOwnership(continent(continent));
	}
	
	protected void updateContinentOwnership(ContinentState continentState) {
		Player newOwner = Player.NEUTRAL;
		for (Player player : Player.values()) {
			if (continentState.owned.get(player) == continentState.regions.size()) {
				newOwner = player;
			}
		}
		
		PlayerState newOwnerState = player(newOwner);
		
		if (continentState.owner != newOwner) {
			// OWNER HAS CHANGED!
			PlayerState oldOwnerState = player(continentState.owner);
			
			oldOwnerState.continents.remove(continentState.continent);
			newOwnerState.continents.put(continentState.continent, continentState);
			
			if (oldOwnerState.player != Player.NEUTRAL) oldOwnerState.placeArmies -= continentState.continent.reward;
			if (newOwnerState.player != Player.NEUTRAL) newOwnerState.placeArmies += continentState.continent.reward;					
			
			continentState.owned.dec(oldOwnerState.player);
			continentState.owned.inc(newOwnerState.player);
			
			continentState.owner = newOwnerState.player;
		}
	}
	
	public RegionState region(Region region) {
		return regions[region.id];
	}
	
	public ContinentState continent(Region region) {
		return continents[region.continent.id];
	}
	
	public ContinentState continent(Continent continent) {
		return continents[continent.id];
	}
	
	public PlayerState player(Player player) {
		return players[player.id];
	}
		
	public void apply(PlaceCommand cmd) {
		RegionState region = region(cmd.region); 
		region.armies += cmd.armies;
		region.owner.totalArmies += cmd.armies;
	}
	
	public void revert(PlaceCommand cmd) {
		RegionState region = region(cmd.region);
		region.armies -= cmd.armies;
		region.owner.totalArmies -= cmd.armies;
	}
	
	public void apply(MoveCommand cmd) {
		RegionState from = region(cmd.from);
		RegionState to = region(cmd.to);
		
		from.armies            -= cmd.armies;
		from.owner.totalArmies -= cmd.armies;
		
		to.armies            += cmd.armies;
		to.owner.totalArmies += cmd.armies;
	}
	
	public void revert(MoveCommand cmd) {
		RegionState from = region(cmd.from);
		RegionState to = region(cmd.to);
		
		from.armies            += cmd.armies;
		from.owner.totalArmies += cmd.armies;
		
		to.armies            -= cmd.armies;
		to.owner.totalArmies -= cmd.armies;
	}
	
	public void apply(AttackCommand cmd) {		
		RegionState regionFrom = region(cmd.from);
		RegionState regionTo = region(cmd.to);
		
		if (regionFrom.owner.player == regionTo.owner.player) {
			// MOVE INSTEAD!
			apply(new MoveCommand(cmd.from, cmd.to, cmd.armies));
			return;
		}
		
		if (regionFrom.armies <= cmd.armies) cmd.armies = regionFrom.armies-1;
		
		if (cmd.armies < 1) return;
		
		if (cmd.attackersCasaulties < 0)               cmd.attackersCasaulties = 0;
		if (cmd.defendersCasaulties < 0)               cmd.defendersCasaulties = 0;
		if (cmd.attackersCasaulties > cmd.armies)      cmd.attackersCasaulties = cmd.armies;
		if (cmd.defendersCasaulties > regionTo.armies) cmd.defendersCasaulties = regionTo.armies;
		
		if (cmd.defendersCasaulties < regionTo.armies) {
			// defenders won
			regionFrom.armies -= cmd.attackersCasaulties;
			regionTo.armies -= cmd.defendersCasaulties;
		} else
		if (cmd.defendersCasaulties >= regionTo.armies && cmd.armies == cmd.attackersCasaulties) {
			// defenders are granted 1 army
			regionFrom.armies -= cmd.armies;
			regionTo.armies = 1;
		} else {
			// attackers won
			regionFrom.armies -= cmd.armies;
			regionTo.armies = cmd.armies - cmd.attackersCasaulties;
			
			// change ownership
			regionTo.owner = player(cmd.fromOwner);
			updateContinentOwnership(regionTo.region.continent);			
		}
		
	}
	
	public void revert(AttackCommand cmd) {		
		RegionState from = region(cmd.from);
		RegionState to   = region(cmd.to);

		PlayerState attacker = from.owner;
		PlayerState defender = player(cmd.toOwner);

		// REVERT ARMIES
		from.armies += cmd.armies;		
		to.armies   -= cmd.armies - cmd.attackersCasaulties - cmd.defendersCasaulties;
		
		// REVERT PLAYER TOTAL ARMIES
		attacker.totalArmies += cmd.attackersCasaulties;
		defender.totalArmies += cmd.defendersCasaulties;
		
		// REVERT OWNER
		if (cmd.attackersCasaulties < cmd.armies && to.armies <= cmd.defendersCasaulties) {
			to.owner = defender;
			
			attacker.regions.remove(to.region);
			defender.regions.put(to.region, to);
			
			ContinentState continent = continent(to.region.continent);
			continent.owned.dec(attacker.player);
			continent.owned.inc(defender.player);			
			updateContinentOwnership(continent);
		}		
	}
	
	/**
	 * ME becomes OPPONENT and vice versa, OPPONENT becomes ME.
	 */
	public void swapPlayers() {
		PlayerState newMe = opp;
		newMe.player = Player.swapPlayer(newMe.player);
		
		PlayerState newOpp = me;
		newOpp.player = Player.swapPlayer(newOpp.player);;
		
		this.me = newMe;
		this.opp = newOpp;
		
		players[Player.ME.id] = newMe;
		players[Player.OPPONENT.id] = newOpp;
		
		for (ContinentState continent : continents) {
			continent.swapPlayer();			
		}
	}
	
	@Override
	public String toString() {
		return "GameState[" + me + "|" + opp + "]";
	}
	
	// =====
	// UTILS
	// =====
	
	public static Player getRegionOwner(BotState botState, String playerName) {
		return botState.getMyPlayerName().equals(playerName) ? Player.ME : (botState.getOpponentPlayerName().equals(playerName) ? Player.OPPONENT : Player.NEUTRAL); 
	}
	
}
