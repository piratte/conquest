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

package conquest.engine.robot;

import java.io.IOException;
import java.util.ArrayList;

import conquest.engine.Robot;
import conquest.engine.replay.GameLog;
import conquest.game.RegionData;
import conquest.game.move.Move;


public class ProcessRobot implements Robot
{
	Object mutex = new Object();
	
	Process child;
	
	IORobot robot;

	private GameLog log;

	private String logPlayerName;
	
	public ProcessRobot(String playerName, String command) throws IOException
	{
		child = Runtime.getRuntime().exec(command);
		System.out.println(playerName + " -> " + command);
		robot = new IORobot(playerName, child.getOutputStream(), false, child.getInputStream(), child.getErrorStream());
	}
	
	@Override
	public void setGameLog(GameLog gameLog, String playerName) {
		this.log = gameLog;
		this.logPlayerName = playerName;
		robot.setGameLog(gameLog, playerName);
	}
	
	@Override
	public void setup(long timeOut)
	{
		robot.setup(timeOut);
	}
	
	@Override
	public void writeMove(Move move) {
		robot.writeMove(move);
	}
	
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
		if (!robot.isRunning()) {
			if (child == null) return false;
			synchronized(mutex) {
				if (child == null) return false;
				child.destroy();
				child = null;
			}
			return false;
		}
		synchronized(mutex) {
			if (child == null) return false;
			try {
				child.exitValue();
			} catch (Exception e) {
				return true;
			}
			try {
				child.destroy();
			} catch (Exception e) {				
			}
			child = null;
			return false;
		}		
	}

	
	public void finish() {
		if (!isRunning()) return;
		try {
			robot.finish();
		} catch (Exception e) {			
		}
		synchronized(mutex) {
			if (child == null) return;
			try {
				child.destroy();
			} catch (Exception e) {				
			}
			child = null;
		}
	}

}
