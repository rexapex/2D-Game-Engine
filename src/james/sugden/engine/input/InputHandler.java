package james.sugden.engine.input;

import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import james.sugden.engine.input.KeyboardHandler.KeyBinding;
import james.sugden.engine.input.MouseHandler.MouseBinding;


public class InputHandler
{
	private KeyboardHandler keys;
	private MouseHandler mouse;
	
	public InputHandler()
	{
		keys = new KeyboardHandler();
		mouse = new MouseHandler();
	}
	
	/**Called at the start of the update method to set this updates key presses
	 * set last pressed must be called at end of the implementing class' update method so last pressed functionality can be used*/
	public void update()
	{
		keys.pollInput();
		mouse.pollInput();
	}
	
	/**Called at the ends of the implementing class' update method*/
	public void setLastPressed()
	{
		keys.setLastPressed();
		mouse.setLastPressed();
	}
	
	
	
	
	
	
	
	
	public int getMouseX()
	{
		return mouse.getMouseX() - Display.getWidth() / 2;
	}
	
	public int getMouseY()
	{
		return mouse.getMouseY() - Display.getHeight() / 2;
	}
	
	public int getMouseDX()
	{
		return mouse.getMouseDX();
	}
	
	public int getMouseDY()
	{
		return mouse.getMouseDY();
	}
	
	/*public int getMouseWorldX()
	{
		return mouse.getMouseX() - Display.getWidth() / 2;
	}
	
	public int getMouseWorldY()
	{
		return mouse.getMouseY() - Display.getHeight() / 2;
	}
	
	public int getMouseWorldDX()
	{
		return mouse.getMouseDX();
	}
	
	public int getMouseWorldDY()
	{
		return mouse.getMouseDY();
	}*/
	
	
	
	
	
	public int getMouseDWheel()
	{
		return mouse.getMouseDWheel();
	}
	
	public MouseBinding addMouseBinding(String name, int id)
	{
		return mouse.addMouseBinding(name, id);
	}
	
	public boolean removeMouseBinding(String name)
	{
		return mouse.removeMouseBinding(name);
	}
	
	public void setMouseBinding(String name, int id)
	{
		mouse.setMouseBinding(name, id);
	}
	
	public ArrayList<MouseBinding> getMouseBindings()
	{
		return mouse.getMouseBindings();
	}
	
	public boolean isMouseDown(String name)
	{
		return mouse.isMouseDown(name);
	}
	
	public boolean wasMouseDownLastUpdate(String name)
	{
		return mouse.wasMouseDownLastUpdate(name);
	}
	
	
	
	
	
	
	
	
	
	public boolean removeKeyBinding(String name)
	{
		return keys.removeKeyBinding(name);
	}
	
	public KeyBinding addKeyBinding(String name, int key)
	{
		return keys.addKeyBinding(name, key);
	}
	
	public void setKeyBinding1(String name, int key)
	{
		keys.setKeyBinding1(name, key);
	}
	
	public void setKeyBinding2(String name, int key)
	{
		keys.setKeyBinding2(name, key);
	}
	
	public ArrayList<KeyBinding> getKeyBindings()
	{
		return keys.getKeyBindings();
	}
	
	public boolean isKeyDown(String name)
	{
		return keys.isKeyDown(name);
	}
	
	public boolean wasKeyDownLastUpdate(String name)
	{
		return keys.wasKeyDownLastUpdate(name);
	}
}
