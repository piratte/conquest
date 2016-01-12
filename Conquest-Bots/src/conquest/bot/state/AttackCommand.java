package conquest.bot.state;

import conquest.bot.map.RegionOwner;
import conquest.game.world.Region;

public class AttackCommand implements Cloneable {

	public Region from;
	public RegionOwner fromOwner;
	
	public Region to;
	public RegionOwner toOwner;
	
	public int armies;
	
	public int attackersCasaulties;
	public int defendersCasaulties;
	
	public AttackCommand(Region from, RegionOwner fromOwner, Region to, RegionOwner toOwner, int armies) {
		this(from, fromOwner, to, toOwner, armies, -1, -1);
	}
	
	public AttackCommand(Region from, RegionOwner fromOwner, Region to, RegionOwner toOwner, int armies, int attackersCasaulties, int defendersCasaulties) {
		this.from = from;
		this.fromOwner = fromOwner;
		
		this.to = to;
		this.toOwner = toOwner;
		this.armies = armies;
		
		this.attackersCasaulties = attackersCasaulties;
		this.defendersCasaulties = defendersCasaulties;
	}
		
}
