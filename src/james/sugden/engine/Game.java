package james.sugden.engine;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.io.File;

import org.lwjgl.opengl.Display;

import james.sugden.engine.camera.Camera;
import james.sugden.engine.input.InputHandler;
import james.sugden.engine.networking.Client;
import james.sugden.engine.networking.Server;
import james.sugden.file_handling.ProjectFileHandler;
import james.sugden.file_handling.ResourceManager;

public class Game
{
	/**The project which is being run*/
	private final File fleProject;
	
	/**The current scene being played/edited*/
	private Scene currentScene;
	
	/**The scene to load: If scene is null and switchToNewScene is false, no new scene; If scene is created and switchToNewScene is false, new scene will be switched to at next update*/
	private String newScenePath;
	
	/**Whether the game should continue updating or return*/
	private volatile boolean running;
	
	/**Monitors input*/
	private InputHandler input;
	
	/**True when in single player or when connecting to a multiplayer server*/
	private boolean runningAsClient;
	
	/**True when running in single player or when hosting a multiplayer server*/
	private boolean runningAsServer;
	
	/**Variables used if the game is networked*/
	private Server server;
	private Client client;
	
	public Game(final File fleProject)
	{
		this.fleProject = fleProject;
	}
	
	/**Initialise the game*/
	public void init(Scene scene, InputHandler input)
	{
		this.currentScene = scene;
		this.input = input;
		this.running = false;
		this.runningAsServer = true;
		this.runningAsClient = true;
		this.server = null;
		this.client = null;
		this.newScenePath = null;
		
		if(currentScene != null)
			ResourceManager.compileScripts(currentScene.getGameObjects());	//Compiles the all the scripts used in the current scene
	}
	
	/**Begin execution of the game loop if the game is being played*/
	public final void startAsGame()
	{
		running = true;
		updateAsGame();
	}
	
	/**Updates the game when being run in game mode*/
	private final void updateAsGame()
	{
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		while(!Display.isCloseRequested() && running)
		{
			glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
			
			Time.update();
			
			if(newScenePath != null)
				loadScene(newScenePath);
			
			if(currentScene == null || input == null)
				continue;
			
			input.update();
			
			if(runningAsServer)
				currentScene.updateAsGame();
			if(runningAsClient)
				drawAsGame();
			
			input.setLastPressed();
			
			Display.update();
			Display.sync(60);
		}
	}
	
	/**Draws the game when being run in game mode*/
	private final void drawAsGame()
	{
		currentScene.drawAsGame();
	}
	
	/**Stops the game when being run in game mode*/
	public final void stopGame()
	{
		currentScene.close();
		running = false;
	}
	
	
	
	/**Updates the scripts in a separate thread so that crashes do not crash the game or game engine*/
	
	
	
	
	
	/**Returns the current scene being played/edited*/
	public final Scene getCurrentScene()
	{
		return currentScene;
	}
	
	public final void switchToNewScene(String scenePath)
	{
		newScenePath = scenePath; 
	}
	
	/**Loads the specified scene and sets it as the current scene*/
	private final void loadScene(String name)
	{
		if(fleProject != null)
		{
			File fleScene = new File(fleProject.getAbsolutePath() + "/scenes/" + name + ".jps");
			//System.out.println(fleScene.getAbsolutePath());
			if(fleScene.exists())
			{
				//stopGame();
				
				if(currentScene != null)
					currentScene.close();
				
				input = new InputHandler();
				
				ResourceManager resourceManager = new ResourceManager(fleProject);
				currentScene = new Scene();
				ProjectFileHandler.openProject(currentScene, fleProject, fleScene, resourceManager, input);
				
				Lookup.init(input, currentScene, this);
				ResourceManager.compileScripts(currentScene.getGameObjects());
				currentScene.initAsGame();
				resourceManager.loadAllResources();
				newScenePath = null;
				
				if(!running)				//Start the game if it is not already running
					startAsGame();
			} else
			{
				System.err.println("Scene does not exist");
			}
		}
	}
	
	/**Handles closing of the game*/
	public final void close()
	{
		if(server != null)
			server.close();
		if(client != null)
			client.close();
		if(currentScene != null)
			currentScene.close();
	}
	
	/**Creates an instance of server and allows it to update the game*/
	public final void hostServer()
	{
		runningAsServer = true;
		runningAsClient = false;
		if(server != null)
		{
			server = new Server();
			server.start();
		}
	}
	
	/**Creates an instance of client and allows the game to draw */
	public final void joinServer(String ipAddress)
	{
		runningAsServer = false;
		runningAsClient = true;
		if(client != null)
		{
			client = new Client(ipAddress);
			client.start();
		}
	}
	
	/**Creates an instance of server and of client*/
	public final void hostAndJoinServer()
	{
		System.out.println("Hosting and joining server");
		runningAsServer = true;
		runningAsClient = true;
		if(server != null)
		{
			server = new Server();
			server.start();
		}
	}
	
	
	
	/**World input methods*/
	public final int getMouseWorldX()
	{
		Camera cam = currentScene.getCamera();
		return (int)(input.getMouseX() * cam.getTransform().scaleX + cam.getTransform().x);
	}
	
	public final int getMouseWorldY()
	{
		Camera cam = currentScene.getCamera();
		return (int)(input.getMouseY() * cam.getTransform().scaleY + cam.getTransform().y);
	}
}
