package james.sugden.utils;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW_MATRIX;
import static org.lwjgl.opengl.GL11.GL_PROJECTION_MATRIX;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glGetFloat;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.util.glu.GLU.gluUnProject;

import java.awt.Image;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector4f;

public class Utils
{
	public final static FloatBuffer toFloatBuffer(float[] values)
	{
		FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
		buffer.put(values);
		buffer.flip();
		return buffer;
	}
	
	public final static IntBuffer toIntBuffer(int[] values)
	{
		IntBuffer buffer = BufferUtils.createIntBuffer(values.length);
		buffer.put(values);
		buffer.flip();
		return buffer;
	}
	
	public final static FloatBuffer reserveData(int size)
	{
		return BufferUtils.createFloatBuffer(size);
	}
	
	public final static Vector4f calcWorldCoords(int x, int y, int dx, int dy)
	{
		FloatBuffer model = BufferUtils.createFloatBuffer(16);
		FloatBuffer projection = BufferUtils.createFloatBuffer(16);
		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		
		glGetFloat(GL_MODELVIEW_MATRIX, model);
		glGetFloat(GL_PROJECTION_MATRIX, projection);
		glGetInteger(GL_VIEWPORT, viewport);
		
		FloatBuffer transformedPoint = BufferUtils.createFloatBuffer(3);
		FloatBuffer transformedDynamicPoint = BufferUtils.createFloatBuffer(3);
		
		gluUnProject(x, Display.getHeight()-y, 0, model, projection, viewport, transformedPoint);
		gluUnProject(dx, dy, 0, model, projection, viewport, transformedDynamicPoint);
		
		return new Vector4f(transformedPoint.get(0),  transformedPoint.get(1), transformedDynamicPoint.get(0), transformedDynamicPoint.get(1));
	}
	
	/**Scales the image to fit a square area while maintaining the aspect ratio*/
	public static final Image scaleImg(float newSize, Image img)
	{
		float newWidth, newHeight;
		if(img.getWidth(null) > img.getHeight(null))
		{
			newWidth = newSize;
			newHeight = newSize * img.getHeight(null) / img.getWidth(null);
		} else
		{
			newHeight = newSize;
			newWidth = newSize * img.getWidth(null) / img.getHeight(null);
		}
		
		return img.getScaledInstance((int)newWidth, (int)newHeight, Image.SCALE_SMOOTH);
	}

	

	/**Utility classes useful for callbacks*/
	public static class IntegerHolder
	{
		public int i;
		
		public IntegerHolder()
		{
			i = 0;
		}
		
		public IntegerHolder(int i)
		{
			this.i = i;
		}
	}
	
	public static class BooleanHolder
	{
		public boolean b;
		
		public BooleanHolder()
		{
			b = false;
		}
		
		public BooleanHolder(boolean b)
		{
			this.b = b;
		}
	}
	
	public static class FloatHolder
	{
		public float f;
		
		public FloatHolder()
		{
			this.f = 0.0f;
		}
		
		public FloatHolder(float f)
		{
			this.f = f;
		}
	}
	
	public static class DoubleHolder
	{
		public double d;
		
		public DoubleHolder()
		{
			this.d = 0.0;
		}
		
		public DoubleHolder(double d)
		{
			this.d = d;
		}
	}
}
