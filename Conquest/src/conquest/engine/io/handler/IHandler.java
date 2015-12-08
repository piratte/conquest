package conquest.engine.io.handler;

public interface IHandler {

	public boolean isRunning();
	public void stop();
	
	public String readLine(long timeOut);
	public boolean writeLine(String line);
	
}
