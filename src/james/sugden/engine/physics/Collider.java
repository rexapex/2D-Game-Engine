package james.sugden.engine.physics;

import james.sugden.engine.game_object.UpdatableComponent;
import james.sugden.engine.maths.Transform;

public abstract class Collider extends UpdatableComponent
{
	/**The transform of the collider relative to the game object*/
	protected Transform transform;
	
	/**If set to true then can be used in scripts and can be passed through
	 * If set to false then is used by physics engine as a collision box*/
	protected boolean isTrigger;
	
	public Collider()
	{
		super();
	}
	
	public Collider(Collider c)
	{
		super(c);
		
		this.transform = new Transform(c.transform);
		this.isTrigger = c.isTrigger;
	}
	
	/**Initialises the collider*/
	public void init()
	{
		transform = new Transform(0, 0, 0);
		isTrigger = false;
	}
	
	/**Redirects to the correct collision method based on type of collider*/
	public final boolean collidesWith(Collider collider)
	{
		if(collider instanceof RectCollider)
			return collidesWith((RectCollider)collider);
		else if(collider instanceof CircleCollider)
			return collidesWith((CircleCollider)collider);
		else
			return false;
	}
	
	/**Checks for collision between a rectangle collider and another collider*/
	public abstract boolean collidesWith(RectCollider collider);
	
	/**Checks for collision between a circle collider and another collider*/
	public abstract boolean collidesWith(CircleCollider collider);
	
	/**Checks for collision between a line collider and another collider*/
	public abstract boolean collidesWith(LineCollider collider);
	
	/**Checks for collision between a path collider and another collider*/
	public abstract boolean collidesWith(PathCollider collider);
	
	/**Checks for collision between a point and a collider*/
	public abstract boolean collidesWith(float x, float y);
	
	public boolean isTrigger()
	{
		return isTrigger;
	}
	
	public void setIsTrigger(boolean isTrigger)
	{
		this.isTrigger = isTrigger;
	}
}
