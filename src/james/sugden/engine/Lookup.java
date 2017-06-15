package james.sugden.engine;

import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.InputHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**Lookup class
 * Used by scripts and editor*/
public class Lookup
{
	private static InputHandler input;
	private static Scene scene;
	private static Random rand;
	private static Game game;
	
	public final static void init(InputHandler input, Scene scene, Game game)
	{
		Lookup.input = input;
		Lookup.scene = scene;
		Lookup.rand = new Random();
		Lookup.game = game;
	}
	
	public final static InputHandler getInput()
	{
		return input;
	}
	
	public final static Scene getScene()
	{
		return scene;
	}
	
	public final static Random getRandom()
	{
		return rand;
	}
	
	public final static Game getGame()
	{
		return game;
	}
	
	public final static GameObject getGameObjectWithName(String name)
	{
		for(GameObject gameObject : scene.getGameObjects())
		{
			if(gameObject.getName().equals(name))
				return gameObject;
		}
		return null;
	}
	
	public final static GameObject[] getGameObjectsWithTag(String tag)
	{
		List<GameObject> gameObjectsList = new ArrayList<>();
		for(GameObject gameObject : scene.getGameObjects())
		{
			if(gameObject.getTag().equals(tag))
				gameObjectsList.add(gameObject);
		}
		return gameObjectsList.toArray(new GameObject[gameObjectsList.size()]);
	}
}
