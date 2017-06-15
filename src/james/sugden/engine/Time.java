package james.sugden.engine;

import org.lwjgl.Sys;

public class Time
{
	private static long lastTime, thisTime;
	private static int delta;
	
	/**Sets the first last time*/
	public static void init()
	{
		calculateDelta();
	}
	
	/**Sets the last time and delta time for this update*/
	public static void update()
	{
		lastTime = thisTime;
		delta = calculateDelta();
	}
	
	public static long getThisTime()
	{
		return thisTime;
	}
	
	public static long getLastTime()
	{
		return lastTime;
	}
	
	public static int getDelta()
	{
		return delta;
	}
	
	/**Works out how many milliseconds have passed since the last update*/
	private static int calculateDelta()
	{
		thisTime = calculateTime();
		int delta = (int)(thisTime - lastTime);
		
		return delta;
	}
	
	/**Return the system time in milliseconds*/
	private static long calculateTime()
	{
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
}
