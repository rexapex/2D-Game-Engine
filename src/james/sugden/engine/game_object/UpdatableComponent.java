package james.sugden.engine.game_object;

public abstract class UpdatableComponent extends Component
{
	public UpdatableComponent()
	{
		super();
	}
	
	public UpdatableComponent(UpdatableComponent c)
	{
		super(c);
	}
	
	public abstract void updateAsGame(GameObject thisGameObject);
}
