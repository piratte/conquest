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
	
}
