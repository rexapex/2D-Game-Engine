package james.sugden.engine.camera;

import java.io.File;

import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.maths.Transform;
import james.sugden.file_handling.ResourceManager;

public class FixedCamera extends Camera
{
	public FixedCamera(float x, float y, float z)
	{
		super(x, y, z);
		
		this.typeName = "Fixed Camera";
	}
	
	public FixedCamera(Transform t)
	{
		super(t.x, t.y, t.rotation);
		
		this.typeName = "Fixed Camera";
	}
	
	public final void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
	
	public final void updateAsGame(GameObject thisGameObject) {}
	
	public void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}

	@Override
	public String[] getEditableFields(File fleProject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject) {
		// TODO Auto-generated method stub
		
	}
}
