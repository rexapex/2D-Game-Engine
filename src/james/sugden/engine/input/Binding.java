package james.sugden.engine.input;

public abstract class Binding
{
	protected String name;
	protected int id;
	protected boolean pressed, lastPressed;
	
	public String getName()
	{
		return name;
	}
	
	public int getID()
	{
		return id;
	}
}
