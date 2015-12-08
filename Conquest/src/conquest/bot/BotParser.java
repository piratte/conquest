// Copyright 2014 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package conquest.bot;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import conquest.engine.io.BotStreamReader;
import conquest.game.RegionData;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.PlaceArmiesMove;


public class BotParser implements Runnable {
	
	final BotStreamReader input;	

	final PrintStream output;
	
	final Bot bot;	
	
	BotState currentState;
	
	public BotParser(Bot bot) {
		this(bot, System.in, System.out);
	}
	
	public BotParser(Bot bot, InputStream input, PrintStream output)
	{
		this.input = new BotStreamReader(input);
		this.output = output;
		
		this.bot = bot;
		this.currentState = new BotState();
	}
	
	public static Thread runInternal(String playerName, String botFQCN, InputStream input, PrintStream output) {
		Class botClass;
		try {
			botClass = Class.forName(botFQCN);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Failed to locate bot class: " + botFQCN, e);
		}
		return runInternal(playerName, botClass, input, output);
	}
	
	public static Thread runInternal(String playerName, Class botClass, InputStream input, PrintStream output) {
		Object botObj;
		try {
			botObj = botClass.getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to construct Bot instance, tried to invoke parameterless constructor from class: " + botClass.getName()); 
		}
		if (!(Bot.class.isAssignableFrom(botObj.getClass()))) {
			throw new RuntimeException("Constructed bot does not implement " + Bot.class.getName() + " interface, bot class instantiated: " + botClass.getName());
		}
		Bot bot = (Bot) botObj;
		return runInternal(playerName, bot, input, output);
	}
	
	public static Thread runInternal(String playerName, Bot bot, InputStream input, PrintStream output) {
		BotParser parser = new BotParser(bot, input, output);
		Thread botThread = new Thread(parser, playerName + "-Bot");
		botThread.start();
		return botThread;
	}
	
	@Override
	public void run()
	{
		while (true) {
			String line;
			try {
				line = input.readLine();
			} catch (IOException e) {
				throw new RuntimeException("Failed to read next line.", e);
			}
			if (line == null) {
				System.err.println("End of INPUT stream reached...");
				return;
			}
			line = line.trim();
			if(line.length() == 0) { continue; }
			String[] parts = line.split(" ");
			if(parts[0].equals("pick_starting_regions")) {
				//pick which regions you want to start with
				currentState.setPickableStartingRegions(parts);
				ArrayList<RegionData> preferredStartingRegions = bot.getPreferredStartingRegions(currentState, Long.valueOf(parts[1]));
				String output = "";
				for(RegionData region : preferredStartingRegions)
					output = output.concat(region.getId() + " ");
				
				this.output.println(output);
			} else if(parts.length == 3 && parts[0].equals("go")) {
				//we need to do a move
				String output = "";
				if(parts[1].equals("place_armies")) 
				{
					//place armies
					ArrayList<PlaceArmiesMove> placeArmiesMoves = bot.getPlaceArmiesMoves(currentState, Long.valueOf(parts[2]));
					for(PlaceArmiesMove move : placeArmiesMoves)
						output = output.concat(move.getString() + ",");
				} 
				else if(parts[1].equals("attack/transfer")) 
				{
					//attack/transfer
					ArrayList<AttackTransferMove> attackTransferMoves = bot.getAttackTransferMoves(currentState, Long.valueOf(parts[2]));
					for(AttackTransferMove move : attackTransferMoves)
						output = output.concat(move.getString() + ",");
				}
				if(output.length() > 0)
					this.output.println(output);
				else
					this.output.println("No moves");
			} else if(parts.length == 3 && parts[0].equals("settings")) {
				//update settings
				currentState.updateSettings(parts[1], parts[2]);
			} else if(parts[0].equals("setup_map")) {
				//initial full map is given
				currentState.setupMap(parts);
			} else if(parts[0].equals("update_map")) {
				//all visible regions are given
				currentState.updateMap(parts);
			} else {
				System.err.printf("Unable to parse line \"%s\"\n", line);
			}
		}
	}

}
