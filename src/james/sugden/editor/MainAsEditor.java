package james.sugden.editor;

import java.awt.Graphics2D;
import java.awt.SplashScreen;

public class MainAsEditor
{
	/**Run the game in edit mode. Scripts and input are disabled.
	 * Map editing is enabled.*/
	public final static void main(String[] args)
	{
		final SplashScreen splash = SplashScreen.getSplashScreen();	//Display a splash screen on editor's start up
		Graphics2D g = null;
		if(splash == null)
			System.err.println("Splash is Null");
		else
		{
			g = splash.createGraphics();
			if(g == null)
				System.err.println("Graphics2D is Null");
		}
		
		SceneEditor sceneEditor = new SceneEditor();
		sceneEditor.init();
	}
	
	/**Updates the given splash screen using the given graphics 2d
	 * Progress is out of 100*/
/*	public final static void updateSplash(SplashScreen splash, Graphics2D g, int progress, String message)
	{
		if(splash != null && g != null)
		{
			g.setComposite(AlphaComposite.Clear);
	        g.setColor(Color.GRAY);
	        g.fillRect(120, 140, 200, 40);
	        g.setColor(Color.WHITE);
	        g.fillRect(120, 140, progress*2, 40);
	        g.setPaintMode();
	        g.setColor(Color.GRAY);
	        g.drawString(message + " " + progress + "%", 120, 150);
	        
	        splash.update();
		}
	}*/
}
