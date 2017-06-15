package james.sugden.engine.maths;

import james.sugden.engine.Time;

import org.lwjgl.util.vector.Vector2f;

public class MathsUtils
{
	/**Calculates the modulus of a and b and returns the answer as a float*/
	public final static float modulus(float a, float b)
	{
		return (float)Math.sqrt(a * a + b * b);
	}
	
	/**Calculates the bearing between the line xy and the vertical*/
	public final static float bearing(float x, float y)
	{
		double angle = Math.toDegrees(Math.atan(Math.abs(x)/Math.abs(y)));
		
		if(x >= 0)
		{
			if(y >= 0)
				return (float)angle;
			else
				return 180 - (float)angle;
		} else if(x < 0)
		{
			if(y >= 0)
				return 360 - (float)angle;
			else
				return 180 + (float)angle;
		} else
		{
			return 0;
		}
	}
	
	/**Linear interpolates between the two points using the value val and returns the result as a float*/
	public final static float lerp(float start, float end, float val)
	{
		return start + val * (end - start);
	}
	
	/**Linear interpolates between the two points and returns the distance to travel this update*/
	public final static Vector2f lerp(float startX, float startY, float endX, float endY)
	{
		float xDist = (endX - startX) * Time.getDelta();
		float yDist = (endY - startY) * Time.getDelta();
		
		return new Vector2f(xDist, yDist);
	}
}
