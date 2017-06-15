package james.sugden.utils;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class RenderingUtilsNEW	//TODO - Add in vbo rendering of utils for all shapes listed
{
	/**Stores the vertices of the shapes if compiled*/
	private static int vboIDCircle;//, vboIDFilledCircle, vboIDArrow, vboIDRectangle;
	
	/**If the rendering utils wishes to be used, a vbo must first be created for the shapes*/
	public static void createVBOCircle(float radius)
	{
		float[] vertices = new float[720];
		for (int i = 0; i < 360; i += 2)
	    {
			vertices[i] = (float)Math.cos(Math.toRadians(i) * radius);
			vertices[i + 1] = (float)Math.sin(Math.toRadians(i) * radius);
	    }
		
		FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
		buffer.put(vertices);
		buffer.flip();
		
		vboIDCircle = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboIDCircle);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
	}
	
	
	
	
	/**Deletes all of the vbos used by rendering utils*/
	public static final void deleteVBOs()
	{
		glDeleteBuffers(vboIDCircle);
	}
	
	
	
	
	
	public final static void drawCircle(float radius)
	{
		glBindBuffer(GL_ARRAY_BUFFER, vboIDCircle);
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(2, GL_FLOAT, 16, 0);	//Distance between x1 and x2 would be 4 because x, y, s, t. Times by 4 for 4 byte floats = 16 bytes
		
		glDrawArrays(GL_LINE_LOOP, 0, 2);
	}
	
	public final static void drawFilledCircle(float radius, float x, float y)
	{
		int triangleAmount = 20; //Number of triangles used to draw circle
		
		float twicePi = (float)(2.0f * Math.PI);
		
		glBegin(GL_TRIANGLE_FAN);
			glVertex2f(x, y); //Centre of circle
			for(int i = 0; i <= triangleAmount; i++)
			{ 
				glVertex2f(x + (radius * (float)Math.cos(i *  twicePi / triangleAmount)), y + (radius * (float)Math.sin(i * twicePi / triangleAmount)));
			}
		glEnd();
	}
	
	public static void drawArrow(float length, float rotation)
	{
		glPushMatrix();
		glRotatef(rotation, 0, 0, 1);
		glBegin(GL_LINES);
			glVertex2f(0, 0);
			glVertex2f(0, length);
		glEnd();
		glBegin(GL_TRIANGLES);
			glVertex2f(length/7.5f, length);
			glVertex2f(-length/7.5f, length);
			glVertex2f(0, length+length/3);
		glEnd();
		glPopMatrix();
	}
	
	public static void drawRectangle(float width, float height)
	{
		
	}
}
