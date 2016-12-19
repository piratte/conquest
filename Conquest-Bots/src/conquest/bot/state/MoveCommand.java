package conquest.bot.state;

import conquest.bot.state.GameState.RegionState;
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
		RegionState regionFrom = state.region(from);
		RegionState regionTo = state.region(to);
		
		if (regionFrom.owner.player != regionTo.owner.player) {
			// SHOULD BE ATTACK INSTEAD!
			throw new RuntimeException("Could not apply MOVE as it has to be ATTACK instead! From[" + from + ",owner:" + regionFrom.owner.player + "] --(" + armies + ")--> To[" + to + ",owner:" + regionTo.owner.player + "]");
		}
		
		if (regionFrom.armies <= armies) armies = regionFrom.armies-1;
		
		regionFrom.armies -= armies;
		regionTo.armies += armies;
	}
	
}
