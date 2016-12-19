package conquest.bot.state;

import conquest.bot.state.GameState.ContinentState;
import conquest.bot.state.GameState.RegionState;
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
	
	/**
	 * Be sure to have {@link #attackersCasaulties} and {@link #defendersCasaulties} defined before applying!
	 * @param state
	 */
	public void apply(GameState state) {
		RegionState regionFrom = state.region(from);
		RegionState regionTo = state.region(to);
		
		if (regionFrom.owner.player == regionTo.owner.player) {
			// MOVE INSTEAD!
			new MoveCommand(from, to, armies).apply(state);
			return;
		}
		
		if (regionFrom.armies <= armies) armies = regionFrom.armies-1;
		
		if (armies < 1) return;
		
		if (attackersCasaulties < 0)               attackersCasaulties = 0;
		if (defendersCasaulties < 0)               defendersCasaulties = 0;
		if (attackersCasaulties > armies)          attackersCasaulties = armies;
		if (defendersCasaulties > regionTo.armies) defendersCasaulties = regionTo.armies;
		
		if (defendersCasaulties < regionTo.armies) {
			// defenders won
			regionFrom.armies -= attackersCasaulties;
			regionTo.armies -= defendersCasaulties;
		} else
		if (defendersCasaulties >= regionTo.armies && armies == attackersCasaulties) {
			// defenders are granted 1 army
			regionFrom.armies -= armies;
			regionTo.armies = 1;
		} else {
			// attackers won
			regionFrom.armies -= armies;
			regionTo.armies = armies - attackersCasaulties;
			
			// change ownership
			regionTo.owner = state.player(fromOwner);
			
			
			ContinentState continent = state.continent(to.continent); 
			continent.owned.inc(fromOwner);
			continent.owned.dec(toOwner);
			
			if (continent.owned.get(toOwner) == to.continent.getRegions().size()) {
				continent.owner = fromOwner;
			} else {
				continent.owner = Player.NEUTRAL;
			}
		}
	}
		
}
