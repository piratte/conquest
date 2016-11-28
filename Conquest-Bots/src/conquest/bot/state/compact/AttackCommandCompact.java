package conquest.bot.state.compact;

import conquest.game.Player;
import conquest.game.world.Region;

/**
 * Some player has (UNSUCCESSFULLY) attacked region 'to', the attack originated within region 'from'.
 * 
 * @author Jimmy
 */
public class AttackCommandCompact implements ICommandCompact {

	public Region from;	
	public Region to;	
	public int armies;	
	public int defendersCasaulties;
	
	public AttackCommandCompact(Region from, Region to, int armies, int defendersCasaulties) {
		this.from = from;		
		this.to = to;
		this.armies = armies;		
		this.defendersCasaulties = defendersCasaulties;
	}

	@Override
	public void apply(GameStateCompact state) {
		state.setArmiesDelta(from, -armies);
		state.setArmiesDelta(to, defendersCasaulties);
	}

	@Override
	public void revert(GameStateCompact state) {
		state.setArmiesDelta(from, armies);
		state.setArmiesDelta(to, -defendersCasaulties);
	}	
	
}
