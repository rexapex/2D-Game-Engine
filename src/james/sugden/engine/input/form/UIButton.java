package james.sugden.engine.input.form;

import james.sugden.utils.Texture;

public class UIButton extends UIComponent
{
	/**Textures rendered for the component*/
	private Texture texNormal, texMouseOver, texMouseClick;
	
	/**Sets up the position and dimensions variables
	 * All values given as percentage of screen
	 * If value is less than 0, value will be 0, if value is greater than 100, value will be 100*/
	public UIButton(float x, float y, float width, float height)
	{
		super(x, y, width, height);
	}
	
	/**Returns the normal texture used when mouse is neither moused over nor clicked*/
	public Texture getNormalTex()
	{
		return texNormal;
	}
	
	/**Returns the texture used when the mouse is over the component but is not clicked*/
	public Texture getMouseOverTex()
	{
		return texMouseOver;
	}
	
	/**Returns the texture used when the mouse is over the component and clicked*/
	public Texture getMouseClickTex()
	{
		return texMouseClick;
	}
	
	protected Texture getTexture()
	{
		return texNormal;
	}
}
