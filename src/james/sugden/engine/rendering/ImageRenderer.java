package james.sugden.engine.rendering;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glColor4f;
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
import java.io.File;
import java.nio.FloatBuffer;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lwjgl.BufferUtils;

import james.sugden.engine.game_object.DrawableComponent;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.file_handling.ResourceManager;
import james.sugden.utils.Texture;

public class ImageRenderer extends DrawableComponent
{
	/**The texture which is rendered*/
	protected Texture texture;
	
	/**Display list which renders the texture*/
	protected int vboID;
	
	/**If set to false then can only be drawn by an animator*/
	protected boolean drawTexture;
	
	/**The transparency the image is rendered at*/
	protected float alpha;
	
	public ImageRenderer()
	{
		super();
	}
	
	public ImageRenderer(ImageRenderer c)
	{
		super(c);
		
		this.texture = c.texture;
		this.vboID = c.vboID;
		this.drawTexture = c.drawTexture;
		this.alpha = c.alpha;
	}
	
	protected void init()
	{
		super.typeName = "Image Renderer";
		this.drawTexture = true;
		this.alpha = 1;
	}
	
	protected void createVBO()
	{
		float[] data = new float[]	//Vertex followed by Texture Coords
				{
					0, 0,
					0, 1,
					texture.getWidth(), 0,
					1, 1,
					texture.getWidth(), texture.getHeight(),
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
	
	public void createEditingPanel(final JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj)
	{
		JTextField txtTexture = new JTextField();
		if(texture != null)
		{
			txtTexture.setText(texture.getName());
			txtTexture.setForeground(Color.GREEN);
		}
		else
			txtTexture.setForeground(Color.RED);
		
		JLabel lblTexture = new JLabel("Texture:");
			lblTexture.setToolTipText("The texture which is drawn by the image renderer");
			
		JCheckBox chkDrawTexture = new JCheckBox();
		chkDrawTexture.setSelected(drawTexture);
		JLabel lblDrawTexture = new JLabel("Draw Texture:");
			lblDrawTexture.setToolTipText("Whether the texture is rendered or is not rendered");
		
		
		
		
		
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
				texture = resourceManager.findLoadImportTexture(txtTexture.getText(), ImageRenderer.this, texExists);
				if(texExists[0] == false)
					txtTexture.setForeground(Color.RED);
				else
					txtTexture.setForeground(Color.GREEN);
			}
		});
		
		chkDrawTexture.addActionListener(e ->
		{
			drawTexture = chkDrawTexture.isSelected();
		});
		
		
		
		pnl.add(lblTexture);
		pnl.add(txtTexture);
		pnl.add(lblDrawTexture);
		pnl.add(chkDrawTexture);
	}
	
	public void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject)
	{
		if(texture != null && drawTexture)
		{
			glPushMatrix();
			texture.bind();
			glColor4f(1f, 1f, 1f, alpha);
			if(!thisGameObject.getUseDepthValue())
				glTranslatef(-texture.getWidth()/2, -texture.getHeight()/2,		//transform.y/1000 used for depth testing - temporary
						(thisGameObject.getTransform().y - (thisGameObject.getTransform().scaleY * texture.getHeight()) /2) /1024);
			else
				glTranslatef(-texture.getWidth()/2, -texture.getHeight()/2, thisGameObject.getDepthValue());
			
			glBindBuffer(GL_ARRAY_BUFFER, vboID);
			
			glEnableClientState(GL_VERTEX_ARRAY);
			glVertexPointer(2, GL_FLOAT, 16, 0);	//Distance between x1 and x2 would be 4 because x, y, s, t. Times by 4 for 4 byte floats = 16 bytes
			
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(2, GL_FLOAT, 16, 8);	//Distance to first tex coord is 2 float so 8 bytes
			
			glDrawArrays(GL_QUADS, 0, 4);
			
			glPopMatrix();
		}
	}
	
	public void drawAsGame(GameObject thisGameObject)
	{
		if(texture != null && drawTexture)
		{
			glPushMatrix();
			texture.bind();
			glColor4f(1, 1, 1, alpha);
			
			if(!thisGameObject.getUseDepthValue())
				thisGameObject.setDepthValue((thisGameObject.getTransform().y - (thisGameObject.getTransform().scaleY * texture.getHeight()) /2) /1024);	//transform.y/1024 used for depth testing - 1024 random number, should replace with map height
			glTranslatef(-texture.getWidth()/2, -texture.getHeight()/2, thisGameObject.getDepthValue());
			
			glBindBuffer(GL_ARRAY_BUFFER, vboID);
			
			glEnableClientState(GL_VERTEX_ARRAY);
			glVertexPointer(2, GL_FLOAT, 16, 0);	//Distance between x1 and x2 would be 4 because x, y, s, t. Times by 4 for 4 byte floats = 16 bytes
			
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(2, GL_FLOAT, 16, 8);	//Distance to first tex coord is 2 floats so 8 bytes
			
			glDrawArrays(GL_QUADS, 0, 4);
			
			glPopMatrix();
		}
	}
	
	public void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
	public void updateAsGame(GameObject thisGameObject) {}
	
	public String[] getEditableFields(File fleProject)
	{
		String obj1;
		if(texture == null)
			obj1 = null;
		else
			obj1 = texture.getPath().substring((fleProject.getAbsolutePath() + "/res/images").length() + 1).replaceAll("\\\\", "/");
		System.err.println(obj1);
		return new String[]{obj1, String.valueOf(drawTexture), String.valueOf(alpha)};
	}
	
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject)
	{
		texture = resourceManager.findTexture(objs[0]);
		if(texture == null)
		{
			if(resourceManager.findImportedTexturePath(objs[0]))
			{
				resourceManager.addTextureToLoad(objs[0], ImageRenderer.this);
			}
		}
		drawTexture = Boolean.valueOf(objs[1]);
		alpha = Float.valueOf(objs[2]);
	}
	
	/**Set the texture reference to the one given*/
	public void setTexture(Texture tex)
	{
		this.texture = tex;
		this.createVBO();
	}
	
	public final Texture getTexture()
	{
		return texture;
	}
	
	/**Sets whether the texture is drawn or not when run in game mode*/
	public void setDrawTexture(boolean drawTexture)
	{
		this.drawTexture = drawTexture;
	}
	
	public final void setAlpha(float alpha)
	{
		this.alpha = alpha;
	}
	
	public final float getAlpha()
	{
		return alpha;
	}
	
	/**Called when program is closed or when the component is deleted*/
	public final void close()
	{
		//glDeleteBuffers(vboID);
	}
}
