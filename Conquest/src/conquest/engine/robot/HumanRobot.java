package conquest.engine.robot;

import java.util.ArrayList;
import java.util.List;

import conquest.engine.Robot;
import conquest.game.RegionData;
import conquest.game.move.AttackTransferMove;
import conquest.game.move.PlaceArmiesMove;
import conquest.game.world.Region;

public class HumanRobot implements Robot {

	private RobotConfig config;
	private int startingArmies;
	private boolean running = true;;
	
	public HumanRobot(String playerName) {
	}

	@Override
	public void setup(RobotConfig config) {
		this.config = config;
	}

	@Override
	public String getPreferredStartingArmies(long timeOut, ArrayList<RegionData> pickableRegions) {
		if (config.gameLog != null) {
			System.out.println(config.playerName + "-Human --> getPrefferedStartingArmies()");
		}
		
		List<Region> availableRegions = new ArrayList<Region>(pickableRegions.size());
		for (RegionData data : pickableRegions) {
			availableRegions.add(data.getRegion());
		}
		
		List<Region> chosen = config.gui.chooseRegionsHuman(config.playerName, availableRegions);
		
		String result = "";
		
		for (Region region : chosen) {
			if (result.length() > 0) result += " ";
			result += region.id;
		}
		
		if (config.gameLog != null) {
			System.out.println(config.playerName + "-Human <-- " + result);
		}
		
		
		return result;
	}

	@Override
	public String getPlaceArmiesMoves(long timeOut) {
		if (config.gameLog != null) {
			System.out.println(config.playerName + "-Human --> getPlaceArmiesMoves()");
		}
		
		List<PlaceArmiesMove> commands = config.gui.placeArmiesHuman(config.playerName, config.team, startingArmies);
		
		String result = "";
		
		for (PlaceArmiesMove command : commands) {
			result += command.getString() + ",";
		}
		
		if (config.gameLog != null) {
			System.out.println(config.playerName + "-Human <-- " + result);
		}
		
		return result;
	}

	@Override
	public String getAttackTransferMoves(long timeOut) {
		if (config.gameLog != null) {
			System.out.println(config.playerName + "-Human --> getAttackTransferMoves()");
		}
		
		List<AttackTransferMove> commands = config.gui.moveArmiesHuman(config.playerName, config.team);
		
		String result = "";
		
		for (AttackTransferMove command : commands) {
			result += command.getString() + ",";
		}
		
		if (config.gameLog != null) {
			System.out.println(config.playerName + "-Human <-- " + result);
		}
		
		return result;
	}

	@Override
	public void writeInfo(String info) {
		String[] keys = info.split(" ");
		String key = keys[0];
		if (!key.equals("settings")) return;
		key = keys[1];
		if(key.equals("starting_armies")) 
		{
			startingArmies = Integer.parseInt(keys[2]);
		}
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void finish() {
		running = false;
	}

}
