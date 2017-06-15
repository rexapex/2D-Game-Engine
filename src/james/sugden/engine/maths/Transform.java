package james.sugden.engine.maths;

public class Transform
{
	public float x, y;
	public float rotation;
	public float scaleX = 1, scaleY = 1;
	
	public Transform(float x, float y, float rotation)
	{
		this.x = x;
		this.y = y;
		this.rotation = rotation;
		this.scaleX = this.scaleY = 1;
	}
	
	/**Deep copy constructor*/
	public Transform(Transform t)
	{
		this.x = t.x;
		this.y = t.y;
		this.rotation = t.rotation;
		this.scaleX = t.scaleX;
		this.scaleY = t.scaleY;
	}
	
	/**Does not create a new Transform object, only copies variable values from other into this*/
	public void copy(Transform other)
	{
		this.x = other.x;
		this.y = other.y;
		this.rotation = other.rotation;
		this.scaleX = other.scaleX;
		this.scaleY = other.scaleY;
	}
}
