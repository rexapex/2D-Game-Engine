package james.sugden.engine.scripts;

import james.sugden.engine.game_object.GameObject;

public abstract class JPGEngineScript	//User written scripts sub this class
{
	public abstract void init(final GameObject thisGameObject);
	public abstract void update(final GameObject thisGameObject);
}
