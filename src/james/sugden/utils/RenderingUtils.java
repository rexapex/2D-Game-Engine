package james.sugden.utils;

import static org.lwjgl.opengl.GL11.*;

public class RenderingUtils
{
	public final static void drawCircle(float radius)
	{
	   glBegin(GL_LINE_LOOP);
	   
	   for (int i = 0; i < 360; i++)
	   {
	      glVertex2f((float)Math.cos(Math.toRadians(i))*radius, (float)Math.sin(Math.toRadians(i))*radius);
	   }
	   
	   glEnd();
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
}
