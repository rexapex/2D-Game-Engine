package james.sugden.engine.input;

import james.sugden.utils.Utils;

import java.util.ArrayList;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector4f;

public class MouseHandler
{
	private ArrayList<MouseBinding> mouseBindings = new ArrayList<>();
	
	private int mousex, mousey, mousedx, mousedy, dwheel;
	private float worldx, worldy, worlddx, worlddy;
	
	public MouseHandler()
	{
		//mouseBindings.add(new MouseBinding("LEFT", 0));
		//mouseBindings.add(new MouseBinding("RIGHT", 1));
		//mouseBindings.add(new MouseBinding("MIDDLE", 2));
	}
	
	public void pollInput()
	{
		mousex = Mouse.getX();
		mousey = Mouse.getY();
		mousedx = Mouse.getDX();
		mousedy = Mouse.getDY();
		
		Vector4f worldCoords = Utils.calcWorldCoords(mousex, mousey, mousedx, mousedy);
		worldx = worldCoords.x;
		worldy = worldCoords.y;
		worlddx = worldCoords.z;
		worlddy = worldCoords.w;
		
		dwheel = Mouse.getDWheel();
		
		while(Mouse.next())
		{
			if(Mouse.getEventButtonState())
			{
				for(MouseBinding binding : mouseBindings)
				{
					if(binding.id == Mouse.getEventButton())
					{
						binding.pressed = true;
						break;
					}
				}
			} else
			{
				for(MouseBinding binding : mouseBindings)
				{
					if(binding.id == Mouse.getEventButton())
					{
						binding.pressed = false;
						break;
					}
				}
			}
		}
	}
	
	public void setLastPressed()
	{
		for(MouseBinding binding : mouseBindings)
		{
			binding.lastPressed = binding.pressed;
		}
	}
	
	
	
	
	public int getMouseX()
	{
		return mousex;
	}
	
	public int getMouseY()
	{
		return mousey;
	}
	
	public int getMouseDX()
	{
		return mousedx;
	}
	
	public int getMouseDY()
	{
		return mousedy;
	}
	
	
	
	
	public float getMouseWorldX()
	{
		return worldx;
	}
	
	public float getMouseWorldY()
	{
		return worldy;
	}
	
	public float getMouseWorldDX()
	{
		return worlddx;
	}
	
	public float getMouseWorldDY()
	{
		return worlddy;
	}
	
	
	
	
	
	public int getMouseDWheel()
	{
		return dwheel;
	}
	
	public boolean removeMouseBinding(String name)
	{
		for(MouseBinding binding : mouseBindings)
		{
			if(binding.name.equals(name))
			{
				mouseBindings.remove(binding);
				return true;
			}
		}
		return false;
	}
	
	public MouseBinding addMouseBinding(String name, int id)
	{
		MouseBinding binding = new MouseBinding(name, id);
		mouseBindings.add(binding);
		return binding;
	}
	
	public void setMouseBinding(String name, int id)
	{
		for(int i = 0; i < mouseBindings.size(); i++)
		{
			if(mouseBindings.get(i).name.equalsIgnoreCase(name))
			{
				mouseBindings.get(i).id = id;
				break;
			}
		}
	}
	
	public ArrayList<MouseBinding> getMouseBindings()
	{
		return mouseBindings;
	}
	
	public boolean isMouseDown(String name)
	{
		for(int i = 0; i < mouseBindings.size(); i++)
		{
			if(mouseBindings.get(i).name.equalsIgnoreCase(name))
			{
				return mouseBindings.get(i).pressed;
			}
		}
		return false;
	}
	
	public boolean wasMouseDownLastUpdate(String name)
	{
		for(int i = 0; i < mouseBindings.size(); i++)
		{
			if(mouseBindings.get(i).name.equalsIgnoreCase(name))
			{
				return mouseBindings.get(i).lastPressed;
			}
		}
		return false;
	}
	
	public class MouseBinding extends Binding
	{
		public MouseBinding()
		{
			name = "NEW MOUSE BINDING";
			id = 0;
			pressed = lastPressed = false;
		}
		
		private MouseBinding(String name, int id)
		{
			this.name = name;
			this.id = id;
		}
	}
}
