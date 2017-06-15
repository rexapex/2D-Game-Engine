package james.sugden.engine;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import james.sugden.engine.audio.AudioSource;
import james.sugden.engine.camera.Camera;
import james.sugden.engine.game_object.Component;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.lighting.PointLight;
import james.sugden.engine.lighting.SpotLight;
import james.sugden.engine.physics.Collider;
import james.sugden.engine.physics.UntriggerableCollider;
import james.sugden.engine.rendering.ParticleEmitter;
import james.sugden.engine.scripts.Script;
import james.sugden.engine.shaders.Shader;
import james.sugden.engine.shaders.ShaderLoader;
import james.sugden.utils.Texture;
import james.sugden.utils.TextureLoader;

public final class Scene
{
	/**The name of this scene*/
	private String name = "";
	
	/**List of all the non-nested game objects in the scene*/
	private List<GameObject> gameObjects;
	
	/**The camera which has it's transformation applied to the screen*/
	private Camera camera;
	
	/**Convenience list used so collisions can be checked quickly*/
	private List<UntriggerableCollider> untriggerableColliders;
	/**2D jagged array to store the outcomes of collisions
	 * 0 = no collision, 1 = left, 2 = top, 4 = right, 8 = bottom
	 * left + top = 3, left + bottom = 9, right + top = 6, right + bottom = 12*/
	private byte[][] collisionOutcomes;	//Note - Byte array more optimised than int array as long as little arithmetic is carried out
	
	/**Lighting shader - default per pixel lighting shader included with game engine*/
	private Shader lightingShader;
	/**A list of all the dynamic lights in the scene*/
	private List<Map.Entry<PointLight, GameObject>> pointLights;
	private List<Map.Entry<SpotLight, GameObject>> spotLights;
	/**The id of the frame buffer object - Used to render lights to the screen*/
	private int fboID;
	/**The lightmap texture for the scene - Bound to the frame buffer object*/
	private Texture texLightMap;
	
	public Scene()
	{
		this.gameObjects = new ArrayList<>();
	}
	
	/**Deep copy constructor*/
	public Scene(final Scene scene)
	{
		this.name = scene.name;
		this.camera = scene.camera;
		this.gameObjects = new ArrayList<>();
		
		List<GameObject> gameObjectsLoading = new ArrayList<>();
		
		for(GameObject object : scene.gameObjects)
		{
			gameObjectsLoading.add(new GameObject(object));
		}
		
		this.gameObjects = gameObjectsLoading;
		this.lightingShader = scene.lightingShader;
	}
	
	
	
	
	
	
	/**Initialise the scene as the editor*/
	public final void initAsEditor()
	{
		for(GameObject o : gameObjects)
		{
			o.initAsEditor();
		}
	}
	
	/**Initialise the scene as the game*/
	public final void initAsGame()
	{
		this.lightingShader = ShaderLoader.loadShader("default_res/shaders/lighting.frag", "default_res/shaders/lighting.vert");	//Load default lighting shader
		this.lightingShader.enable();
		glUniform3f(glGetUniformLocation(this.lightingShader.getProgram(), "ambientColour"), 1.0f, 1.0f, 1.0f);
		glUniform1f(glGetUniformLocation(this.lightingShader.getProgram(), "ambientIntensity"), 0.45f);
		Shader.disable();
		
		this.pointLights = new ArrayList<>();
		this.spotLights = new ArrayList<>();
		this.untriggerableColliders = new ArrayList<>();
		
		for(GameObject o : gameObjects)	//Initialise all game objects - add lights and un triggerable colliders to the lists
		{
			o.initAsGame();
			for(Component component : o.getComponents())
			{
				//System.out.println(o.getName() + ": " + component.getIdentificationName());
				if(component instanceof Collider)	//Give the scene all of the untriggerable colliders in the scene
				{
					Collider collider = (Collider)component;
					if(!collider.isTrigger())
					{
						untriggerableColliders.add(new UntriggerableCollider(o, collider));
					}
				} else if(component instanceof PointLight)	//Give the scene all of the point lights in the scene
				{
					PointLight light = (PointLight)component;
					light.setPos(o.getTransform().x, o.getTransform().y);
					pointLights.add(new SimpleEntry<PointLight, GameObject>(light, o));
				} else if(component instanceof SpotLight)	//Give the scene all of the spot lights in the scene
				{
					SpotLight light = (SpotLight)component;
					light.setPos(o.getTransform().x, o.getTransform().y);
					spotLights.add(new SimpleEntry<SpotLight, GameObject>(light, o));
				} else if(component instanceof AudioSource)	//Begin playing audio sources which should be played at the start of the scene
				{
					if(((AudioSource)component).playAtStart())
					{
						((AudioSource)component).play();
					}
				} else if(component instanceof ParticleEmitter)	//Create the vbo and buffer for the particle emitter before the game starts
				{
					((ParticleEmitter)component).createVBO();
				} else if(component instanceof Script)
				{
					((Script)component).initEngineScript(o);
				}
			}
		}
		
		if(untriggerableColliders.size() > 0)
		{
			int arraySize = untriggerableColliders.size() - 1;	//The numbers of arrays in the collision outcomes array
			//System.out.println("Array size " + arraySize);
			
			this.collisionOutcomes = new byte[arraySize][];	//Jagged array has entry for each collider - possibly can be optimised so only for object with colliders
			for(int i = 0; i < collisionOutcomes.length; i++)
			{
				collisionOutcomes[i] = new byte[collisionOutcomes.length - i];	//Arrays decrease in size to conserve space - first array doesn't need spot for it's own collider so -1
				//System.out.println("Length " + (collisionOutcomes.length - i));
			}
		}
		
		//If frame buffer objects are supported then create a frame buffer object used in the lighting process
		if(GLContext.getCapabilities().GL_EXT_framebuffer_object)
		{
			IntBuffer buffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();	//Create the int buffer	4 bytes = 1 int
			EXTFramebufferObject.glGenFramebuffersEXT();	//Generate an fbo id and place it in the buffer
			fboID = buffer.get();							//Retrieve the fbo id from the buffer
			
			texLightMap = TextureLoader.genTexture(Display.getWidth(), Display.getHeight());			//Generate an empty texture the size of the display
			EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboID);	//Bind the frame buffer object
			EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,		//Bind the light map tex to the fbo
					EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, texLightMap.getID(), 0);
			
			int framebuffer = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT); 
			switch(framebuffer)	//Perform a frame buffer object completeness check
			{
			    case EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT:
			        break;
			    case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
			        throw new RuntimeException("FrameBuffer: " + fboID
			                + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception");
			    case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
			        throw new RuntimeException("FrameBuffer: " + fboID
			                + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception");
			    case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
			        throw new RuntimeException("FrameBuffer: " + fboID
			                + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception");
			    case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
			        throw new RuntimeException("FrameBuffer: " + fboID
			                + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception");
			    case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
			        throw new RuntimeException("FrameBuffer: " + fboID
			                + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception");
			    case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
			        throw new RuntimeException("FrameBuffer: " + fboID
			                + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception");
			    default:
			        throw new RuntimeException("Unexpected reply from glCheckFramebufferStatusEXT: " + framebuffer);
			}
		}
	}
	
	
	
	
	
	
	/**Used by SceneEditor if lighting has been enabled*/
	public final void setupLights()
	{
		this.lightingShader = ShaderLoader.loadShader("default_res/shaders/lighting.frag", "default_res/shaders/lighting.vert");	//Load default lighting shader
		this.lightingShader.enable();
		glUniform3f(glGetUniformLocation(this.lightingShader.getProgram(), "ambientColour"), 1.0f, 1.0f, 1.0f);
		glUniform1f(glGetUniformLocation(this.lightingShader.getProgram(), "ambientIntensity"), 0.45f);
		Shader.disable();
		
		this.pointLights = new ArrayList<>();
		this.spotLights = new ArrayList<>();
		for(GameObject o : gameObjects)
		{
			for(Component c : o.getComponents())
			{
				if(c instanceof PointLight)	//Give the scene all of the lights in the scene
				{
					PointLight light = (PointLight)c;
					light.setPos(o.getTransform().x, o.getTransform().y);
					pointLights.add(new SimpleEntry<PointLight, GameObject>(light, o));
				}
				else if(c instanceof SpotLight)	//Give the scene all of the lights in the scene
				{
					SpotLight light = (SpotLight)c;
					light.setPos(o.getTransform().x, o.getTransform().y);
					spotLights.add(new SimpleEntry<SpotLight, GameObject>(light, o));
				}
			}
		}
	}
	
	/**Test whether lighting has been setup*/
	public final boolean isLightingSetup()
	{
		return !(pointLights == null || spotLights == null);
	}
	
	
	
	
	
	
	/**Updates the scene in game mode*/
	public final void updateAsGame()
	{
		GL11.glLoadIdentity();
		if(camera != null)
			camera.applyTransform();
		
		for(int i = 0; i < gameObjects.size(); i++)
		{
			gameObjects.get(i).updateAsGame();
		}
		
		//Collision detection
		if(collisionOutcomes != null)
		{
			for(int i = 0; i < collisionOutcomes.length; i++)	//Loop over each array in jagged array
			{
				for(int j = 0; j < collisionOutcomes[i].length; j++)	//Loop over each element of the current array
				{
					Collider collider1 = untriggerableColliders.get(i).collider;
					Collider collider2 = untriggerableColliders.get(i + j + 1).collider;	//Plus one so doesn't test for collision with itself
				//	System.out.println(collider1.getIdentificationName() + " & " + collider2.getIdentificationName());
					if(collider1.collidesWith(collider2))
					{
						untriggerableColliders.get(i).gameObject.revertTranslation(0);
						untriggerableColliders.get(i + j + 1).gameObject.revertTranslation(0);
						//System.out.println("Collision detected between " + collider1.getIdentificationName() + " & " + collider2.getIdentificationName());
					}
				}
			}
		}
	}
	
	/**Updates the scene in edit mode
	 * Returns the game object selected by clicking*/
	public final GameObject updateAsEditor(GameObject gameObjectSelected, int mouseWorldX, int mouseWorldY)
	{
		GameObject newGameObjectSelected = gameObjectSelected;
		if(camera != null)
			camera.applyTransform();
		
		synchronized(gameObjects)	//Synchronise so add and remove methods don't cause concurrent modification from editor
		{
			for(GameObject gameObject : gameObjects)
			{
				if(gameObject.updateAsEditor(gameObjectSelected, mouseWorldX, mouseWorldY))
					newGameObjectSelected = gameObject;
			}
		}
		
		return newGameObjectSelected;
	}
	
	
	
	
	
	/**Draws the scene in game mode*/
	public final void drawAsGame()
	{
		doLighting();
		
		for(int i = 0; i < gameObjects.size(); i++)	//Draw all game objects in the scene in game mode
		{
			gameObjects.get(i).drawAsGame();
		}
		
		Shader.disable();	//Disable the lighting shader
	}
	
	/**Draws the scene in edit mode*/
	public final void drawAsEditor(GameObject selectedGameObject, boolean doLighting)
	{
		if(doLighting)
			doLighting();
		
		synchronized(gameObjects)	//Synchronise so add and remove methods don't cause concurrent modification from editor
		{
			for(GameObject o : gameObjects)
			{
				o.drawAsEditor(selectedGameObject);
			}
		}
		
		Shader.disable();
	}
	
	private final void doLighting()
	{
		if((pointLights != null && spotLights != null) && (pointLights.size() > 0 || spotLights.size() > 0))	//Lighting block
		{
			//If there are no lights, scene is in full bright mode
			lightingShader.enable();	//Enable the lighting shader
			
			int numPointLights = pointLights.size();
			if(numPointLights > 64)			//Max number of lights as set by shader
				numPointLights = 64;
			int numSpotLights = spotLights.size();
			if(numSpotLights > 64)			//Max number of lights as set by shader
				numSpotLights = 64;
			
			glUniform1i(glGetUniformLocation(lightingShader.getProgram(), "numPointLights"), numPointLights);
			glUniform1i(glGetUniformLocation(lightingShader.getProgram(), "numSpotLights"), numSpotLights);
			
			for(int i = 0; i < numPointLights; i++)
			{
				PointLight light = pointLights.get(i).getKey();
				GameObject object = pointLights.get(i).getValue();
				
				glUniform2f(glGetUniformLocation(lightingShader.getProgram(), "pointLights["+i+"].pos"), light.getX(), light.getY());
				glUniform1f(glGetUniformLocation(lightingShader.getProgram(), "pointLights["+i+"].depth"), object.getDepthValue());
				glUniform3f(glGetUniformLocation(lightingShader.getProgram(), "pointLights["+i+"].ambientColour"), light.getRed(), light.getGreen(), light.getBlue());
				glUniform3f(glGetUniformLocation(lightingShader.getProgram(), "pointLights["+i+"].diffuseColour"), light.getRed(), light.getGreen(), light.getBlue());
				glUniform1f(glGetUniformLocation(lightingShader.getProgram(), "pointLights["+i+"].intensity"), light.getIntensity());
				glUniform1f(glGetUniformLocation(lightingShader.getProgram(), "pointLights["+i+"].constant"), light.getAttenConstant());
				glUniform1f(glGetUniformLocation(lightingShader.getProgram(), "pointLights["+i+"].linear"), light.getAttenLinear());
				glUniform1f(glGetUniformLocation(lightingShader.getProgram(), "pointLights["+i+"].quadratic"), light.getAttenQuadratic());
			}
			
			for(int i = 0; i < numSpotLights; i++)
			{
				SpotLight light = spotLights.get(i).getKey();
				GameObject object = spotLights.get(i).getValue();
				
				glUniform2f(glGetUniformLocation(lightingShader.getProgram(), "spotLights["+i+"].pos"), light.getX(), light.getY());
				glUniform2f(glGetUniformLocation(lightingShader.getProgram(), "spotLights["+i+"].dir"), light.getDirX(), light.getDirY());
				glUniform1f(glGetUniformLocation(lightingShader.getProgram(), "spotLights["+i+"].depth"), object.getDepthValue());
				glUniform3f(glGetUniformLocation(lightingShader.getProgram(), "spotLights["+i+"].ambientColour"), light.getRed(), light.getGreen(), light.getBlue());
				glUniform3f(glGetUniformLocation(lightingShader.getProgram(), "spotLights["+i+"].diffuseColour"), light.getRed(), light.getGreen(), light.getBlue());
				glUniform1f(glGetUniformLocation(lightingShader.getProgram(), "spotLights["+i+"].intensity"), light.getIntensity());
				glUniform1f(glGetUniformLocation(lightingShader.getProgram(), "spotLights["+i+"].constant"), light.getAttenConstant());
				glUniform1f(glGetUniformLocation(lightingShader.getProgram(), "spotLights["+i+"].linear"), light.getAttenLinear());
				glUniform1f(glGetUniformLocation(lightingShader.getProgram(), "spotLights["+i+"].quadratic"), light.getAttenQuadratic());
				glUniform1f(glGetUniformLocation(lightingShader.getProgram(), "spotLights["+i+"].cosInnerCutoff"), light.getCosInnerCutoff());
				glUniform1f(glGetUniformLocation(lightingShader.getProgram(), "spotLights["+i+"].cosOuterCutoff"), light.getCosOuterCutoff());
			}
			
			glUniform1i(glGetUniformLocation(lightingShader.getProgram(), "sampler"), 0);
		}
	}
	
	/**Returns the list of game objects in this scene*/
	public final List<GameObject> getGameObjects()
	{
		synchronized(gameObjects)
		{
			return gameObjects;
		}
	}
	
	/**Adds the given game object the scene's game object list*/
	public final void addGameObject(GameObject gameObject)
	{
		synchronized(gameObjects)
		{
			gameObjects.add(gameObject);
		}
	}
	
	/**Removes the given game object from the scene's game object list*/
	public final void removeGameObject(GameObject gameObject)
	{
		synchronized(gameObjects)
		{
			System.out.println("Removed " + gameObjects.remove(gameObject));
			gameObject.close();
		}
	}
	
	public final void setCamera(Camera camera)
	{
		this.camera = camera;
	}
	
	public final Camera getCamera()
	{
		return camera;
	}
	
	public final String getName()
	{
		return name;
	}
	
	public final void setName(String name)
	{
		this.name = name;
	}
	
	/**Returns the list of untriggerable colliders in game mode*/
	/*public final List<UntriggerableCollider> getUntriggerableColliders()
	{
		return untriggerableColliders;
	}*/
	
	/**Handles the closing of the scene*/
	public final void close()
	{
		for(GameObject o : gameObjects)
		{
			o.close();
		}
	}
}








//if(lights.size() > 0)
//{
	/*glColorMask(false, false, false, false);
	glDepthMask(false);
	glStencilFunc(GL_NEVER, 1, 0xFF);
	glStencilOp(GL_REPLACE, GL_KEEP, GL_KEEP);
	
	glStencilMask(0xFF);
	glClear(GL_STENCIL_BUFFER_BIT);
	for(SpotLight light : lights)
	{
		light.drawArea();
	}
	
	glColorMask(true, true, true, true);
	glDepthMask(true);
	glStencilMask(0x00);
	glStencilFunc(GL_EQUAL, 1, 0xFF);*/
//}