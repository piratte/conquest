package conquest.bot.state.compact;

import conquest.game.Player;
import conquest.game.world.Region;

/**
 * Some player has (SUCCESSFULLY) attacked region 'to', the attack originated within region 'from'.
 * 
 * @author Jimmy
 */
public class ConquerCommandCompact implements ICommandCompact {

	public Region from;	
	public Region to;
	public Player attacker;
	public Player defender;
	public int attackersArmies;
	public int defendersArmies;
	public int attackersCasaulties;
	
	public ConquerCommandCompact(Region from, Region to, Player attacker, Player defender, int attackersArmies, int defendersArmies, int attackersCasaulties) {
		this.from = from;		
		this.to = to;
		this.attacker = attacker;
		this.defender = defender;		 
		this.attackersArmies = attackersArmies;
		this.defendersArmies = defendersArmies;
		this.attackersCasaulties = attackersCasaulties;
	}

	@Override
	public void apply(GameStateCompact state) {
		state.setArmiesDelta(from, -attackersArmies);
		state.setArmies(to, attackersArmies - attackersCasaulties);
		state.setOwner(to, attacker);
	}

	@Override
	public void revert(GameStateCompact state) {
		state.setArmiesDelta(from, attackersArmies);
		state.setArmies(to, defendersArmies);
		state.setOwner(to, defender);
	}	
	
}
