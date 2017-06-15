package james.sugden.engine.game_object;

public abstract class DrawableComponent extends UpdatableComponent
{
	public DrawableComponent()
	{
		super();
	}
	
	public DrawableComponent(DrawableComponent c)
	{
		super(c);
	}
	
	public abstract void drawAsGame(GameObject thisGameObject);
}
