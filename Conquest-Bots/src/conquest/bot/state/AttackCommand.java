package conquest.bot.state;

import conquest.game.Player;
import conquest.game.world.Region;

public class AttackCommand implements Cloneable {

	public Region from;
	public Player fromOwner;
	
	public Region to;
	public Player toOwner;
	
	public int armies;
	
	public int attackersCasaulties;
	public int defendersCasaulties;
	
	public AttackCommand(Region from, Player fromOwner, Region to, Player toOwner, int armies) {
		this(from, fromOwner, to, toOwner, armies, -1, -1);
	}
	
	public AttackCommand(GameState state, MoveCommand move) {
		this(move.from, state.region(move.from).owner.player, move.to, state.region(move.to).owner.player, move.armies);
	}
	
	public AttackCommand(Region from, Player fromOwner, Region to, Player toOwner, int armies, int attackersCasaulties, int defendersCasaulties) {
		this.from = from;
		this.fromOwner = fromOwner;
		
		this.to = to;
		this.toOwner = toOwner;
		this.armies = armies;
		
		this.attackersCasaulties = attackersCasaulties;
		this.defendersCasaulties = defendersCasaulties;
	}
		
}
