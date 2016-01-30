package conquest.tournament;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import conquest.engine.RunGame.GameResult;
import conquest.tournament.run.ConquestFightRound;
import conquest.tournament.run.ConquestFightRoundGenerator;
import conquest.tournament.utils.Sanitize;

public class ConquestFight {
	
	private int seed = 0;

	private ConquestFightConfig fightConfig;
	
	private File tableFile;
	
	private File resultDirFile;
	
	private File replayDirFile;
	
	public ConquestFight(ConquestFightConfig prototypeConfig, File tableFile, File resultDirFile, File replayDirFile) {
		this.fightConfig = prototypeConfig;
		this.tableFile = tableFile;
		this.resultDirFile = resultDirFile;
		this.replayDirFile = replayDirFile;
	}
	
	private void log(String name, String msg) {
		System.out.println("[" + name + "] " + msg);
	}
	
	public void fight(String bot1Id, String bot1Init, String bot2Id, String bot2Init) {		
		bot1Id = Sanitize.idify(bot1Id);
		bot2Id = Sanitize.idify(bot2Id);
		
		String gameId = bot1Id + "-vs-" + bot2Id; 
		
		log(gameId, "FIGHT! GAMES: " + fightConfig.games);
		
		fightConfig.config.bot1Id = bot1Id;
		fightConfig.config.bot1Init = bot1Init;
		fightConfig.config.bot2Id = bot2Id;
		fightConfig.config.bot2Init = bot2Init;
		
		ConquestFightRound[] rounds = ConquestFightRoundGenerator.generateFightRounds(fightConfig.seed, fightConfig.config, fightConfig.games);
		
		GameResult[] results = new GameResult[rounds.length];
		
		replayDirFile.mkdirs();
						
		for (int i = 0; i < rounds.length; ++i) {
			long start = System.currentTimeMillis();
			
			gameId = bot1Id + "-vs-" + bot2Id + "-" + i;
			
			log(gameId, "ROUND " + (i+1) + " / " + rounds.length);
			
			// SET REPLAY FILE
			int roundNumber = 0;
			while (true) {
				rounds[i].getConfig().replayLog = new File(replayDirFile, bot1Id + "-vs-" + bot2Id + "-Round-" + roundNumber + ".replay");
				if (!rounds[i].getConfig().replayLog.exists()) break;
				++roundNumber;
			}
			
			GameResult result = rounds[i].run();
						
			log(gameId, "ROUND " + (i+1) + " / " + rounds.length + " FINISHED: " + result.getHumanString());
			
			results[i] = result;
			
			log(gameId, "TIME: " + (System.currentTimeMillis() - start) + "ms");
		}
		
		gameId = bot1Id + "-vs-" + bot2Id; 
		
		log(gameId, "FIGHT FINISHED!");
		
		outputResults(rounds, results);		
	}

	private void outputResults(ConquestFightRound[] rounds, GameResult[] results) {
		String bot1Id = rounds[0].getConfig().bot1Id;
		String bot2Id = rounds[0].getConfig().bot2Id;
		
		String fileName;
		
		if (bot1Id.compareTo(bot2Id) < 0) {
			fileName = bot1Id + "-vs-" + bot2Id + ".csv";
		} else {
			fileName = bot2Id + "-vs-" + bot1Id + ".csv";
		}
		
		outputResults(new File(resultDirFile, fileName), rounds, results);
		
		outputResults(tableFile, rounds, results);
	}

	
	private void outputResults(File file, ConquestFightRound[] rounds, GameResult[] results) {
		file.getParentFile().mkdirs();
		
		PrintWriter writer = null;
		
		boolean outputHeader = !file.exists();
		
		try {
			writer = new PrintWriter(new FileOutputStream(file, true));
			
			if (outputHeader) writer.println(results[0].getCSVHeader() + ";replay");
			
			int index = 0;
			for (GameResult result : results) {
				writer.println(result.getCSV() + ";" + rounds[index].getConfig().replayLog.getAbsolutePath());
				++index;
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to write results into file: " + file.getAbsolutePath(), e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {					
				}
			}
		}	
	}

}
