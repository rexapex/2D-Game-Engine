package james.sugden.engine.camera;

import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import james.sugden.engine.game_object.UpdatableComponent;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.engine.maths.Transform;
import james.sugden.file_handling.ResourceManager;

public abstract class Camera extends UpdatableComponent
{
	protected Transform transform;
	
	public Camera()
	{
		transform = new Transform(0, 0, 0);
	}
	
	public Camera(float x, float y, float rotation)
	{
		transform = new Transform(x, y, rotation);
	}
	
	public void applyTransform()
	{
		glScalef(transform.scaleX, transform.scaleY, 1);
		glRotatef(transform.rotation, 0, 0, 1);
		glTranslatef(transform.x, transform.y, 0);
	}
	
	public void createEditingPanel(final JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj)
	{
		
	}
	
	protected void init() {}
	
	public void close() {}
	
	public final Transform getTransform()
	{
		return transform;
	}
}