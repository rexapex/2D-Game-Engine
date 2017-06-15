package james.sugden.engine.audio;

import static org.lwjgl.openal.AL10.AL_FALSE;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_PITCH;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_TRUE;
import static org.lwjgl.openal.AL10.AL_VELOCITY;
import static org.lwjgl.openal.AL10.alSource3f;
import static org.lwjgl.openal.AL10.alSourcePause;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lwjgl.openal.AL10;

import james.sugden.engine.game_object.Component;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.file_handling.ResourceManager;
import james.sugden.utils.AudioBuffer;

public class AudioSource extends Component
{
	private int source;
	private AudioBuffer buffer;
	
	/**If true the audio plays when the scene finished loading*/
	private boolean playAtStart;
	
	/**Loops the track once it ends if true*/
	private boolean loop;
	
	public AudioSource()
	{
		
	}
	
	protected void init()
	{
		super.typeName = "Audio Source";
	}
	
	public final void play()
	{
		int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
		
		if(state != AL10.AL_STOPPED)
			stop();
		
		if(loop)
			alSourcei(source, AL_LOOPING, AL_TRUE);
		else
			alSourcei(source, AL_LOOPING, AL_FALSE);
		
		alSourcePlay(source);
	}
	
	public final void pause()
	{
		alSourcePause(source);
	}
	
	public final void stop()
	{
		alSourceStop(source);
	}
	
	public final void setSourceAndBuffer(int source, AudioBuffer buffer)
	{
		this.source = source;
		this.buffer = buffer;
	}
	
	public final int getSource()
	{
		return source;
	}
	
	public final void setPitch(float val)
	{
		alSourcef(source, AL_PITCH, val);
	}
	
	public final void setGain(float val)
	{
		alSourcef(source, AL_GAIN, val);
	}
	
	public final void setPosition(float x, float y)
	{
		alSource3f(source, AL_POSITION, x, y, 0);
	}
	
	public final void setVelocity(float x, float y)
	{
		alSource3f(source, AL_VELOCITY, x, y, 0);
	}
	
	public final void setLooping(boolean val)
	{
		alSourcei(source, AL_LOOPING, val ? AL_TRUE : AL_FALSE);
	}
	
	public final boolean playAtStart()
	{
		return playAtStart;
	}
	
	public final boolean loop()
	{
		return loop;
	}
	
	public void createEditingPanel(final JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj)
	{
		JTextField txtAudioBuffer = new JTextField();
		if(buffer != null)
			txtAudioBuffer.setText(buffer.getName());
		
		JLabel lblAudioBuffer = new JLabel("Audio Buffer:");
		lblAudioBuffer.setToolTipText("The audio to be played by the audio source");
		
		JCheckBox chkPlayAtStart = new JCheckBox();
		chkPlayAtStart.setSelected(playAtStart);
		JLabel lblPlayAtStart = new JLabel("Play at Start:");
		
		JCheckBox chkLoop = new JCheckBox();
		chkLoop.setSelected(loop);
		JLabel lblLoop = new JLabel("Loop:");
		
		
		txtAudioBuffer.setTransferHandler(new TransferHandler("text"));	//Enable text to be dropped onto the text field
		/**Key listener used to set the value as it's being typed*/
		txtAudioBuffer.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override public void insertUpdate(DocumentEvent e) {doChanges();}
			@Override public void removeUpdate(DocumentEvent e) {doChanges();}
			@Override public void changedUpdate(DocumentEvent e) {doChanges();}
			
			private void doChanges()
			{
				if(buffer != null)
					buffer.deleteSource(AudioSource.this);
				buffer = resourceManager.findLoadImportAudioBuffer(txtAudioBuffer.getText(), AudioSource.this);
				if(buffer == null)
					txtAudioBuffer.setForeground(Color.RED);
				else
					txtAudioBuffer.setForeground(Color.GREEN);
			}
		});
		
		chkPlayAtStart.addActionListener(e ->
		{
			playAtStart = chkPlayAtStart.isSelected();
		});
		
		chkLoop.addActionListener(e ->
		{
			loop = chkLoop.isSelected();
		});
		
		pnl.add(lblAudioBuffer);
		pnl.add(txtAudioBuffer);
		pnl.add(lblPlayAtStart);
		pnl.add(chkPlayAtStart);
		pnl.add(lblLoop);
		pnl.add(chkLoop);
	}
	
	public String[] getEditableFields(File fleProject)
	{
		if(buffer != null)
			return new String[]{buffer.getPath().substring((fleProject.getAbsolutePath() + "/res/sounds").length() + 1).replaceAll("\\\\", "/"), String.valueOf(playAtStart)};
		else
			return new String[]{"null", String.valueOf(playAtStart)};
	}
	
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject)
	{
		buffer = resourceManager.findAudioBuffer(objs[0]);
		if(buffer == null)
		{
			if(resourceManager.findImportedAudioBufferPath(objs[0]))
			{
				resourceManager.addAudioBufferToLoad(objs[0], AudioSource.this);
			}
		}
		playAtStart = Boolean.valueOf(objs[1]);
	}
	
	public void close()
	{
		stop();
		if(buffer != null)
			buffer.deleteSource(this);
	}
	
	public void drawAsEditor(GameObject thisGameObject,	GameObject selectedGameObject) {}
	public void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
}
