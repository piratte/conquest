package conquest.tournament;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * This file takes as input a property file with botId=botInit + {@link ConquestFightConfig} and performs 
 * fights between one bot vs. other bots. 
 * 
 * @author Jimmy
 */
public class ConquestFightBatch {
	
	private Properties bots;
	
	private ConquestFightConfig fightConfig;
	
	public ConquestFightBatch(File botsPropertyFile, ConquestFightConfig fightConfig) {
		this.bots = new Properties();
		
		try {
			InputStream stream = new FileInputStream(botsPropertyFile);
			bots.load(stream);
			stream.close();
		} catch (Exception e) {
			throw new RuntimeException("Failed to read bots property file: " + botsPropertyFile.getAbsolutePath(), e);
		}
		
		this.fightConfig = fightConfig;
	}
	
	public ConquestFightBatch(Properties bots, ConquestFightConfig fightConfig) {
		this.bots = bots;
		this.fightConfig = fightConfig;
	}
	
	public void fight(String botId, boolean reverseGames, File tableFile, File resultDirFile, File replayDirFile) {
		
		if (!bots.containsKey(botId)) throw new RuntimeException("Cannot execute fights for '" + botId + "' as it does not have bot init specified within the property file (key not exists).");
		
		String botInit = bots.getProperty(botId);
		
		for (Entry<Object, Object> entries : bots.entrySet()) {
			String otherBotId = entries.getKey().toString();
			if (otherBotId.startsWith(";")) continue;
			String otherBotInit = entries.getValue().toString();
			
			if (botId.equals(otherBotId)) continue;
			
			System.out.println("------");
			System.out.println("------");
			System.out.println("CONQUEST FIGHT BATCH: " + botId + " vs. " + otherBotId);
			System.out.println("------");
			System.out.println("------");
			
			ConquestFight fight = new ConquestFight(fightConfig, tableFile, resultDirFile, replayDirFile);
			
			fight.fight(botId, botInit, otherBotId, otherBotInit);	
			
			if (reverseGames) {
				fight.fight(otherBotId, otherBotInit, botId, botInit);
			}
		}
		
	}

}
