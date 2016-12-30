package conquest.bot.state;

import conquest.game.world.Region;

public class MoveCommand {

	public Region from;
	public Region to;
	public int armies;

	public MoveCommand(Region from, Region to, int armies) {
		this.from = from;
		this.to = to;
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
