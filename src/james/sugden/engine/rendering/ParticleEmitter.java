package james.sugden.engine.rendering;

import static org.lwjgl.opengl.GL11.GL_COLOR_ARRAY;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glColorPointer;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glTexCoordPointer;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glMapBuffer;
import static org.lwjgl.opengl.GL15.glUnmapBuffer;
import james.sugden.engine.Lookup;
import james.sugden.engine.Time;
import james.sugden.engine.game_object.DrawableComponent;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.engine.maths.Transform;
import james.sugden.file_handling.ResourceManager;
import james.sugden.utils.Texture;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.lwjgl.BufferUtils;

public class ParticleEmitter extends DrawableComponent
{
	/**The transform relative to the game object*/
	private Transform transform;
	
	/**Whether the particle system is enabled or disabled
	 * After being disabled, the stops spawning particles but continues to update live ones until they die*/
	private boolean enabled;
	
	/**All the positions of particles which are currently being rendered*/
	private List<Particle> particles;
	
	/**VBO ID which stores vertex and colour info of all particles*/
	private int vboID;
	
	/**Stores and sends vertex, colour and texture coords to vbo*/
	private ByteBuffer oldBuffer;
	
	/**Values set by the user to determine how the particles will look and behave*/
	private float particleWidth, particleHeight;
	//private float rotationBound1, rotationBound2;
	
	/**If a texture is provided, it will be mapped to every particle*/
	private Texture tex;
	
	/**Whether gravity is applied to the particles, if no source is provided, particles are pulled down*/
	private boolean applyGravity;
	/**The point where particles are pulled to if gravity is enabled*/
	private GameObject gravitySource;
	
	/**Colour values for the particles at the centre of the particle emitter*/
	private float red, green, blue;
	
	public ParticleEmitter()
	{
		particleWidth = 4;
		particleHeight = 4;
		applyGravity = false;
		gravitySource = null;
		red = blue = green = 1;
		enabled = true;
	}
	
	/**Constructs the particle system with the given properties
	 * Rotation bounds determines which direction the particles are emitted in and are measured in degrees*/
	public ParticleEmitter(float partWidth, float partHeight, float partSizeVariation, boolean varyPartSize, float rotationBound1,
			float rotationBound2, boolean applyGravity, Color colour)
	{
		this.particleWidth = partWidth;
		this.particleHeight = partHeight;
	//	this.rotationBound1 = rotationBound1;
	//	this.rotationBound2 = rotationBound2;
		this.tex = null;
		this.applyGravity = applyGravity;
		this.gravitySource = null;
		this.red = colour.getRed()/255;
		this.green = colour.getGreen()/255;
		this.blue = colour.getBlue()/255;
		this.enabled = true;
	}
	
	/**Deep copy constructor*/
	public ParticleEmitter(ParticleEmitter emitter)
	{
		super(emitter);
		this.transform = new Transform(emitter.transform);
		this.particleWidth = emitter.particleWidth;
		this.particleHeight = emitter.particleHeight;
		this.tex = emitter.tex;
		this.applyGravity = emitter.applyGravity;
		this.gravitySource = emitter.gravitySource;
		this.red = emitter.red;
		this.green = emitter.green;
		this.blue = emitter.blue;
		this.enabled = emitter.enabled;
	}
	
	protected final void init()
	{
		typeName = "Particle Emitter";
		particles = new ArrayList<>();
		transform = new Transform(0, 0, 0);
	}
	
	/**Creates a vbo to store all particle vertices and colours
	 * Must be called from opengl thread*/
	public final void createVBO()
	{
		//Vertex data followed by colour data followed by tex coords
		oldBuffer = BufferUtils.createByteBuffer(4200);	//4200 byte long buffer because max particles is 1 and each has 28 bytes, x, y, r, g, b, s, t
		
		vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, oldBuffer, GL_DYNAMIC_DRAW);
	}
	
	public final void updateAsGame(GameObject thisGameObject)
	{
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		ByteBuffer byteBuffer = glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, oldBuffer);
		FloatBuffer newBuffer = byteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		long thisTime = Time.getThisTime();
		for(int i = 0; i < particles.size(); i++)
		{
			if(particles.get(i).update(thisTime, newBuffer))
			{
				particles.remove(i);
			}
		}
		glUnmapBuffer(GL_ARRAY_BUFFER);
		
		if(enabled)
		{
			spawnParticle(thisTime);
		}
	}
	
	public final void drawAsGame(GameObject thisGameObject)
	{
		if(tex != null)
			tex.bind();
		else
			Texture.unbind();
		
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(2, GL_FLOAT, 28, 0);	//Distance between x1 and x2 would be 5 because x, y, r, g, b. Times by 5 for 4 byte floats = 20 bytes
		
		glEnableClientState(GL_COLOR_ARRAY);
		glColorPointer(3, GL_FLOAT, 28, 8);	//Distance to first colour is 2 float so 8 bytes
		
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glTexCoordPointer(2, GL_FLOAT, 28, 20);
		
		glDrawArrays(GL_QUADS, 0, 4*particles.size());
		
		glDisableClientState(GL_VERTEX_ARRAY);
		glDisableClientState(GL_COLOR_ARRAY);
	}
	
	/**Spawns a new particles with random speed and direction*/
	private final void spawnParticle(long thisTime)
	{
		if(particles.size() < 150)
		{
			/**Random rotation between the two bounds*/
		//	float rotation = Lookup.getRandom().nextFloat() * (rotationBound2 - rotationBound1) + rotationBound1;
			
			if(Lookup.getRandom().nextInt(3) == 0)
			{
				float xVelocity = (Lookup.getRandom().nextFloat()-0.5f) * 10;
				float yVelocity = (Lookup.getRandom().nextFloat()-0.5f) * 12;
				
				particles.add(new Particle(thisTime + 1000, xVelocity, yVelocity));
			}
		}
	}
	
	public void createEditingPanel(JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj)
	{
		JPanel pnlPos = new JPanel(new GridLayout(1, 2));
		SpinnerNumberModel mdlX = new SpinnerNumberModel();
			mdlX.setValue(transform.x);
			JSpinner spnX = new JSpinner(mdlX);
		SpinnerNumberModel mdlY = new SpinnerNumberModel();
			mdlY.setValue(transform.y);
			JSpinner spnY = new JSpinner(mdlY);
		JLabel lblPos = new JLabel("Position (X, Y):");
			lblPos.setToolTipText("The relative position of the particle emitter");
		pnlPos.add(spnX);
		pnlPos.add(spnY);
		
		JPanel pnlRotation = new JPanel(new GridLayout(1, 0));
		SpinnerNumberModel mdlRotation = new SpinnerNumberModel();
			mdlRotation.setValue(transform.rotation);
			JSpinner spnRotation = new JSpinner(mdlRotation);
		JLabel lblRotation = new JLabel("Rotation:");
			lblRotation.setToolTipText("The relative rotation of the particle emitter");
		pnlRotation.add(spnRotation);
		
		JPanel pnlScale = new JPanel(new GridLayout(1, 0));
		SpinnerNumberModel mdlScaleX = new SpinnerNumberModel(transform.scaleX, -25, 25, 0.25f);
			JSpinner spnScaleX = new JSpinner(mdlScaleX);
		SpinnerNumberModel mdlScaleY = new SpinnerNumberModel(transform.scaleY, -25, 25, 0.25f);
			JSpinner spnScaleY = new JSpinner(mdlScaleY);
		JLabel lblScale = new JLabel("Scale (X, Y):");
			lblScale.setToolTipText("The relative scale of the particle emitter");
		pnlScale.add(spnScaleX);
		pnlScale.add(spnScaleY);
		
		JPanel pnlParticleDimensions = new JPanel(new GridLayout(1, 2));
		SpinnerNumberModel mdlParticleWidth = new SpinnerNumberModel();
			mdlParticleWidth.setValue(particleWidth);
			JSpinner spnParticleWidth = new JSpinner(mdlParticleWidth);
		SpinnerNumberModel mdlParticleHeight = new SpinnerNumberModel();
			mdlParticleHeight.setValue(particleHeight);
			JSpinner spnParticleHeight = new JSpinner(mdlParticleHeight);
		JLabel lblParticleDimensions = new JLabel("Particle Dimensions (Width, Height):");
			lblParticleDimensions.setToolTipText("The dimensions of the emitted particles");
		pnlParticleDimensions.add(spnParticleWidth);
		pnlParticleDimensions.add(spnParticleHeight);
		
		//Texture must be bound for particles to be rendered when lighting is active
		JTextField txtTexture = new JTextField();
		if(tex != null)
			txtTexture.setText(new File(tex.getPath()).getName());
		
		JPanel pnlGravity = new JPanel(new GridLayout(0, 2));
		JCheckBox chkApplyGravity = new JCheckBox();
		JTextField txtGravitySource = new JTextField();
		if(gravitySource != null)
			txtGravitySource.setText(gravitySource.getName());
		txtGravitySource.setEnabled(applyGravity);
		chkApplyGravity.setSelected(applyGravity);
		JLabel lblApplyGravity = new JLabel("Apply Gravity:");
			lblApplyGravity.setToolTipText("Whether the particles emitted are affected by gravity");
		pnlGravity.add(chkApplyGravity);
		pnlGravity.add(txtGravitySource);
		
		JLabel lblColour = new JLabel("Colour (Red, Green, Blue):");
			lblColour.setToolTipText("The colour of the particles");
		JButton btnChooseColour = new JButton("Choose Colour");
		
		mdlX.addChangeListener(e ->
		{
			transform.x = mdlX.getNumber().floatValue();
		});
		mdlY.addChangeListener(e ->
		{
			transform.y = mdlY.getNumber().floatValue();
		});
		mdlRotation.addChangeListener(e ->
		{
			transform.rotation = mdlRotation.getNumber().floatValue();
		});
		mdlScaleX.addChangeListener(e ->
		{
			transform.scaleX = mdlScaleX.getNumber().floatValue();
		});
		mdlScaleY.addChangeListener(e ->
		{
			transform.scaleY = mdlScaleY.getNumber().floatValue();
		});
		mdlParticleWidth.addChangeListener(e ->
		{
			particleWidth = mdlParticleWidth.getNumber().floatValue();
		});
		mdlParticleHeight.addChangeListener(e ->
		{
			particleHeight = mdlParticleHeight.getNumber().floatValue();
		});
		
		chkApplyGravity.addChangeListener(e ->
		{
			applyGravity = chkApplyGravity.isSelected();
			txtGravitySource.setEnabled(applyGravity);
		});
		txtGravitySource.addKeyListener(new KeyListener()
		{
			public void keyTyped(KeyEvent e)
			{
				GameObject o = Lookup.getGameObjectWithName(txtGravitySource.getName());
				if(o != null)
					gravitySource = o;
			}
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});
		
		btnChooseColour.addActionListener(e ->
		{
			JDialog dlgColourChooser = new JDialog();
			dlgColourChooser.setAlwaysOnTop(true);
			dlgColourChooser.setTitle("Change colour of: " + identificationName);
			
			JColorChooser chrColour = new JColorChooser();
			chrColour.setPreviewPanel(new JPanel());
			chrColour.getSelectionModel().addChangeListener(e1 ->
			{
				red = chrColour.getColor().getRed()/255;
				green = chrColour.getColor().getGreen()/255;
				blue = chrColour.getColor().getBlue()/255;
			});
			
			dlgColourChooser.add(chrColour);
			dlgColourChooser.pack();
			dlgColourChooser.setVisible(true);
		});
		
		pnl.add(lblPos);
		pnl.add(pnlPos);
		pnl.add(lblRotation);
		pnl.add(pnlRotation);
		pnl.add(lblScale);
		pnl.add(pnlScale);
		pnl.add(lblParticleDimensions);
		pnl.add(pnlParticleDimensions);
		pnl.add(lblApplyGravity);
		pnl.add(pnlGravity);
		pnl.add(lblColour);
		pnl.add(btnChooseColour);
	}
	
	public String[] getEditableFields(File fleProject)
	{
		return new String[]{transform.x+","+transform.y+","+transform.rotation+","+transform.scaleX+","+transform.scaleY,
				particleWidth+","+particleHeight, applyGravity+","+gravitySource, red+","+green+","+blue};
	}
	
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject)
	{
		transform = new Transform(Float.valueOf((objs[0]).split(",")[0]), Float.valueOf(((String)objs[0]).split(",")[1]), Float.valueOf(((String)objs[0]).split(",")[2]));
		transform.scaleX = Float.valueOf((objs[0]).split(",")[3]);
		transform.scaleY = Float.valueOf((objs[0]).split(",")[4]);
		particleWidth = Float.valueOf((objs[1]).split(",")[0]);
		particleHeight = Float.valueOf((objs[1]).split(",")[1]);
		applyGravity = Boolean.valueOf((objs[2]).split(",")[0]);
		gravitySource = null;
		red = Float.valueOf((objs[3]).split(",")[0]);
		green = Float.valueOf((objs[3]).split(",")[1]);
		blue = Float.valueOf((objs[3]).split(",")[2]);
	}
	
	public void close() {}
	public void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
	public void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
	
	/**Set the particle emitter to be enable or disabled*/
	public final void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	
	private final class Particle
	{
		private float x, y, xVelocity, yVelocity, red, green, blue;
		
		/**At this time the particle dies*/
		private long aliveUntil;
		
		private Particle(long aliveUntil, float xVelocity, float yVelocity)
		{
			this.aliveUntil = aliveUntil;
			this.x = 0;
			this.y = 0;
			this.xVelocity = xVelocity;
			this.yVelocity = yVelocity;
			this.red = ParticleEmitter.this.red;
			this.green = ParticleEmitter.this.green;
			this.blue = ParticleEmitter.this.blue;
		}
		
		/**Return of true means particle has passed it's life time so should be removed from the list*/
		private boolean update(long thisTime, FloatBuffer buffer)
		{
			if(thisTime >= aliveUntil)
				return true;
			else
			{
				x += xVelocity;
				y += yVelocity;
				
				if(applyGravity)
				{
					yVelocity--;
				}
				
				//Colour of particle gets darker the further away from the centre it is
				//float attenuation = 1/(float)(Math.sqrt(x*y));
				//red *= attenuation;
				//green *= attenuation;
				//blue *= attenuation;
				
				buffer.put(x);
				buffer.put(y);
				buffer.put(red);
				buffer.put(green);
				buffer.put(blue);
				buffer.put(0);
				buffer.put(0);
				buffer.put(x + particleWidth);
				buffer.put(y);
				buffer.put(red);
				buffer.put(green);
				buffer.put(blue);
				buffer.put(1);
				buffer.put(0);
				buffer.put(x + particleWidth);
				buffer.put(y + particleHeight);
				buffer.put(red);
				buffer.put(green);
				buffer.put(blue);
				buffer.put(1);
				buffer.put(1);
				buffer.put(x);
				buffer.put(y + particleHeight);
				buffer.put(red);
				buffer.put(green);
				buffer.put(blue);
				buffer.put(0);
				buffer.put(1);
				
				return false;
			}
		}
	}
}
