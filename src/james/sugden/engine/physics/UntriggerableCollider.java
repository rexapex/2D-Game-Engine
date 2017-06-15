package james.sugden.engine.physics;

import james.sugden.engine.game_object.GameObject;

public class UntriggerableCollider	//Used only in game mode to store object and collider
{
	public GameObject gameObject;
	public Collider collider;
	
	public UntriggerableCollider(GameObject o, Collider c)
	{
		gameObject = o;
		collider = c;
	}
}
