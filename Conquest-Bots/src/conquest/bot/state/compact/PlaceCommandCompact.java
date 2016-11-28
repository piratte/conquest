package conquest.bot.state.compact;

import conquest.game.world.Region;

public class PlaceCommandCompact implements ICommandCompact {

	public Region to;
	public int armies;

	public PlaceCommandCompact(Region to, int armies) {
		this.to = to;
		this.armies = armies;
	}

	@Override
	public void apply(GameStateCompact state) {
		state.setArmiesDelta(to, armies);
	}

	@Override
	public void revert(GameStateCompact state) {
		state.setArmiesDelta(to, -armies);
	}
	
}
