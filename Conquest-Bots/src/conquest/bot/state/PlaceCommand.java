package conquest.bot.state;

import conquest.bot.state.GameState.RegionState;
import conquest.game.world.Region;

public class PlaceCommand {

	public Region region;
	public int armies;

	public PlaceCommand(Region region, int armies) {
		this.region = region;
		this.armies = armies;
	}
	
	/**
	 * Apply the action to a {@link GameState}.
	 * @param state
	 */
	public void apply(GameState state) {
		RegionState regionState = state.region(region);
		regionState.armies += armies;
	}
	
}

