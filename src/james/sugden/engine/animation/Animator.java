package james.sugden.engine.animation;

import java.awt.GridBagConstraints;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import james.sugden.engine.Lookup;
import james.sugden.engine.game_object.DrawableComponent;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.engine.rendering.ImageRenderer;
import james.sugden.file_handling.ResourceManager;

public class Animator extends DrawableComponent
{
	/**The first animation which is animated when the scene loads*/
	private int firstAnimation, currentAnimation;
	
	/**Possible Animations*/
	private ArrayList<Animation> animations;
	
	/**Array used when loading to ensure all animation renderer's are loaded first*/
	private String[] objs;
	
	public Animator()
	{
		super();
	}
	
	/**Deep copy constructor*/
	public Animator(Animator a)
	{
		super(a);
		
		this.firstAnimation = a.firstAnimation;
		this.currentAnimation = a.currentAnimation;
		
		this.animations = new ArrayList<>();
		for(Animation animation : a.animations)	//dOESNT CREATE COPY - Posisblty need chaning
		{
			this.animations.add(new Animation(animation));
		}
		
		this.objs = null;
	}
	
	/**Initialise the animator*/
	protected void init()
	{
		typeName = "Animator";
		animations = new ArrayList<>();
		currentAnimation = firstAnimation = 0;
	}
	
	public void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject)
	{
		
	}
	
	public void updateAsGame(GameObject thisGameObject)
	{
		outerloop:
			for(int j = 0; j < animations.get(currentAnimation).connectors.size(); j++)
			{
				Connector connector = animations.get(currentAnimation).connectors.get(j);
				for(int i = 0; i < connector.variables.size(); i++)
				{
					if(connector.types.get(i).equals("INPUT"))
					{
						boolean pressed = Lookup.getInput().isKeyDown(String.valueOf(connector.variables.get(i)));
						if(pressed != Boolean.parseBoolean((String)connector.variableValues.get(i)))
						{
							break;
						}
					}
					if(i == connector.variables.size()-1)
					{
						currentAnimation = animations.indexOf(connector.animation);
						break outerloop;
					}
				}
			}
		animations.get(currentAnimation).animationRenderer.updateAsGame(thisGameObject);
	}
	
	public void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject)
	{
		if(animations.size() > firstAnimation)
		{
			animations.get(firstAnimation).animationRenderer.drawAsEditorFromAnimator(thisGameObject, selectedGameObject);
		}
	}
	
	public void drawAsGame(GameObject thisGameObject)
	{
		if(animations.size() > 0)
		{
			animations.get(currentAnimation).animationRenderer.drawAsGameFromAnimator(thisGameObject);
		}
	}
	
	public void createEditingPanel(final JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj)
	{
		JButton btnEditAnimation = new JButton("Edit Animation");
		JLabel lblEditAnimation = new JLabel("Open Animator in Editor:");
		lblEditAnimation.setToolTipText("Opens the animator component in the animation editor");
		
		btnEditAnimation.addActionListener(e ->
		{
			eventObj.fireEvent(EEventObject.EEventType.OPEN_ANIMATION_EDITOR);
		});
		
		pnl.add(lblEditAnimation);
		pnl.add(btnEditAnimation);
	}
	
	public String[] getEditableFields(File fleProject)
	{
		if(objs == null)
		{
			String[] objsToReturn = new String[animations.size()+1];
			
			String obj1 = String.valueOf(firstAnimation);
			objsToReturn[0] = obj1;
			
			for(int i = 0; i < animations.size(); i++)
			{
				objsToReturn[i+1] = "";
				for(Connector connector : animations.get(i).connectors)
				{
					objsToReturn[i+1] += " " + animations.indexOf(connector.animation) + ";";
					for(int j = 0; j < connector.types.size(); j++)
					{
						objsToReturn[i+1] += connector.types.get(j) + "," + connector.variables.get(j) + "," + connector.variableValues.get(j);
						if(j < connector.types.size()-1)
						{
							objsToReturn[i+1] += ";";
						}
					}
				}
				objsToReturn[i+1] = objsToReturn[i+1].replaceFirst(" ", "");
			}
			
			return objsToReturn;
		}
		else
		{
			return objs;
		}
	}
	
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject)
	{
		firstAnimation = currentAnimation = Integer.valueOf(objs[0]);
		this.objs = objs;
	}
	
	public void close() {}
	
	public final void addAnimation(AnimationRenderer a)
	{
		animations.add(new Animation(a));
	}
	
	public final void removeAnimation(int i)
	{
		animations.remove(i);
	}
	
	public final void removeAnimation(ImageRenderer animation)
	{
		for(Animation a : animations)
		{
			if(a.animationRenderer.equals(animation))
			{
				animations.remove(a);
				break;
			}
		}
	}
	
	public final Animation[] getAnimations()
	{
		return animations.toArray(new Animation[animations.size()]);
	}
	
	public final void loadAllAnimations(List<AnimationRenderer> animations)
	{
		for(int i = 0; i < animations.size(); i++)
		{
			Animation aReqs = new Animation(animations.get(i));
			this.animations.add(aReqs);
		}
		
		for(int i = 1; i < objs.length; i++)
		{
			String obj = (String)objs[i];
			if(obj.equals("null"))
				break;
			else
			{
				String[] strConnectors = obj.split(" ");
				for(String strConnector : strConnectors)
				{
					String[] strConstraints = strConnector.split(";");
					if(strConstraints[0] != null && !strConstraints[0].equals(""))
					{
						Connector connector = new Connector(this.animations.get(Integer.valueOf(strConstraints[0])));
						for(int j = 1; j < strConstraints.length; j++)
						{
							String[] strConstraintInfo = strConstraints[j].split(",");
							connector.addConstraint(strConstraintInfo[0], strConstraintInfo[1], strConstraintInfo[2]);
						}
						this.animations.get(i-1).connectors.add(connector);
					}
				}
			}
		}
		objs = null;
	}
	
	public final void setFirstAnimation(Animation anim)
	{
		currentAnimation = firstAnimation = animations.indexOf(anim);
	}
	
	public final int getFirstAnimation()
	{
		return firstAnimation;
	}
	
	public class Animation
	{
		public AnimationRenderer animationRenderer;
		
		/**Connects this animation to another*/
		private List<Connector> connectors;
		
		/**Deep copy constructor*/
		private Animation(Animation a)
		{
			this.animationRenderer = new AnimationRenderer(a.animationRenderer);
			
			this.connectors = new ArrayList<>();
			for(Connector c : a.connectors)
			{
				this.connectors.add(new Connector(c, this));
			}
		}
		
		private Animation(AnimationRenderer renderer)
		{
			this.animationRenderer = renderer;
			this.connectors = new ArrayList<>();
		}
		
		public final void addConnector(Animation ar)
		{
			for(Connector c : connectors)
			{
				if(c.animation.equals(ar))
					return;
			}
			Connector c = new Connector(ar);
			connectors.add(c);
		}
		
		public final boolean removeConnector(Connector connector)
		{
			return connectors.remove(connector);
		}
		
		public final List<Connector> getConnectors()
		{
			return connectors;
		}
	}
	
	public class Connector
	{
		public Animation animation;
		
		public List<String> types;
		public List<Object> variables;
		public List<Object> variableValues;
		
		private Connector(Animation animation)
		{
			this.animation = animation;
			this.types = new ArrayList<>();
			this.variables = new ArrayList<>();
			this.variableValues = new ArrayList<>();
		}
		
		/**Deep copy constructor*/
		private Connector(Connector c, Animation a)
		{
			this.animation = a;
			this.types = c.types;
			this.variables = c.variables;
			this.variableValues = c.variableValues;
		}
		
		public final void addConstraint(String type, Object variable, Object value)
		{
			types.add(type);
			variables.add(variable);
			variableValues.add(value);
		}
		
		public final void removeContraint(Object variable)
		{
			int i = variables.indexOf(variable);
			variables.remove(i);
			variableValues.remove(i);
			types.remove(i);
		}
		
		public final void setConstraintValue(Object variable, Object value)
		{
			int i = variables.indexOf(variable);
			variableValues.set(i, value);
		}
		
		public final void setConstraintVariable(Object variable, Object value)
		{
			int i = variableValues.indexOf(value);
			variables.set(i, variable);
		}
	}
}
