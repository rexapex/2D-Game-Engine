package james.sugden.engine.game_object;

import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import james.sugden.engine.animation.AnimationRenderer;
import james.sugden.engine.animation.Animator;
import james.sugden.engine.animation.Animator.Animation;
import james.sugden.engine.audio.AudioListener;
import james.sugden.engine.audio.AudioSource;
import james.sugden.engine.camera.FollowCamera;
import james.sugden.engine.lighting.PointLight;
import james.sugden.engine.maths.ETransformOrigin;
import james.sugden.engine.maths.MathsUtils;
import james.sugden.engine.maths.Transform;
import james.sugden.engine.physics.CircleCollider;
import james.sugden.engine.physics.Collider;
import james.sugden.engine.physics.RectCollider;
import james.sugden.engine.rendering.ImageRenderer;
import james.sugden.engine.rendering.ParticleEmitter;
import james.sugden.engine.scripts.Script;
import james.sugden.utils.RenderingUtils;
import james.sugden.utils.Texture;

public class GameObject
{
	/**The name given by the user to identify an instance*/
	private String name;
	
	/**The tag given by the user to identify the object as part of a group of instances*/
	private String tag;
	
	/**If the game object is a prefab, the name of the prefab*/
	private String prefab;
	
	/**The components attached to the game object*/
	private List<Component> components;
	
	/**Game objects nested within this game object*/
	private List<GameObject> gameObjects;
	
	/**Convenience list of untriggerable collider's to make checking collisions faster in game mode*/
	//private List<Collider> untriggerableColliders;
	
	/**Signifies whether the game object should be updated by the client or by the server
	 * Updated by server if true, updated by client if false
	 * Only applies for networked games*/
	private boolean networked;
	
	/**Determines where the object is, how big it is and how it's rotated*/
	private Transform transform;
	private ETransformOrigin transformOrigin;
	/**The transform of the object last update, used so transform can be reverted in case of collisions*/
	private Transform lastTransform;
	
	/**Variables used to for specifying specific depth values*/
	private boolean useDepthValue;
	private float depthValue;
	
	public GameObject(String name, String tag)
	{
		this.name = name;
		this.tag = tag;
		this.networked = false;
		this.transformOrigin = ETransformOrigin.CENTRE;
		this.transform = new Transform(0, 0, 0);
		this.lastTransform = new Transform(0, 0, 0);
		this.components = new ArrayList<>();
		this.useDepthValue = false;
		this.depthValue = 0;
	}
	
	public GameObject()
	{
		this.name = String.valueOf(Math.random());
		this.tag = this.prefab = "";
		this.networked = false;
		this.transformOrigin = ETransformOrigin.CENTRE;
		this.transform = new Transform(0, 0, 0);
		this.lastTransform = new Transform(0, 0, 0);
		this.components = new ArrayList<>();
		this.useDepthValue = false;
		this.depthValue = 0;
	}
	
	/**Copy constructor for making deep copies*/
	public GameObject(GameObject o)
	{
		this.transformOrigin = o.transformOrigin;
		this.transform = new Transform(o.transform);
		this.lastTransform = new Transform(o.transform);
		this.networked = o.networked;
		this.name = o.name;
		this.tag = o.tag;
		
		this.gameObjects = new ArrayList<>();
		this.components = new ArrayList<Component>();
		for(Component component : o.components)
		{
			if(component instanceof Animator)
			{
				components.add(new Animator((Animator)component));
				for(Animation animation : ((Animator)component).getAnimations())
				{
					this.components.add(animation.animationRenderer);
				}
			}
			else if(component instanceof RectCollider)
				components.add(new RectCollider((RectCollider)component, this.transform));
			else if(component instanceof CircleCollider)
				components.add(new CircleCollider((CircleCollider)component, this.transform));
			else if(component instanceof ImageRenderer && !(component instanceof AnimationRenderer))		//AnimationRenderer inherits from ImageRenderer so need to extra careful
				components.add(new ImageRenderer((ImageRenderer)component));
			else if(component instanceof Script)
				components.add(new Script((Script)component));
			else if(component instanceof AudioSource)
				components.add((AudioSource)component);
			else if(component instanceof ParticleEmitter)
				components.add((ParticleEmitter)component);
			else if(component instanceof PointLight)
				components.add(new PointLight((PointLight)component));
			else if(component instanceof FollowCamera)
				components.add(component);						//Doesn't use copy constructor - probably need to change due to values changing in game
		}
		
		this.useDepthValue = o.useDepthValue;
		this.depthValue = o.depthValue;
	}
	
	/**Initialise the game object in edit mode*/
	public final void initAsEditor()
	{
		
	}
	
	/**Initialise the game object in game mode*/
	public final void initAsGame()
	{
		
	}
	
	/**Sets up components after being loaded from a file*/
	public void setupComponents()
	{
		ArrayList<AnimationRenderer> animations = new ArrayList<>();
		Animator animator = null;
		for(Component c : components)
		{
			if(c instanceof Animator)
				animator = (Animator)c;
			else if(c instanceof AnimationRenderer)
				animations.add((AnimationRenderer)c);
		}
		if(animator != null)
		{
			animator.loadAllAnimations(animations);
		}
	}
	
	
	/**Reverts to the last translation based on the information of the collision (which sides collided)*/
	public final void revertTranslation(int info)
	{
		transform.copy(lastTransform);
	}
	
	/**Update the game object in game mode*/
	public final void updateAsGame()
	{
		lastTransform.copy(transform);
		
		for(Component c : components)
		{
			if(c instanceof UpdatableComponent)	//Update all the updatable components belonging to the game object
			{
				((UpdatableComponent)c).updateAsGame(this);
			}
		}
	}
	
	/**Returns true if the game object was selected*/
	public final boolean updateAsEditor(GameObject gameObjectSelected, int mouseWorldX, int mouseWorldY)
	{
		synchronized(components)
		{
			for(Component c : components)	//Update components of this game object
			{
				c.updateAsEditor(this, gameObjectSelected);
			}
		}
		
		//Allows user to click in the circle of a game object to select it in the editor
		if(Mouse.isButtonDown(1) && MathsUtils.modulus(transform.x - mouseWorldX, transform.y - mouseWorldY) <= 20)
		{
			return true;
		} else
		{
			return false;
		}
	}
	
	/**Draws the game object in game mode*/
	public final void drawAsGame()
	{
		glPushMatrix();
		glRotatef(transform.rotation, 0, 0, 1);
		glTranslatef(transform.x, transform.y, 0);
		glScalef(transform.scaleX, transform.scaleY, 1);
		for(Component c : components)
		{
			if(c instanceof DrawableComponent)
			{
				((DrawableComponent)c).drawAsGame(this);
			}
			//c.drawAsEditor(this, null);
		}
		glPopMatrix();
	}
	
	/**Draws the game object in edit mode*/
	public final void drawAsEditor(GameObject selectedGameObject)
	{
		glPushMatrix();
		glTranslatef(transform.x, transform.y, 0);
		glRotatef(transform.rotation, 0, 0, 1);
		glScalef(transform.scaleX, transform.scaleY, 1);
		synchronized(components)
		{
			for(Component c : components)
			{
				c.drawAsEditor(this, selectedGameObject);
			}
		}
		glPopMatrix();
		
		//Draw circle selection tool
		glPushMatrix();
		Texture.unbind();
		glTranslatef(transform.x, transform.y, -1);
		//glScalef(1, 1, 1);
		glColor3f(0.6f, 0.5f, 0);
		RenderingUtils.drawCircle(20);
		if(this.equals(selectedGameObject))
		{
			glColor3f(1, 0, 0);
			RenderingUtils.drawArrow(30, 0);
			glColor3f(0, 0, 1);
			RenderingUtils.drawArrow(30, -90);
		}
		glColor3f(1, 1, 1);
		
		glPopMatrix();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**Returns the component with the specified name*/
	public final Component getComponentWithName(String name)
	{
		for(Component c : components)
		{
			//System.out.println(name+"----"+c.getIdentificationName());
			if(c.getIdentificationName().equals(name))
				return c;
		}
		return null;
	}
	
	/**Returns all the rectangle collider's with the specified name*/
	public final Collider getColliderWithName(String name)
	{
		for(Component c : components)
		{
			//System.out.println(name + " " + c.getIdentificationName());
			if(c instanceof Collider && c.getIdentificationName().equals(name))
				return (Collider)c;
		}
		return null;
	}
	
	/**Returns the audio source with the specified name*/
	public final AudioSource getAudioSourceWithName(String name)
	{
		for(Component c : components)
		{
			//System.out.println(name + " " + c.getIdentificationName());
			if(c instanceof AudioSource && c.getIdentificationName().equals(name))
				return (AudioSource)c;
		}
		return null;
	}
	
	/**Returns the audio listener with the specified name*/
	public final AudioListener getAudioListenerWithName(String name)
	{
		for(Component c : components)
		{
			//System.out.println(name + " " + c.getIdentificationName());
			if(c instanceof AudioListener && c.getIdentificationName().equals(name))
				return (AudioListener)c;
		}
		return null;
	}
	
	/**Returns all the image renderer's with the specified name*/
	public final ImageRenderer getImageRendererWithName(String name)
	{
		for(Component c : components)
		{
			//System.out.println(name + " " + c.getIdentificationName());
			if(c instanceof ImageRenderer && c.getIdentificationName().equals(name))
				return (ImageRenderer)c;
		}
		return null;
	}
	
	/**Returns all the animation renderer's with the specified name*/
	public final AnimationRenderer getAnimationRendererWithName(String name)
	{
		for(Component c : components)
		{
			//System.out.println(name + " " + c.getIdentificationName());
			if(c instanceof AnimationRenderer && c.getIdentificationName().equals(name))
				return (AnimationRenderer)c;
		}
		return null;
	}
	
	/**Returns all the particle emitter with the specified name*/
	public final ParticleEmitter getParticleEmitterWithName(String name)
	{
		for(Component c : components)
		{
			//System.out.println(name + " " + c.getIdentificationName());
			if(c instanceof ParticleEmitter && c.getIdentificationName().equals(name))
				return (ParticleEmitter)c;
		}
		return null;
	}
	
	
	
	
	
	
	
	
	
	/**Returns a list of this game objects components*/
	public final List<Component> getComponents()
	{
		return components;
	}
	
	/**Adds the given component to the list of components*/
	public final void addComponent(Component c)
	{
		synchronized(components)
		{
			components.add(c);
		}
	}
	
	/**Removes the given component from the list of components*/
	public final void removeComponent(Component c)
	{
		synchronized(components)
		{
			components.remove(c);
			c.close();
		}
	}
	
	/**Returns a list of the nested game objects*/
	public final List<GameObject> getGameObjects()
	{
		return gameObjects;
	}
	
	/**Adds the given game object to the list of nested game objects*/
	public final void addNestedGameObject(GameObject o)
	{
		synchronized(gameObjects)
		{
			gameObjects.add(o);
		}
	}
	
	/**Removes the given game object from the list of nested game objects*/
	public final void removeNestedGameObject(GameObject o)
	{
		synchronized(gameObjects)
		{
			gameObjects.remove(o);
			o.close();
		}
	}
	
	/**Returns the name of the instance*/
	public final String getName()
	{
		return name;
	}
	
	/**Returns the tag assigned to the instance*/
	public final String getTag()
	{
		return tag;
	}
	
	/**Returns the prefab of the game object*/
	public final String getPrefab()
	{
		return prefab;
	}
	
	/**Sets the name of the instance*/
	public final void setName(String name)
	{
		this.name = name;
	}
	
	/**Sets the tag assigned to the instance*/
	public final void setTag(String tag)
	{
		this.tag = tag;
	}
	
	/**Sets the prefab of the game object*/
	public final void setPrefab(String prefab)
	{
		this.prefab = prefab;
	}
	
	/**Sets the game object to be updated by the server or client*/
	public final void setNetworked(boolean networked)
	{
		this.networked = networked;
	}
	
	/**Returns whether the object is networked or not*/
	public final boolean getNetworked()
	{
		return networked;
	}
	
	/**Returns the transform object for the game object*/
	public final Transform getTransform()
	{
		return transform;
	}
	
	public final void setUseDepthValue(boolean useDepthValue)
	{
		this.useDepthValue = useDepthValue;
	}
	
	public final void setDepthValue(float depthValue)
	{
		this.depthValue = depthValue;
	}
	
	public final boolean getUseDepthValue()
	{
		return useDepthValue;
	}
	
	public final float getDepthValue()
	{
		return depthValue;
	}
	
	/**Handles the closing of the game object*/
	public final void close()
	{
		for(Component c : components)
		{
			c.close();
		}
	//	for(GameObject o : gameObjects)
	//	{
	//		o.close();
	//	}
	}
}
