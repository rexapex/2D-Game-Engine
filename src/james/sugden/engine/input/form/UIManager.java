package james.sugden.engine.input.form;


public class UIManager
{
	/**The position in the list of the current ui being rendered and updated*/
	private UserInterface currentUI;
	
	/**Initialises the ui by loading available ui's and generating a vbo for the current ui*/
	public final void init()
	{
		currentUI = null;
	}
	
	/**Monitors input on the ui components*/
	public final void update()
	{
		if(currentUI != null)
			currentUI.update();
	}
	
	/**Draw the vbo's for the current ui*/
	public final void draw()
	{
		if(currentUI != null)
			currentUI.draw();
	}
	
	/**Returns reference to the current user interface*/
	public final UserInterface getCurrentUI()
	{
		return currentUI;
	}
}

/*Panel
Tab Manager
Button
Check Box
Combo Box
Label
List Box
Spinner
Image
Radio Button
Text Box
Percentage Bar
Menu Bar
Menu Item
Dialogue*/