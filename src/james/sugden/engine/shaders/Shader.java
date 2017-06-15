package james.sugden.engine.shaders;

import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glUseProgram;
import james.sugden.engine.game_object.Component;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.file_handling.ResourceManager;

import java.awt.GridBagConstraints;
import java.io.File;

import javax.swing.JPanel;

public class Shader extends Component
{
	private int shaderProgram;
	
	public Shader(int shaderProgram)
	{
		this.shaderProgram = shaderProgram;
	}
	
	public void enable()
	{
		glUseProgram(shaderProgram);
	}
	
	public static void disable()
	{
		glUseProgram(0);
	}
	
	public void close()
	{
		glDeleteProgram(shaderProgram);
	}
	
	public int getProgram()
	{
		return shaderProgram;
	}
	
	protected void init() {}
	public void createEditingPanel(JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj) {}
	public String[] getEditableFields(File fleProject) {return null;}
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject) {}
	public void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
	public void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
}
