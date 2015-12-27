package conquest.engine.robot;


import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import conquest.bot.BotParser;
import conquest.engine.Robot;
import conquest.engine.io.InputOutputStream;
import conquest.engine.replay.GameLog;
import conquest.game.RegionData;
import conquest.game.move.Move;
import conquest.view.GUI;


public class InternalRobot implements Robot {
	
	private Object mutex = new Object();
	
	private InputOutputStream botInput;
	private InputOutputStream botOutput;
	
	private Thread bot;

	private IORobot robot;
	
	public InternalRobot(String playerName, String botFQCN) throws IOException {
		botInput = new InputOutputStream();
		botOutput = new InputOutputStream();
		
		bot = BotParser.runInternal(playerName, botFQCN, botInput.getInputStream(), new PrintStream(botOutput.getOutputStream()));
		System.out.println(playerName + " -> " + botFQCN);
		
		robot = new IORobot(playerName, botInput.getOutputStream(), true, botOutput.getInputStream(), null);				
	}
	
	@Override
	public void setup(RobotConfig config) {
		robot.setup(config);
	}
		
//	@Override
//	public void writeMove(Move move) {
//		robot.writeMove(move);
//	}
	
	@Override
	public String getPreferredStartingArmies(long timeOut, ArrayList<RegionData> pickableRegions)
	{
		return robot.getPreferredStartingArmies(timeOut, pickableRegions);
	}
	
	@Override
	public String getPlaceArmiesMoves(long timeOut)
	{
		return robot.getPlaceArmiesMoves(timeOut);
	}
	
	@Override
	public String getAttackTransferMoves(long timeOut)
	{
		return robot.getAttackTransferMoves(timeOut);
	}
		
	@Override
	public void writeInfo(String info){
		robot.writeInfo(info);
	}

	public boolean isRunning() {
		if (robot == null || bot == null) return false;
		synchronized(mutex) {
			// BOT & ROBOT RUNNING?
			if (robot != null && robot.isRunning() && bot != null && bot.isAlive()) return true;
			
			// SOMEONE DIED OFF...
			finishInternal();
		}		
		return false;
	}
	
	public void finish() {
		if (!isRunning()) return;
		synchronized(mutex) {			
			finishInternal();
		}
	}
	
	private void finishInternal() {
		if (bot != null) {
			try {
				bot.interrupt();
			} catch (Exception e) {				
			}
			bot = null;			
		}
		if (robot != null) {
			try {
				robot.finish();
			} catch (Exception e) {			
			}
			robot = null;
		}		
	}

}
