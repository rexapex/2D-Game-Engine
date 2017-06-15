package james.sugden.engine;

import static org.lwjgl.openal.AL10.AL_ORIENTATION;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_VELOCITY;
import static org.lwjgl.openal.AL10.alListener3f;
import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_GREATER;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glAlphaFunc;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClearDepth;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class Setup
{
	/**Create a game and return it to the calling class*/
	/*public final static Game initAsGame(Scene scene, InputHandler input)
	{
		Game game = new Game();
		
		game.init(scene, input);
		
		return game;
	}*/
	
	/**Create an opengl context and initialise opengl's othograthic projection*/
	public final static void initGLProjection()
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		//GL11.glOrtho(0, Display.getWidth(), 0, Display.getHeight(), 1, -1);
		GL11.glOrtho(-Display.getWidth()/2, Display.getWidth()/2, -Display.getHeight()/2, Display.getHeight()/2, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
	
	/**Create an opengl context and initialise opengl's othograthic projection
	 * Uses the values for the screen width and height given*/
	public final static void initGLProjection(int width, int height)
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(-width/2, width/2, -height/2, height/2, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		System.out.println("New Viewport Size: " + width + ", " + height);
	}
	
	/**Initialise parts of opengl for rendering*/
	public final static void initGLRendering()
	{
		glDisable(GL_LIGHTING);
		glDisable(GL_LIGHT0);
		glDisable(GL_CULL_FACE);
		
		/**Z Buffer*/
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		glClearDepth(1);
		/**Alpha Testing*/
		glAlphaFunc(GL_GREATER, 0.5f);
		glEnable(GL_ALPHA_TEST);
		
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_COLOR);
		
		/**Enable transparency*/
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_BLEND);
		
		/**Enable stencil test*/
		glEnable(GL_STENCIL_TEST);
	}
	
	/**Initialise parts of openal for audio*/
	public static final void initALAudio()
	{
		try {
			AL.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		alListener3f(AL_POSITION, 0, 0, 0);
		alListener3f(AL_VELOCITY, 0, 0, 0);
		alListener3f(AL_ORIENTATION, 0, 0, -1);
	}
}
