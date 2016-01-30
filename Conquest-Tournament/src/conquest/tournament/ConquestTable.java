package conquest.tournament;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import conquest.game.Team;
import conquest.tournament.utils.CSV;
import conquest.tournament.utils.CSV.CSVRow;
import conquest.utils.HashMapMapInt;

/**
 * Summarize table.csv that ConquestFight may incrementally output.
 * 
 * @author Jimmy
 */
public class ConquestTable {
	
	public static class BotSummary {
		
		public String botId;
		
		public int games;
		
		public int rounds;
		public double roundsAvg;
		
		public int wins;
		public double winsAvg;
		
		public int loses;
		public double losesAvg;
		
		public int regions;
		public double regionsAvg;
		
		public int armies;
		public double armiesAvg;
		
		public void addGame(int rounds, boolean victory, int regions, int armies) {
			++games;
			
			this.rounds += rounds;
			if (victory) ++wins;
			else ++loses;
			this.regions += regions;
			this.armies += armies;
			
			roundsAvg  = ((double)this.rounds) / ((double)games);
			winsAvg     = ((double)wins) / ((double)games);
			losesAvg    = ((double)loses) / ((double)games);
			regionsAvg = ((double)this.regions) / ((double)games);
			armiesAvg  = ((double)this.armies) / ((double)games);
		}
		
		public String getCSVHeader() {
			return "botId;games;wins;winsAvg;loses;losesAvg;regions;regionsAvg;armies;armiesAvg";
		}
		
		public String getCSV() {
			return botId + ";" + games + ";" + wins + ";" + winsAvg + ";" + loses + ";" + losesAvg + ";" + regions + ";" + regionsAvg + ";" + armies + ";" + armiesAvg;
		}
		
	}
	
	public static class TableSummary {
		
		public Map<String, BotSummary> bots = new HashMap<String, BotSummary>();
		
		/**
		 * KEY1 won 'value' of games over KEY2
		 */
		public HashMapMapInt<String, String> wins = new HashMapMapInt<String, String>();
		
		public void addGame(String bot1Id, String bot2Id, Team winner, int rounds, int bot1Regions, int bot1Armies, int bot2Regions, int bot2Armies) {
			
			boolean bot1Win = winner == Team.PLAYER_1;
			boolean bot2Win = winner == Team.PLAYER_2;
			
			BotSummary bot1 = getOrCreate(bot1Id);
			BotSummary bot2 = getOrCreate(bot2Id);
			
			bot1.addGame(rounds, bot1Win, bot1Regions, bot1Armies);
			bot2.addGame(rounds, bot2Win, bot2Regions, bot2Armies);
			
			if (bot1Win) wins.inc(bot1Id, bot2Id);
			if (bot2Win) wins.inc(bot2Id, bot1Id);			
		}
		
		private BotSummary getOrCreate(String botId) {
			BotSummary summary = bots.get(botId);
			if (summary != null) return summary;
			
			summary = new BotSummary();
			summary.botId = botId;
			bots.put(botId, summary);
			
			return summary;
		}
		
	}

	private File tableFileIn;
	private File summaryFileOut;
	
	public ConquestTable(File tableFileIn, File summaryFileOut) {
		this.tableFileIn = tableFileIn;
		this.summaryFileOut = summaryFileOut;
	}
	
	public void run() {
		if (tableFileIn.isFile()) {
			// PROCESS SINGLE FILE
			System.out.println("Reading: " + tableFileIn.getAbsolutePath());			
			TableSummary summary = ConquestTable.readTable(tableFileIn);
			System.out.println("Writing: " + summaryFileOut.getAbsolutePath());
			ConquestTable.writeSummary(summary, summaryFileOut);
		} else 
		if (tableFileIn.isDirectory()) {
			// PROCESS ALL .csv WITHIN THE DIR
			TableSummary summary = new TableSummary();
			
			for (File file : tableFileIn.listFiles()) {
				if (file.getName().endsWith(".csv")) {
					System.out.println("Reading: " + file.getAbsolutePath());								
					ConquestTable.readTable(file, summary);
				}
			}
			System.out.println("Writing: " + summaryFileOut.getAbsolutePath());
			ConquestTable.writeSummary(summary, summaryFileOut);
		} else {
			throw new RuntimeException("Table file is invalid, neither FILE nor DIRECTORY: " + tableFileIn.getAbsolutePath());
		}
	}
	
	public static TableSummary readTable(File tableFileIn) {
		return readTable(tableFileIn, null);
	}
	
	/**
	 * Read another file with results appending data into 'result'.
	 * @param tableFileIn
	 * @param result if null will be created
	 * @return
	 */
	public static TableSummary readTable(File tableFileIn, TableSummary result) {
		if (result == null) result = new TableSummary();
		
		try {
			CSV csv = new CSV(tableFileIn, ";", true);
			
			for (CSVRow row : csv.rows) {
				
				Team winner = Team.valueOf(row.getString("winner"));
				
				String bot1Id = row.getString("Bot1");
				String bot2Id = row.getString("Bot2");
								
				int rounds = row.getInt("round");
				
				int bot1Regions = row.getInt("player1Regions");				
				int bot1Armies = row.getInt("player1Armies");
				
				int bot2Regions = row.getInt("player2Regions");				
				int bot2Armies = row.getInt("player2Armies");
				
				result.addGame(bot1Id, bot2Id, winner, rounds, bot1Regions, bot1Armies, bot2Regions, bot2Armies);
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to read table from: " + tableFileIn.getAbsolutePath(), e);
		}
		
		return result;
	}
	
	public static void writeSummary(TableSummary summary, File summaryFileOut) {
		
		if (summaryFileOut.exists()) summaryFileOut.delete();
		
		PrintWriter writer = null;
		
		try {
			writer = new PrintWriter(new FileOutputStream(summaryFileOut));
			
			List<String> botIds = new ArrayList<String>( summary.bots.keySet() );
			Collections.sort(botIds);
			
			// HEADER
			
			writer.print("botId;victories");
			
			for (String botId : botIds) {
				writer.print(";");
				writer.print(botId);
			}
			
			writer.print(";");
			writer.print(summary.bots.values().iterator().next().getCSVHeader());
			
			writer.println();
			
			// VALUES
			
			for (String bot1Id : botIds) {
				
				BotSummary bot = summary.bots.get(bot1Id);
				
				writer.print(bot1Id);
				writer.print(";");
				writer.print(bot.wins);
				
				for (String bot2Id : botIds) {
					writer.print(";");					
					if (bot1Id.equals(bot2Id)) writer.print("x");
					else writer.print("[" + summary.wins.get(bot1Id).get(bot2Id) + " : " + summary.wins.get(bot2Id).get(bot1Id) + "]");
				}
				
				writer.print(";");
				writer.print(bot.getCSV());
				
				writer.println();
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to output table summary into: " + summaryFileOut.getAbsolutePath(), e);
		} finally {
			if (writer != null) writer.close();
		}
		
	}
	
}
