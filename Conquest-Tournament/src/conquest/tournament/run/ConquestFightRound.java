package conquest.tournament.run;

import conquest.engine.RunGame;
import conquest.engine.RunGame.Config;
import conquest.engine.RunGame.GameResult;
import conquest.game.Team;


public class ConquestFightRound {
	
	private Config config;
	
	public ConquestFightRound(Config config) {
		this.config = config;
	}
	
	public synchronized GameResult run() {
		
		RunGame game = new RunGame(config);
		
		GameResult result = game.go();
		
		if (result.winner == null) {
			if (result.player1Regions > result.player2Regions) result.winner = Team.PLAYER_1;
			else
			if (result.player1Regions < result.player2Regions) result.winner = Team.PLAYER_2;
			else
			if (result.player1Armies > result.player2Armies) result.winner = Team.PLAYER_1;
			else
			if (result.player1Armies < result.player2Armies) result.winner = Team.PLAYER_2;			
		}
		
			
		System.out.println("GAME FINISHED - Winner: " + result.winner);
		
		return result;		
	}

	public Config getConfig() {
		return config;
	}
		
}
