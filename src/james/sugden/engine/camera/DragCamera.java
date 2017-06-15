package james.sugden.engine.camera;

import james.sugden.engine.Lookup;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.maths.Transform;
import james.sugden.file_handling.ResourceManager;

import java.io.File;

import org.lwjgl.input.Mouse;

public class DragCamera extends Camera
{
	public DragCamera(float x, float y, float rotation)
	{
		super(x, y, rotation);
	}
	
	public DragCamera(Transform t)
	{
		super(t.x, t.y, t.rotation);
		
		this.typeName = "Fixed Camera";
	}
	
	public final void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject)
	{
		/**Panning*/
		if(Mouse.isButtonDown(2))
		{
			transform.x += Lookup.getInput().getMouseDX() * 0.3f / transform.scaleX;
			transform.y += Lookup.getInput().getMouseDY() * 0.3f / transform.scaleY;
		}
		
		/**Zooming*/
		float zoomVal = Lookup.getInput().getMouseDWheel() * 0.0001f;
		transform.scaleX += zoomVal;
		transform.scaleY += zoomVal;
		if(transform.scaleX > 2)
		{
			transform.scaleX = 2;
			transform.scaleY = 2;
		} else if(transform.scaleX < 0.25f)
		{
			transform.scaleX = 0.25f;
			transform.scaleY = 0.25f;
		}
	}
	
	public final void updateAsGame(GameObject thisGameObject)
	{
		if(Mouse.isButtonDown(0))
		{
			transform.x += Lookup.getInput().getMouseDX() * 0.1f;
			transform.y += Lookup.getInput().getMouseDY() * 0.1f;
		}
	}
	
	public void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}

	@Override
	public String[] getEditableFields(File fleProject)
	{
		return new String[]{""+transform.x, ""+transform.y, ""+transform.rotation, ""+transform.scaleX, ""+transform.scaleY};
	}

	@Override
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject)
	{
		transform.x = Float.valueOf(objs[0]);
		transform.y = Float.valueOf(objs[1]);
		transform.rotation = Float.valueOf(objs[2]);
		transform.scaleX = Float.valueOf(objs[3]);
		transform.scaleY = Float.valueOf(objs[4]);
	}
}
