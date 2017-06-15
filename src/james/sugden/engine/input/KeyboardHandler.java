package james.sugden.engine.input;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

public class KeyboardHandler
{
	private ArrayList<KeyBinding> keyBindings = new ArrayList<>();
	
	public void pollInput()
	{
		while(Keyboard.next())
		{
			if(Keyboard.getEventKeyState())
			{
				for(KeyBinding binding : keyBindings)
				{
					if(binding.id == Keyboard.getEventKey() || binding.id2 == Keyboard.getEventKey())
					{
						binding.pressed = true;
						break;
					}
				}
			} else
			{
				for(KeyBinding binding : keyBindings)
				{
					if(binding.id == Keyboard.getEventKey() || binding.id2 == Keyboard.getEventKey())
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
		for(KeyBinding binding : keyBindings)
		{
			binding.lastPressed = binding.pressed;
		}
	}
	
	public boolean removeKeyBinding(String name)
	{
		for(KeyBinding binding : keyBindings)
		{
			if(binding.name.equals(name))
			{
				keyBindings.remove(binding);
				return true;
			}
		}
		return false;
	}
	
	/**Adds a new key binding*/
	public KeyBinding addKeyBinding(String name, int key)
	{
		KeyBinding binding = new KeyBinding(name, key);
		keyBindings.add(binding);
		return binding;
	}
	
	/**Changes an already existent binding to the new key*/
	public void setKeyBinding1(String name, int key)
	{
		for(int i = 0; i < keyBindings.size(); i++)
		{
			if(keyBindings.get(i).name.equalsIgnoreCase(name))
			{
				keyBindings.get(i).id = key;
				break;
			}
		}
	}
	
	/**Changes an already existent binding to the new key*/
	public void setKeyBinding2(String name, int key)
	{
		for(int i = 0; i < keyBindings.size(); i++)
		{
			if(keyBindings.get(i).name.equalsIgnoreCase(name))
			{
				keyBindings.get(i).id2 = key;
				break;
			}
		}
	}
	
	public ArrayList<KeyBinding> getKeyBindings()
	{
		return keyBindings;
	}
	
	public boolean isKeyDown(String name)
	{
		for(int i = 0; i < keyBindings.size(); i++)
		{
			if(keyBindings.get(i).name.equalsIgnoreCase(name))
			{
				return keyBindings.get(i).pressed;
			}
		}
		return false;
	}
	
	public boolean wasKeyDownLastUpdate(String name)
	{
		for(int i = 0; i < keyBindings.size(); i++)
		{
			if(keyBindings.get(i).name.equalsIgnoreCase(name))
			{
				return keyBindings.get(i).lastPressed;
			}
		}
		return false;
	}
	
	public class KeyBinding extends Binding
	{
		private int id2;
		
		public KeyBinding()
		{
			name = "NEW MOUSE BINDING";
			id = 0;
			pressed = lastPressed = false;
		}
		
		private KeyBinding(String name, int key)
		{
			this.name = name;
			this.id = key;
		}
		
		public int getID2()
		{
			return id2;
		}
	}
}
