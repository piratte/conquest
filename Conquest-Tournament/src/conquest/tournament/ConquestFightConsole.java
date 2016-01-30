package conquest.tournament;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Iterator;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;

import conquest.engine.Engine.FightMode;
import conquest.engine.RunGame.Config;

public class ConquestFightConsole {
	
	private static final char ARG_SEED_SHORT = 's';
	
	private static final String ARG_SEED_LONG = "seed";
	
	private static final char ARG_GAME_CONFIG_SHORT = 'o';
	
	private static final String ARG_GAME_CONFIG_LONG = "game-config";
	
	private static final char ARG_GAMES_COUNT_SHORT = 'g';
	
	private static final String ARG_GAMES_COUNT_LONG = "games-count";
	
	private static final char ARG_REVERSE_GAMES_SHORT = 'r';
	
	private static final String ARG_REVERSE_GAMES_LONG = "reverse-games";
	
	private static final char ARG_BOT1_ID_SHORT = 'a';
	
	private static final String ARG_BOT1_ID_LONG = "bot1-id";
	
	private static final char ARG_BOT1_INIT_SHORT = 'b';
	
	private static final String ARG_BOT1_INIT_LONG = "bot1-init";
	
	private static final char ARG_BOT2_ID_SHORT = 'c';
	
	private static final String ARG_BOT2_ID_LONG = "bot2-id";
	
	private static final char ARG_BOT2_INIT_SHORT = 'd';
	
	private static final String ARG_BOT2_INIT_LONG = "bot2-init";
	
	private static final char ARG_RESULT_DIR_SHORT = 'u';
	
	private static final String ARG_RESULT_DIR_LONG = "result-dir";
	
	private static final char ARG_REPLAY_DIR_SHORT = 'y';
	
	private static final String ARG_REPLAY_DIR_LONG = "replay-dir";
	
	private static final char ARG_TABLE_FILE_SHORT = 't';
	
	private static final String ARG_TABLE_FILE_LONG = "table-file";
	
	private static JSAP jsap;

	private static int seed = 0;

	private static String roundConfig;
	
	private static int gamesCount;
	
	private static boolean reverseGames;
	
	private static String bot1Id;
	
	private static String bot1Init;
	
	private static String bot2Id;
	
	private static String bot2Init;
		
	private static String resultDir;
	
	private static File resultDirFile;
	
	private static String replayDir;
	
	private static File replayDirFile;
	
	private static String tableFileName;
	
	private static File tableFile;

	private static boolean headerOutput = false;

	private static JSAPResult config;

	private static void fail(String errorMessage) {
		fail(errorMessage, null);
	}

	private static void fail(String errorMessage, Throwable e) {
		header();
		System.out.println("ERROR: " + errorMessage);
		System.out.println();
		if (e != null) {
			e.printStackTrace();
			System.out.println("");
		}		
        System.out.println("Usage: java -jar conquest-tournament.jar ");
        System.out.println("                " + jsap.getUsage());
        System.out.println();
        System.out.println(jsap.getHelp());
        System.out.println();
        throw new RuntimeException("FAILURE: " + errorMessage);
	}

	private static void header() {
		if (headerOutput) return;
		System.out.println();
		System.out.println("==============");
		System.out.println("Conquest Fight");
		System.out.println("==============");
		System.out.println();
		headerOutput = true;
	}
		
	private static void initJSAP() throws JSAPException {
		jsap = new JSAP();
		    	
    	
        
        FlaggedOption opt1 = new FlaggedOption(ARG_BOT1_ID_LONG)
	    	.setStringParser(JSAP.STRING_PARSER)
	    	.setRequired(true) 
	    	.setShortFlag(ARG_BOT1_ID_SHORT)
	    	.setLongFlag(ARG_BOT1_ID_LONG);    
	    opt1.setHelp("Bot 1 ID.");
	
	    jsap.registerParameter(opt1);
	    
	    FlaggedOption opt11 = new FlaggedOption(ARG_BOT1_INIT_LONG)
	    	.setStringParser(JSAP.STRING_PARSER)
	    	.setRequired(true) 
	    	.setShortFlag(ARG_BOT1_INIT_SHORT)
	    	.setLongFlag(ARG_BOT1_INIT_LONG);    
	    opt11.setHelp("Bot 1 INIT string, e.g.: internal:conquest.bot.BotStarter");
	
	    jsap.registerParameter(opt11);
	    
	    FlaggedOption opt2 = new FlaggedOption(ARG_BOT2_ID_LONG)
	    	.setStringParser(JSAP.STRING_PARSER)
	    	.setRequired(true) 
	    	.setShortFlag(ARG_BOT2_ID_SHORT)
	    	.setLongFlag(ARG_BOT2_ID_LONG);    
	    opt2.setHelp("Bot 2 ID.");
	    
	    jsap.registerParameter(opt2);
	    
	    FlaggedOption opt22 = new FlaggedOption(ARG_BOT2_INIT_LONG)
	    	.setStringParser(JSAP.STRING_PARSER)
	    	.setRequired(true) 
	    	.setShortFlag(ARG_BOT2_INIT_SHORT)
	    	.setLongFlag(ARG_BOT2_INIT_LONG);    
	    opt22.setHelp("Bot 2 INIT string, e.g.: process:java -cp bin conquest.bot.BotStarter");
	    
	    jsap.registerParameter(opt22);
	    
	    
	    FlaggedOption opt3 = new FlaggedOption(ARG_REVERSE_GAMES_LONG)
	    	.setStringParser(JSAP.BOOLEAN_PARSER)
	    	.setRequired(false)
	    	.setDefault("true")
	    	.setShortFlag(ARG_REVERSE_GAMES_SHORT)
	    	.setLongFlag(ARG_REVERSE_GAMES_LONG);    
	    opt3.setHelp("Whether we should also generate rounds for 'reversed games', that is, play another 'games count' rounds where Bot2 plays as Player1 and Bot1 as Player2.");
	    
	    jsap.registerParameter(opt3);
	    
	    FlaggedOption opt31 = new FlaggedOption(ARG_GAME_CONFIG_LONG)
	    	.setStringParser(JSAP.STRING_PARSER)
	    	.setRequired(true) 
	    	.setShortFlag(ARG_GAME_CONFIG_SHORT)
	    	.setLongFlag(ARG_GAME_CONFIG_LONG);    
	    opt31.setHelp("List of simulator options, see Config.fromString()");
	
	    jsap.registerParameter(opt31);
	    
	    FlaggedOption opt32 = new FlaggedOption(ARG_RESULT_DIR_LONG)
	    	.setStringParser(JSAP.STRING_PARSER)
	    	.setRequired(false)
	    	.setDefault("./results")
	    	.setShortFlag(ARG_RESULT_DIR_SHORT)
	    	.setLongFlag(ARG_RESULT_DIR_LONG);    
	    opt32.setHelp("Directory where to output results, will be created if not exist.");
	    
	    jsap.registerParameter(opt32);
	    
	    FlaggedOption opt321 = new FlaggedOption(ARG_REPLAY_DIR_LONG)
	    	.setStringParser(JSAP.STRING_PARSER)
	    	.setRequired(false)
	    	.setDefault("./results/replays")
	    	.setShortFlag(ARG_REPLAY_DIR_SHORT)
	    	.setLongFlag(ARG_REPLAY_DIR_LONG);    
	    opt321.setHelp("Directory where to output replays, will be created if not exist.");
	    
	    jsap.registerParameter(opt321);
	    
	    FlaggedOption opt322 = new FlaggedOption(ARG_TABLE_FILE_LONG)
	    	.setStringParser(JSAP.STRING_PARSER)
	    	.setRequired(false)
	    	.setDefault("./results/results-table.csv")
	    	.setShortFlag(ARG_TABLE_FILE_SHORT)
	    	.setLongFlag(ARG_TABLE_FILE_LONG);    
	    opt322.setHelp("File where to collect results of all fights (we incrementally append results here).");
	    
	    jsap.registerParameter(opt322);
	    
	    FlaggedOption opt33 = new FlaggedOption(ARG_GAMES_COUNT_LONG)
	    	.setStringParser(JSAP.INTEGER_PARSER)
	    	.setRequired(false)
	    	.setDefault("5")
	    	.setShortFlag(ARG_GAMES_COUNT_SHORT)
	    	.setLongFlag(ARG_GAMES_COUNT_LONG);    
	    opt33.setHelp("How many fight rounds (full games) bots should fight each other.");
	
	    jsap.registerParameter(opt33);
    
	    FlaggedOption opt6 = new FlaggedOption(ARG_SEED_LONG)
	    	.setStringParser(JSAP.INTEGER_PARSER)
	    	.setRequired(false)
	    	.setDefault("0")
	    	.setShortFlag(ARG_SEED_SHORT)
	    	.setLongFlag(ARG_SEED_LONG);    
	    opt6.setHelp("Seed to be used when generating seeds for respective levels.");
	
	    jsap.registerParameter(opt6);
   	}

	private static void readConfig(String[] args) {
		System.out.println("Parsing command arguments.");
		
		try {
	    	config = jsap.parse(args);
	    } catch (Exception e) {
	    	fail(e.getMessage());
	    	System.out.println("");
	    	e.printStackTrace();
	    	throw new RuntimeException("FAILURE!");
	    }
		
		if (!config.success()) {
			String error = "Invalid arguments specified.";
			Iterator errorIter = config.getErrorMessageIterator();
			if (!errorIter.hasNext()) {
				error += "\n-- No details given.";
			} else {
				while (errorIter.hasNext()) {
					error += "\n-- " + errorIter.next();
				}
			}
			fail(error);
    	}

		seed = config.getInt(ARG_SEED_LONG);

		roundConfig = config.getString(ARG_GAME_CONFIG_LONG);
		
		gamesCount = config.getInt(ARG_GAMES_COUNT_LONG);
		
		reverseGames = config.getBoolean(ARG_REVERSE_GAMES_LONG);
		
		resultDir = config.getString(ARG_RESULT_DIR_LONG);
		
		replayDir = config.getString(ARG_REPLAY_DIR_LONG);
		
		tableFileName = config.getString(ARG_TABLE_FILE_LONG);
		
		bot1Id = config.getString(ARG_BOT1_ID_LONG);
		
		bot1Init = config.getString(ARG_BOT1_INIT_LONG);
		
		bot2Id = config.getString(ARG_BOT2_ID_LONG);
		
		bot2Init = config.getString(ARG_BOT2_INIT_LONG);
	}
	
	private static void sanityChecks() {
		System.out.println("Sanity checks...");
		
		System.out.println("-- seed: " + seed);
		System.out.println("-- game config: " + roundConfig);
		System.out.println("-- #games: " + gamesCount);
		System.out.println("-- play reversed games: " + reverseGames);
		
		resultDirFile = new File(resultDir);
		System.out.println("-- result dir: " + resultDir + " --> " + resultDirFile.getAbsolutePath());
		
		if (!resultDirFile.exists()) {
			System.out.println("---- result dir does not exist, creating!");
			resultDirFile.mkdirs();
		}
		if (!resultDirFile.exists()) {
			fail("Result dir does not exists. Parsed as: " + resultDir + " --> " + resultDirFile.getAbsolutePath());
		}
		if (!resultDirFile.isDirectory()) {
			fail("Result dir is not a directory. Parsed as: " + resultDir + " --> " + resultDirFile.getAbsolutePath());
		}
		System.out.println("---- result directory exists, ok");
		
		replayDirFile = new File(replayDir);
		System.out.println("-- replay dir: " + replayDir + " --> " + replayDirFile.getAbsolutePath());
		
		if (!replayDirFile.exists()) {
			System.out.println("---- replay dir does not exist, creating!");
			replayDirFile.mkdirs();
		}
		if (!replayDirFile.exists()) {
			fail("Replay dir does not exists. Parsed as: " + replayDir + " --> " + replayDirFile.getAbsolutePath());
		}
		if (!replayDirFile.isDirectory()) {
			fail("Replay dir is not a directory. Parsed as: " + replayDir + " --> " + replayDirFile.getAbsolutePath());
		}
		System.out.println("---- replay directory exists, ok");
		
		tableFile = new File(tableFileName);
		System.out.println("-- table file: " + tableFileName + " --> " + tableFile.getAbsolutePath());
		
		if (tableFile.exists() && !tableFile.isFile()) {
			fail("Table file exists and is not a file. Parsed as: " + tableFileName + " --> " + tableFile.getAbsolutePath());
		}
		
		System.out.println("-- bot1: " + bot1Id + " / " + bot1Init);
		System.out.println("-- bot2: " + bot2Id + " / " + bot2Init);
		
	    System.out.println("Sanity checks OK!");
	}
	
	private static void fight() {
		ConquestFightConfig config = new ConquestFightConfig();
		
		config.config = Config.fromString(roundConfig);
		config.seed = seed;
		config.games = gamesCount;
		
		ConquestFight fight = new ConquestFight(config, tableFile, resultDirFile, replayDirFile);
		fight.fight(bot1Id, bot1Init, bot2Id, bot2Init);
		
		if (reverseGames) {
			fight.fight(bot2Id, bot2Init, bot1Id, bot1Init);
		}
	}
		
	// ==============
	// TEST ARGUMENTS
	// ==============
	public static String[] getTestArgs() {
		return new String[] {
				  "-s", "20"     // seed
				, "-o", "GAME;PLR1;PLR2;x;x;false;false;-1;true;5000;5;100;CONTINUAL_1_1_A60_D70"   // game-config
				, "-g", "3"      // games-count
				, "-r", "true"   // reverse-games
				, "-a", "AggressiveBot"                              // bot1-id
				, "-b", "dir;process:../Conquest-Bots;java -cp ./bin;../Conquest/bin conquest.bot.external.JavaBot conquest.bot.custom.AggressiveBot" // bot1-init
				, "-c", "BotStarter"                                 // bot2-id
				, "-d", "internal:conquest.bot.BotStarter"           // bot2-init
				, "-u", "./results"                   // result-dir
				, "-y", "./results/replays"           // replay-dir
				, "-t", "./results/results-table.csv" // table-file
		};
		
		// game-config:
		//		result.gameId = parts[0];                                        // should be always: GAME
		//		result.playerName1 = parts[1];                                   // should be always: PLR1
		//		result.playerName2 = parts[2];                                   // should be always: PLR2
		//		result.bot1Id = parts[3];                                        // will be auto-changed
		//		result.bot2Id = parts[4];                                        // will be auto-changed
		//		result.visualize = Boolean.parseBoolean(parts[5]);               // should be always FALSE
		//		result.forceHumanVisualization = Boolean.parseBoolean(parts[6]); // should be always FALSE
		// followed by engine config:
		//		result.seed = Integer.parseInt(parts[0]);                        // will be auto-changed according to master seed above
		//		result.fullyObservableGame = Boolean.parseBoolean(parts[1]);
		//		result.botCommandTimeoutMillis = Long.parseLong(parts[2]);
		//		result.startingArmies = Integer.parseInt(parts[3]);
		//		result.maxGameRounds = Integer.parseInt(parts[4]);
		//		result.fight = FightMode.valueOf(parts[5]);                      // see FightMode for strings
	}	
	
	public static void main(String[] args) throws JSAPException {
		// -----------
		// FOR TESTING
		// -----------
		//args = getTestArgs();		
		
		// --------------
		// IMPLEMENTATION
		// --------------
		
		initJSAP();
	    
	    header();
	    
	    readConfig(args);
	    
	    sanityChecks();
	    
	    fight();
	    
	    System.out.println("---// FINISHED //---");
	    
	    System.exit(0);
	}

}
