package james.sugden.engine.physics;

import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.engine.maths.MathsUtils;
import james.sugden.engine.maths.Transform;
import james.sugden.file_handling.ResourceManager;
import james.sugden.utils.RenderingUtils;
import james.sugden.utils.Texture;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class CircleCollider extends Collider
{
	/**The radius of the circle*/
	private float radius;
	
	public CircleCollider()
	{
		super();
	}
	
	/**Deep copy constructor*/
	public CircleCollider(CircleCollider c, Transform gameObjectTransform)
	{
		super(c);
		
		this.transform = new Transform(gameObjectTransform);
		this.radius = c.radius;
	}
	
	/**Initialise the rect collider*/
	public void init()
	{
		super.init();
		
		this.typeName = "Circle_Collider";
		
		this.radius = 30;
		this.transform.x = this.transform.y = 0;
	}
	
	/**Rectangle to rectangle collision
	 * Separating axis theorem - to be implemented for rotated rectangles*/
	public boolean collidesWith(RectCollider collider)	//TODO - FINISH rectangle to circle collision detection
	{
		//Circle collides with rectangle if the centre of the circle is inside the rectangle or
		//if the line of the rectanlge pass through the circle
		
		return collider.collidesWith(this);
		
		/*if(collider.collidesWith(transform.x, transform.y))	//TODO - Add line intersection to complete method
			return true;
		else
			return false;*/
	}
	
	/**Rectangle to circle collision*/
	public boolean collidesWith(CircleCollider collider)
	{
		//The distance between the centre's of the circles will be less than or equal to the sum of both their radii if there is an interscetion
		//Uses the greatest transform so the collider is still circular
		if(MathsUtils.modulus(transform.x - collider.transform.x, transform.y - collider.transform.y) <=
				radius * Math.max(transform.scaleX, transform.scaleY) + collider.radius * Math.max(collider.transform.scaleX, collider.transform.scaleY))
			return true;
		else
			return false;
	}
	
	/**Rectangle to line collision*/
	public boolean collidesWith(LineCollider collider)	//TODO - Add in line to circle collision
	{
		return false;
	}
	
	/**Rectangle to path collision*/
	public boolean collidesWith(PathCollider collider)	//TODO - Add in path to circle collision
	{
		return false;
	}
	
	/**Checks whether the point is inside the rect collider*/
	public boolean collidesWith(float x, float y)
	{
		//If the distance from the point to the centre of the circle is less than or equal to the radius then the point is in the circle
		if(MathsUtils.modulus(transform.x - x, transform.y - y) <= radius * Math.max(transform.scaleX, transform.scaleY))
			return true;
		else
			return false;
	}
	
	public void createEditingPanel(JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj)
	{
		JPanel pnlPos = new JPanel();
			GridLayout lytPos = new GridLayout(1, 2);
			pnlPos.setLayout(lytPos);
		SpinnerNumberModel mdlX = new SpinnerNumberModel();
			mdlX.setValue(transform.x);
			JSpinner spnX = new JSpinner(mdlX);
		SpinnerNumberModel mdlY = new SpinnerNumberModel();
			mdlY.setValue(transform.y);
			JSpinner spnY = new JSpinner(mdlY);
		JLabel lblPos = new JLabel("Position (X, Y):");
		pnlPos.add(spnX);
		pnlPos.add(spnY);
		
		JPanel pnlRadius = new JPanel();
			GridLayout lytRadius = new GridLayout(1, 2);
			pnlRadius.setLayout(lytRadius);
		SpinnerNumberModel mdlRadius = new SpinnerNumberModel();
			mdlRadius.setValue(radius);
			JSpinner spnRadius = new JSpinner(mdlRadius);
		JLabel lblRadius = new JLabel("Radius:");
		pnlRadius.add(spnRadius);
		
		JLabel lblIsTrigger = new JLabel("Is Trigger");
		JCheckBox btnIsTrigger = new JCheckBox();
		btnIsTrigger.setSelected(isTrigger);
		
		mdlX.addChangeListener(e ->
		{
			transform.x = mdlX.getNumber().floatValue();
		});
		mdlY.addChangeListener(e ->
		{
			transform.y = mdlY.getNumber().floatValue();
		});
		
		mdlRadius.addChangeListener(e ->
		{
			radius = mdlRadius.getNumber().floatValue();
		});
		
		btnIsTrigger.addChangeListener(e ->
		{
			isTrigger = btnIsTrigger.isSelected();
		});
		
		pnl.add(lblPos);
		pnl.add(pnlPos);
		pnl.add(lblRadius);
		pnl.add(pnlRadius);
		pnl.add(lblIsTrigger);
		pnl.add(btnIsTrigger);
	}
	
	public String[] getEditableFields(File fleProject)
	{
		return new String[]{String.valueOf(isTrigger), String.valueOf(transform.x), String.valueOf(transform.y), String.valueOf(transform.rotation), String.valueOf(radius)};
	}
	
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject)
	{
		isTrigger = Boolean.valueOf(objs[0]);
		transform.x = Float.valueOf(objs[1]);
		transform.y = Float.valueOf(objs[2]);
		transform.rotation = Float.valueOf(objs[3]);
		radius = Float.valueOf(objs[4]);
	}
	
	public void close() {}
	
	public void updateAsGame(GameObject thisGameObject)
	{
		/**Update the positions of the vertices incase the game object moved*/
		transform.x = thisGameObject.getTransform().x;
		transform.y = thisGameObject.getTransform().y;
		transform.scaleX = thisGameObject.getTransform().scaleX;
		transform.scaleY = thisGameObject.getTransform().scaleY;
	}
	
	public void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject)
	{
		/**Update the positions of the vertices incase the game object moved*/
		transform.x = thisGameObject.getTransform().x;
		transform.y = thisGameObject.getTransform().y;
		transform.scaleX = thisGameObject.getTransform().scaleX;
		transform.scaleY = thisGameObject.getTransform().scaleY;
	}
	
	public void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject)
	{
		Texture.unbind();
		glPushMatrix();
		if(isTrigger)
			glColor3f(0, 1, 0);
		else
			glColor3f(1, 0, 0);
		glTranslatef(0, 0, -1);
		float scaleX = transform.scaleX > transform.scaleY ? 1 : transform.scaleY/transform.scaleX;	//Calculate the ratio to scale it by to match the other scale
		float scaleY = transform.scaleY > transform.scaleX ? 1 : transform.scaleX/transform.scaleY;
		glScalef(scaleX, scaleY, 1);		//Scale so that both dimensions are in the higher scale
		RenderingUtils.drawCircle(radius);
		glColor3f(1, 1, 1);
		glPopMatrix();
	}
	
	public float getRadius()
	{
		return radius;
	}
}






/*Vector2f vVec1 = new Vector2f(collider.getTopLeft().x - collider.getTopRight().x, collider.getTopLeft().y - collider.getTopRight().y);
Vector2f vVec2 = new Vector2f(collider.getTopRight().x - collider.getBottomRight().x, collider.getTopRight().y - collider.getBottomRight().y);
Vector2f vVec3 = new Vector2f(collider.getBottomRight().x - collider.getBottomLeft().x, collider.getBottomRight().y - collider.getBottomLeft().y);
Vector2f vVec4 = new Vector2f(collider.getBottomLeft().x - collider.getTopLeft().x, collider.getBottomLeft().y - collider.getTopLeft().y);

Vector2f uVec1 = new Vector2f(collider.getTopLeft().x - transform.x, collider.getTopLeft().y - transform.y);
Vector2f uVec2 = new Vector2f(collider.getBottomRight().x - transform.x, collider.getBottomRight().y - transform.y);

Vector2f proj1 = new Vector2f(Vector2f.dot(uVec1, vVec1), Vector2f.dot(uVec1, vVec1));*/
