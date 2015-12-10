package conquest.engine.io.handler;

import conquest.engine.replay.GameLog;

public interface IHandler {

	public void setGameLog(GameLog log, String playerName);
	
	public boolean isRunning();
	public void stop();
	
	public String readLine(long timeOut);
	public boolean writeLine(String line);
	
}
