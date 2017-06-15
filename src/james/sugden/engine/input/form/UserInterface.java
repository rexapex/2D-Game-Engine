package james.sugden.engine.input.form;

import james.sugden.engine.Lookup;

import java.util.ArrayList;
import java.util.List;

public class UserInterface
{
	/**A list of all parent components in the ui*/
	private List<UIComponent> components = new ArrayList<>();
	
	/**Updates the user interface*/
	public final void update()
	{
		float mousex = Lookup.getInput().getMouseX();
		float mousey = Lookup.getInput().getMouseY();
		
		for(UIComponent comp : components)
		{
			comp.mouseOver(mousex, mousey);
		}
	}
	
	/**Renders the vbo for this ui*/
	public final void draw()
	{
		for(UIComponent comp : components)
		{
			comp.draw();
		}
	}
	
	/**Returns the list of components contained in this user interface*/
	public final List<UIComponent> getUIComponents()
	{
		return components;
	}
}
