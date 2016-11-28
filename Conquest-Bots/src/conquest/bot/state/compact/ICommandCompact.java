package conquest.bot.state.compact;

import conquest.game.Player;

public interface ICommandCompact {
	
	/**
	 * Apply given command to 'state' assuming it is performed by 'who'. WARNING: no checks performed whether such a command makes sense!
	 * @param state
	 * @param who
	 */
	public void apply(GameStateCompact state);
	
	/**
	 * Revert given command in 'state' assuming it was performed by 'who'. WARNING: no checks performed whether such a command has taken place!
	 * @param state
	 * @param who
	 */
	public void revert(GameStateCompact state);

}
