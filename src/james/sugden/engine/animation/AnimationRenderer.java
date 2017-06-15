package james.sugden.engine.animation;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoordPointer;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lwjgl.BufferUtils;

import james.sugden.engine.Time;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.engine.rendering.ImageRenderer;
import james.sugden.file_handling.ResourceManager;
import james.sugden.utils.Texture;

public class AnimationRenderer extends ImageRenderer
{
	/**The number of cuts made to the sprite sheet*/
	private int noOfCuts;
	
	/**Time delay between switching images - milliseconds
	 * Last time the animation change image - milliseconds*/
	private long timeDelay, lastTimeChanged;
	
	/**The index of the current animation*/
	private int currentAnimation;
	
	/**The array of texture which are animated*/
	private AnimationTexture[] animationTextures;
	
	/**Keeps track of the order it sends textures to be loaded
	 * In set texture method takes the first texture from the queue*/
	private Queue<AnimationTexture> texturesBeingLoaded;
	
	public AnimationRenderer()
	{
		super();
	}
	
	/**Deep copy constructor*/
	public AnimationRenderer(AnimationRenderer c)
	{
		super(c);
		
		this.typeName = "Animation Renderer";
		this.noOfCuts = c.noOfCuts;
		this.currentAnimation = c.currentAnimation;
		this.timeDelay = c.timeDelay;
		this.lastTimeChanged = Time.getThisTime();
		
		this.texturesBeingLoaded = new LinkedList<>();
		for(AnimationTexture animationTexture : c.texturesBeingLoaded)
		{
			texturesBeingLoaded.add(new AnimationTexture(animationTexture));
		}
		
		this.animationTextures = new AnimationTexture[c.animationTextures.length];
		for(int i = 0; i < c.animationTextures.length; i++)
		{
			animationTextures[i] = new AnimationTexture(c.animationTextures[i]);
		}
	}
	
	protected void init()
	{
		this.typeName = "Animation Renderer";
		this.noOfCuts = 0;
		this.texturesBeingLoaded = new LinkedList<>();
		this.currentAnimation = 0;
		this.timeDelay = 0;
		this.lastTimeChanged = Time.getThisTime();
	}
	
	/**Overwritten from image renderer - makes the width proportionate to the number of cuts*/
	protected final void createVBO()
	{
		float[] data = new float[]	//Vertex followed by Texture Coords
				{
					0, 0,
					0, 1,
					texture.getWidth()/noOfCuts, 0,
					1, 1,
					texture.getWidth()/noOfCuts, texture.getHeight(),
					1, 0,
					0, texture.getHeight(),
					0, 0
				};
		
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		
		vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
	}
	
	@Override
	public void createEditingPanel(final JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventOBJ)
	{
		JTextField txtTexture = new JTextField();
		if(texture != null)
		{
			txtTexture.setText(texture.getName());
			txtTexture.setForeground(Color.GREEN);
		}
		else
			txtTexture.setForeground(Color.RED);
		
		JLabel lblTexture = new JLabel("Sprite Sheet:");
			lblTexture.setToolTipText("The image which can be cut into sections");
		
		SpinnerNumberModel mdlNoOfCuts = new SpinnerNumberModel();
		JSpinner spnNoOfCuts = new JSpinner(mdlNoOfCuts);
		mdlNoOfCuts.setValue(noOfCuts);
		
		SpinnerNumberModel mdlTimeDelay = new SpinnerNumberModel();
		JSpinner spnTimeDelay = new JSpinner(mdlTimeDelay);
		mdlTimeDelay.setValue(timeDelay);
		
		JLabel lblNoOfCuts = new JLabel("Number of Cuts:");
			lblNoOfCuts.setToolTipText("The number of times the sprite sheet is cut into equal sections");
		JLabel lblTimeDelay = new JLabel("Time Delay:");
			lblNoOfCuts.setToolTipText("The time delay between switching image (milliseconds)");
		
		
		spnTimeDelay.addChangeListener(e1 ->
		{
			timeDelay = mdlTimeDelay.getNumber().intValue();
		});
		
		
		mdlNoOfCuts.addChangeListener(e1 ->
		{
			try {
				noOfCuts = mdlNoOfCuts.getNumber().intValue();		//Save the number so it can be applied on loading
				if(texture != null && texture.getName() != null)	//Allows texture cutting if the texture has been loaded
				{
					BufferedImage img = ImageIO.read(new File(texture.getPath()));
					makeCuts(img, resourceManager);
				}
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		});
		
		
		
		txtTexture.setTransferHandler(new TransferHandler("text"));	//Enable text to be dropped onto the text field
		/**Key listener used to set the value as it's being typed*/
		txtTexture.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override public void insertUpdate(DocumentEvent e) {doChanges();}
			@Override public void removeUpdate(DocumentEvent e) {doChanges();}
			@Override public void changedUpdate(DocumentEvent e) {doChanges();}
			
			private void doChanges()
			{
				boolean[] texExists = new boolean[1];
				texture = resourceManager.findLoadImportTexture(txtTexture.getText(), AnimationRenderer.this, texExists);
				if(texExists[0] == false)
					txtTexture.setForeground(Color.RED);
				else
					txtTexture.setForeground(Color.GREEN);
			}
		});
		
		pnl.add(lblTexture);
		pnl.add(txtTexture);
		pnl.add(lblNoOfCuts);
		pnl.add(spnNoOfCuts);
		pnl.add(lblTimeDelay);
		pnl.add(spnTimeDelay);
	}
	
	public void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject)
	{
		
	}
	
	public void updateAsGame(GameObject thisGameObject)
	{
		if((Time.getThisTime() - lastTimeChanged) >= timeDelay && animationTextures != null)
		{
			currentAnimation ++;
			if(currentAnimation >= animationTextures.length)
				currentAnimation = 0;
			lastTimeChanged = Time.getThisTime();
		}
	}
	
	public void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
	public void drawAsGame(GameObject thisGameObject) {}
	
	/**Used rather than drawAsEditor so only the current animation is drawn from the animator*/
	public final void drawAsEditorFromAnimator(GameObject thisGameObject, GameObject selectedGameObject)
	{
		if(animationTextures != null && animationTextures.length > 0 && animationTextures[0].tex != null && texture != null)
		{
			glPushMatrix();
			glColor3f(1, 1, 1);
			
			glTranslatef(-texture.getWidth()/(noOfCuts+1)/2, -texture.getHeight()/2,
					(thisGameObject.getTransform().y - (thisGameObject.getTransform().scaleY * texture.getHeight()) /2) /1000);
			animationTextures[0].tex.bind();
			
			glBindBuffer(GL_ARRAY_BUFFER, vboID);
			
			glEnableClientState(GL_VERTEX_ARRAY);
			glVertexPointer(2, GL_FLOAT, 16, 0);	//Distance between x1 and x2 would be 4 because x, y, s, t. Times by 4 for 4 byte floats = 16 bytes
			
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(2, GL_FLOAT, 16, 8);	//Distance to first tex coord is 2 float so 8 bytes
			
			glDrawArrays(GL_QUADS, 0, 4);
			
			glPopMatrix();
		}
	}
	
	/**Used rather than drawAsEditor so only the current animation is drawn from the animator*/
	public final void drawAsGameFromAnimator(GameObject thisGameObject)
	{
		if(animationTextures != null && animationTextures.length > 0 && animationTextures[0].tex != null)
		{
			glPushMatrix();
			glColor3f(1, 1, 1);
			
			if(!thisGameObject.getUseDepthValue())
				glTranslatef(-texture.getWidth()/(noOfCuts+1)/2, -texture.getHeight()/2,		//transform.y/1000 used for depth testing - temporary
						(thisGameObject.getTransform().y - (thisGameObject.getTransform().scaleY * animationTextures[currentAnimation].tex.getHeight()) /2) /1024);
			else
				glTranslatef(-texture.getWidth()/(noOfCuts+1)/2, -texture.getHeight()/2, thisGameObject.getDepthValue());
			
			animationTextures[currentAnimation].tex.bind();
			
			glBindBuffer(GL_ARRAY_BUFFER, vboID);
			
			glEnableClientState(GL_VERTEX_ARRAY);
			glVertexPointer(2, GL_FLOAT, 16, 0);	//Distance between x1 and x2 would be 4 because x, y, s, t. Times by 4 for 4 byte floats = 16 bytes
			
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(2, GL_FLOAT, 16, 8);	//Distance to first tex coord is 2 float so 8 bytes
			
			glDrawArrays(GL_QUADS, 0, 4);
			
			glPopMatrix();
		}
	}
	
	public final void makeCuts(BufferedImage img, ResourceManager resourceManager)
	{
		//Cannot try to load new textures until all previous are finished
		if(!texturesBeingLoaded.isEmpty())
			return;
		
		animationTextures = new AnimationTexture[noOfCuts+1];
		for(int i = 0; i <= noOfCuts; i++)
		{
			BufferedImage bImg = img.getSubimage(img.getWidth()/(noOfCuts+1)*i, 0, img.getWidth()/(noOfCuts+1), img.getHeight());
			animationTextures[i] = new AnimationTexture();
			animationTextures[i].tex = null;
			animationTextures[i].img = bImg;
			texturesBeingLoaded.add(animationTextures[i]);
			resourceManager.addTextureToLoad(bImg, (ImageRenderer)this);
		}
	}
	
	public final void setTexture(Texture tex)
	{
		if(!texturesBeingLoaded.isEmpty())
		{
			AnimationTexture animTex = texturesBeingLoaded.remove();
			animTex.tex = tex;
		}
		else
		{
			texture = tex;
			if(noOfCuts == 0)	//Overwritten version requires dividing by no of cuts so check divisor isn't zero
				super.createVBO();
			else
				this.createVBO();
		}
	}
	
	public final AnimationTexture[] getAnimationTextures()
	{
		return animationTextures;
	}
	
	@Override
	public final String[] getEditableFields(File fleProject)
	{
		String obj1;
		if(texture == null)
			obj1 = null;
		else
			obj1 = texture.getName();
		
		Object obj2 = noOfCuts;
		Object obj3 = timeDelay;
		
		return new String[]{obj1, String.valueOf(obj2), String.valueOf(obj3)};
	}
	
	@Override
	public final void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject)
	{
		if(objs[2] != null)
		{
			timeDelay = Integer.valueOf(objs[2]);
		}
		
		if(objs[1] != null)
		{
			try {
				noOfCuts = Integer.valueOf(objs[1]);
				if(objs[0] != null && !(objs[0]).equals("null"))
					makeCuts(ImageIO.read(new File(fleProject+"/res/images/"+objs[0])), resourceManager);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		texture = resourceManager.findLoadImportTexture(objs[0], AnimationRenderer.this, new boolean[1]);
	}
	
	public final long getTimeDelay()
	{
		return timeDelay;
	}
	
	public class AnimationTexture
	{
		public Texture tex;
		public Image img;
		public int pDisplayList;
		
		private AnimationTexture()
		{
			
		}
		
		private AnimationTexture(AnimationTexture t)
		{
			this.tex = t.tex;
			this.img = t.img;
			this.pDisplayList = t.pDisplayList;
		}
	}
}
