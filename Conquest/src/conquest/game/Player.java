package conquest.game;

public enum Player {

	ME(1),
	OPPONENT(2),
	NEUTRAL(3);
	
	public final int id;

	private Player(int id) {
		this.id = id;
	}
	
	
	
}
