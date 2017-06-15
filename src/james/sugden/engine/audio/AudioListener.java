package james.sugden.engine.audio;

import static org.lwjgl.openal.AL10.AL_ORIENTATION;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_VELOCITY;
import static org.lwjgl.openal.AL10.alListener3f;
import james.sugden.engine.game_object.Component;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.file_handling.ResourceManager;

import java.awt.GridBagConstraints;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AudioListener extends Component
{
	protected void init()
	{
		super.typeName = "Audio Listener";
	}
	
	public void useListener(GameObject o)
	{
		alListener3f(AL_POSITION, o.getTransform().x, o.getTransform().y, 0);
		alListener3f(AL_VELOCITY, 0, 0, 0);
		alListener3f(AL_ORIENTATION, 0, 0, -1);
	}
	
	public void createEditingPanel(final JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj)
	{
		JButton btnSetAsDefault = new JButton("Set as Default");
		JLabel lblSetAsDefault = new JLabel("Set as Default Audio Listener:");
		lblSetAsDefault.setToolTipText("Sets the audio listener as the default audio listener for the scene. Replaces the current default audio listener");
		
		pnl.add(lblSetAsDefault);
		pnl.add(btnSetAsDefault);
	}
	
	public String[] getEditableFields(File fleProject) {return null;}
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject) {}
	public void close() {}
	public void drawAsEditor(GameObject thisGameObject,	GameObject selectedGameObject) {}
	public void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
}
