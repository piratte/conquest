package conquest.bot.state;

import java.util.ArrayList;
import java.util.List;

import conquest.bot.BotState;
import conquest.bot.map.RegionOwner;
import conquest.game.RegionData;
import conquest.game.world.Continent;
import conquest.game.world.Region;

public class GameState {

	public static class RegionState implements Cloneable {
		
		public int armies;
		public RegionOwner owner;
		
		public RegionState() {
			armies = 0;
			owner = RegionOwner.NEUTRAL;
		}
		
		public RegionState(BotState botState, RegionData data) {
			this.armies = data.getArmies();
			this.owner  = getRegionOwner(botState, data.getPlayerName());
		}
		
	}
	
	private List<RegionState> regions;
	
	public GameState(BotState state) {
		regions = new ArrayList<RegionState>(Region.values().length);
		for (int i = 0; i < Region.values().length; ++i) {
			regions.add(new RegionState());
		}
		
		for (RegionData data : state.getMap().regions) {
			getRegionState(data.getRegion()).armies = data.getArmies();
			RegionOwner owner = getRegionOwner(state, data.getPlayerName());
			getRegionState(data.getRegion()).owner = owner;	
		}
	}
	
	/**
	 * Region can be found under index 'Region.id-1'.
	 * @return
	 */
	public List<RegionState> getRegionStates() {
		return regions;
	}
	
	public RegionState getRegionState(Region region) {
		return regions.get(region.id-1);
	}
	
	public void apply(PlaceCommand cmd) {
		getRegionState(cmd.region).armies += cmd.armies;
	}
	
	public void revert(PlaceCommand cmd) {
		getRegionState(cmd.region).armies -= cmd.armies;
	}
	
	public void apply(MoveCommand cmd) {
		getRegionState(cmd.from).armies -= cmd.armies;
		getRegionState(cmd.to).armies   += cmd.armies;
	}
	
	public void revert(MoveCommand cmd) {
		getRegionState(cmd.from).armies += cmd.armies;
		getRegionState(cmd.to).armies   -= cmd.armies;
	}
	
	public void apply(AttackCommand cmd) {		
		RegionState from = getRegionState(cmd.from);
		RegionState to   = getRegionState(cmd.to);
		
		from.armies -= cmd.armies;		
		to.armies   += cmd.armies - cmd.attackersCasaulties - cmd.defendersCasaulties;
		
		if (cmd.attackersCasaulties < cmd.armies && to.armies <= cmd.defendersCasaulties) {
			// ATTACKERS WON
			to.owner = cmd.fromOwner;
		}
	}
	
	public void revert(AttackCommand cmd) {		
		RegionState from = getRegionState(cmd.from);
		RegionState to   = getRegionState(cmd.to);
		
		// REVERT ARMIES
		from.armies += cmd.armies;		
		to.armies   -= cmd.armies - cmd.attackersCasaulties - cmd.defendersCasaulties;
		
		// REVERT OWNER
		to.owner = cmd.toOwner;
	}
	
	public int getPlaceArmies(RegionOwner who) {
		int armies = 5;
		for (Continent continent : Continent.values()) {
			boolean all = true;
			for (Region region : continent.getRegions()) {
				if (getRegionState(region).owner != who) {
					all = false;
					break;
				}
			}
			if (all) {
				armies += continent.reward;
			}
		}
		return armies;
	}
	
	/**
	 * How many regions are controlled by respective {@link RegionOwner}s.
	 * @return
	 */
	public HashMapInt<RegionOwner> getRegionOwnerCount() {
		HashMapInt<RegionOwner> result = new HashMapInt<RegionOwner>();
		for (Region region : Region.values()) {
			RegionState state = getRegionState(region);
			result.inc(state.owner);
		}
		return result;
	}
	
	/**
	 * How many armies respective {@link RegionOwner}s control.
	 * @return
	 */
	public HashMapInt<RegionOwner> getArmiesCount() {
		HashMapInt<RegionOwner> result = new HashMapInt<RegionOwner>();
		for (Region region : Region.values()) {
			RegionState state = getRegionState(region);
			result.inc(state.owner, state.armies);
		}
		return result;
	}
	
	// =====
	// UTILS
	// =====
	
	public static RegionOwner getRegionOwner(BotState botState, String playerName) {
		return botState.getMyPlayerName().equals(playerName) ? RegionOwner.ME : (botState.getOpponentPlayerName().equals(playerName) ? RegionOwner.OPPONENT : RegionOwner.NEUTRAL); 
	}
	
}
