package conquest.bot.state;

import conquest.game.world.Region;

public class PlaceCommand {

	public Region region;
	public int armies;

	public PlaceCommand(Region region, int armies) {
		this.region = region;
		this.armies = armies;
	}
	
}

