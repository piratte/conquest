package conquest.bot.state.compact;

import conquest.game.world.Region;

public class MoveCommandCompact implements ICommandCompact {

	public Region from;
	public Region to;
	public int armies;

	public MoveCommandCompact(Region from, Region to, int armies) {
		this.from = from;
		this.to = to;
		this.armies = armies;
	}

	@Override
	public void apply(GameStateCompact state) {
		state.setArmiesDelta(from, -armies);
		state.setArmiesDelta(to, armies);
	}

	@Override
	public void revert(GameStateCompact state) {
		state.setArmiesDelta(from, armies);
		state.setArmiesDelta(to, -armies);
	}
	
}
