package conquest.bot.state;

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
		state.apply(this);
	}
	
	/**
	 * Revert this action in {@link GameState}.
	 * @param state
	 */
	public void revert(GameState state) {
		state.revert(this);;
	}
	
}

