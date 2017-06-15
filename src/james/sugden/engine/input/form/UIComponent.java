package james.sugden.engine.input.form;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glTexCoordPointer;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import james.sugden.utils.Texture;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public abstract class UIComponent
{
	/**The vbo used to render all components of this ui*/
	private int vboID;
	
	/**The position of the component as a percentage of the screen*/
	protected float x, y;
	
	/**The size of the component as a percentage of the screen*/
	protected float width, height;
	
	/**Sets up the position and dimensions variables
	 * All values given as percentage of screen
	 * If value is less than 0, value will be 0, if value is greater than 100, value will be 100*/
	public UIComponent(float x, float y, float width, float height)
	{
		this.x = x < 0 ? 0 : x > 100 ? 100 : x;
		this.y = y < 0 ? 0 : y > 100 ? 100 : y;
		this.width = width < 0 ? 0 : width > 100 ? 100 : width;
		this.height = height < 0 ? 0 : height > 100 ? 100 : height;
	}
	
	/**Creates a vbo of all components in the ui*/
	public final void createVBO()
	{
		float[] data = new float[]	//Vertex and tex coord data
				{
					x, y, 0, 0,
					x+width, y, 1, 0,
					x+width, y+height, 1, 1,
					x, y+height, 0, 1
				};
		
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);	//Create buffer to be the exact size required
		buffer.put(data);	//Put the vertex and tex coord data into the buffer
		buffer.flip();	//Flip the buffer - Important
		
		vboID = glGenBuffers();	//Generate a vbo for this ui component
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);	//Pass the buffer to the vbo
	}
	
	/**Returns the ui component on top which is moused over
	 * Can be ovewritten so that it returns child components over parent components in container components*/
	public final UIComponent mouseOver(float mousex, float mousey)	//mousex and mousey are in percentage across screen
	{
		if(mousex >= x && mousex <= x + width && mousey >= y && mousey <= y + height)	//If the mouse position lies within the rectangle bounds
			return this;	//Returns itself as the ui component moused over
		else
			return null;
	}
	
	/**Render the vbo for this ui component*/
	public final void draw()
	{
		if(getTexture() != null)
		{
			getTexture().bind();
			
			glBindBuffer(GL_ARRAY_BUFFER, vboID);
			
			glEnableClientState(GL_VERTEX_ARRAY);
			glVertexPointer(2, GL_FLOAT, 16, 0);	//Distance between x1 and x2 would be 4 because x, y, s, t. Times by 4 for 4 byte floats = 16 bytes
			
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(2, GL_FLOAT, 16, 8);	//Distance to first tex coord is 2 float so 8 bytes
			
			glDrawArrays(GL_QUADS, 0, 4);
		}
	}
	
	/**Returns the texture to use for this render cycle*/
	protected abstract Texture getTexture();
}
