package james.sugden.engine.physics;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.engine.maths.MathsUtils;
import james.sugden.engine.maths.Transform;
import james.sugden.file_handling.ResourceManager;
import james.sugden.utils.Texture;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.lwjgl.util.vector.Vector2f;

public class RectCollider extends Collider
{
	/**The dimensions of the rectangle*/
	private float width, height;
	
	/**Positions of the vertices of the rectangle*/
	private Vector2f topLeft, topRight, bottomLeft, bottomRight;
	
	public RectCollider()
	{
		super();
	}
	
	/**Deep copy constructor*/
	public RectCollider(RectCollider c, Transform gameObjectTransform)
	{
		super(c);
		
		width = c.width;
		height = c.height;
		
		/**Initialise the vertices*/
		topLeft = new Vector2f();
		topRight = new Vector2f();
		bottomLeft = new Vector2f();
		bottomRight = new Vector2f();
		
		/**Set the positions of the vertices*/
		topLeft.x = bottomLeft.x = gameObjectTransform.x + transform.x;
		topLeft.y = topRight.y = gameObjectTransform.y + transform.y;
		topRight.x = bottomRight.x = topLeft.x + width;
		bottomLeft.y = bottomRight.y = topLeft.y + height;
	}
	
	/**Initialise the rect collider*/
	public void init()
	{
		super.init();
		
		this.typeName = "Rect_Collider";
		
		topLeft = new Vector2f(0, 0);
		topRight = new Vector2f(0, 0);
		bottomLeft = new Vector2f(0, 0);
		bottomRight = new Vector2f(0, 0);
		width = height = 64;
	}
	
	/**Rectangle to rectangle collision
	 * Separating axis theorem - to be implemented for rotated rectangles*/
	public boolean collidesWith(RectCollider collider)	//Works for unrotated rectangle
	{
		/**Collision detection for two unrotated rectangles*/
		if((topLeft.y <= collider.bottomLeft.y && bottomLeft.y >= collider.topLeft.y &&
				bottomLeft.x <= collider.bottomRight.x && bottomRight.x >= collider.bottomLeft.x) ||
			(collider.topLeft.y < bottomLeft.y && collider.bottomLeft.y > topLeft.y &&
					collider.bottomLeft.x < bottomRight.x && collider.bottomRight.x > bottomLeft.x))
			return true;
		else
			return false;
	}
	
	/**Rectangle to circle collision*/
	public boolean collidesWith(CircleCollider collider)
	{
		//x' = cos(theta) * (cx - originx) - sin(theta) * (cy - originy) + originx
		//y' = sin(theta) * (cx - originx) - cos(theta) * (cy - originy) + originy
		//float newCircleX = (float)(Math.cos(transform.rotation) * (collider.transform.x - (topLeft.x + width/2)) - Math.sin(transform.rotation)
		//		* (collider.transform.y - (topLeft.y - height/2)) + (topLeft.x + width/2));
		//float newCircleY = (float)(Math.sin(transform.rotation) * (collider.transform.x - (topLeft.x + width/2)) - Math.cos(transform.rotation)
		//		* (collider.transform.y - (topLeft.y - height/2)) + (topLeft.y - height/2));		//-height/2 might need to be replaced with +height/2
		
		float newCircleX = collider.transform.x;
		float newCircleY = collider.transform.y;
		
		float closestX, closestY;
		
		if(newCircleX < topLeft.x)
			closestX = topLeft.x;
		else if(newCircleX > topRight.x)
			closestX = topRight.x;
		else
			closestX = newCircleX;
		
		if(newCircleY > bottomLeft.y)
			closestY = bottomLeft.y;
		else if(newCircleY < topLeft.y)
			closestY = topLeft.y;
		else
			closestY = newCircleY;
		
		float dist = MathsUtils.modulus(newCircleX - closestX, newCircleY - closestY);
		if(dist < collider.getRadius() * Math.max(collider.transform.scaleX, collider.transform.scaleY))	//Use greatest scale for circle collider
			return true;
		else
			return false;
	}
	
	/**Rectangle to line collision*/
	public boolean collidesWith(LineCollider collider)
	{
		return false;
	}
	
	/**Rectangle to path collision*/
	public boolean collidesWith(PathCollider collider)
	{
		return false;
	}
	
	/**Checks whether the point is inside the rect collider*/
	public boolean collidesWith(float x, float y)
	{
		if(x >= topLeft.x && x <= topRight.x && y >= bottomLeft.y && y <= topLeft.y)
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
		
		/*SpinnerNumberModel mdlRotation = new SpinnerNumberModel();
			mdlRotation.setValue(transform.rotation);
			JSpinner spnRotation = new JSpinner(mdlRotation);
		JLabel lblRotation = new JLabel("Rotation:");*/
		
		JPanel pnlDimensions = new JPanel();
			GridLayout lytDimensions = new GridLayout(1, 2);
			pnlDimensions.setLayout(lytDimensions);
		SpinnerNumberModel mdlWidth = new SpinnerNumberModel();
			mdlWidth.setValue(width);
			JSpinner spnWidth = new JSpinner(mdlWidth);
		SpinnerNumberModel mdlHeight = new SpinnerNumberModel();
			mdlHeight.setValue(height);
			JSpinner spnHeight = new JSpinner(mdlHeight);
		JLabel lblDimensions = new JLabel("Dimensions (Width, Height):");
		pnlDimensions.add(spnWidth);
		pnlDimensions.add(spnHeight);
		
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
	//	mdlRotation.addChangeListener(e ->
	//	{
	//		transform.rotation = mdlRotation.getNumber().floatValue();
	//	});
		
		mdlWidth.addChangeListener(e ->
		{
			width = mdlWidth.getNumber().floatValue();
		});
		mdlHeight.addChangeListener(e ->
		{
			height = mdlHeight.getNumber().floatValue();
		});
		
		btnIsTrigger.addChangeListener(e ->
		{
			isTrigger = btnIsTrigger.isSelected();
		});
		
		pnl.add(lblPos);
		pnl.add(pnlPos);
		pnl.add(lblDimensions);
		pnl.add(pnlDimensions);
		//pnl.add(lblRotation);
		//pnl.add(spnRotation);
		pnl.add(lblIsTrigger);
		pnl.add(btnIsTrigger);
	}
	
	public String[] getEditableFields(File fleProject)
	{
		return new String[]{String.valueOf(isTrigger), String.valueOf(transform.x), String.valueOf(transform.y),
				String.valueOf(transform.rotation), String.valueOf(width), String.valueOf(height)};
	}
	
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject)
	{
		isTrigger = Boolean.valueOf(objs[0]);
		transform.x = Float.valueOf(objs[1]);
		transform.y = Float.valueOf(objs[2]);
		transform.rotation = Float.valueOf(objs[3]);
		width = Float.valueOf(objs[4]);
		height = Float.valueOf(objs[5]);
	}
	
	public void close() {}
	
	public void updateAsGame(GameObject thisGameObject)
	{
		/**Update the positions of the vertices incase the game object moved*/
		topLeft.x = bottomLeft.x = thisGameObject.getTransform().x + transform.x * thisGameObject.getTransform().scaleX;
		topLeft.y = topRight.y = thisGameObject.getTransform().y + transform.y * thisGameObject.getTransform().scaleY;
		topRight.x = bottomRight.x = topLeft.x + width * thisGameObject.getTransform().scaleX;
		bottomLeft.y = bottomRight.y = topLeft.y + height * thisGameObject.getTransform().scaleY;
	}
	
	public void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject)
	{
		/**Update the positions of the vertices incase the game object moved*/
		topLeft.x = bottomLeft.x = thisGameObject.getTransform().x + transform.x * thisGameObject.getTransform().scaleX;
		topLeft.y = topRight.y = thisGameObject.getTransform().y + transform.y * thisGameObject.getTransform().scaleY;
		topRight.x = bottomRight.x = topLeft.x + width * thisGameObject.getTransform().scaleX;
		bottomLeft.y = bottomRight.y = topLeft.y + height * thisGameObject.getTransform().scaleY;
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
		glBegin(GL_LINES);
			glVertex2f(transform.x, transform.y);
			glVertex2f(transform.x+width, transform.y);
			glVertex2f(transform.x+width, transform.y);
			glVertex2f(transform.x+width, transform.y+height);
			glVertex2f(transform.x+width, transform.y+height);
			glVertex2f(transform.x, transform.y+height);
			glVertex2f(transform.x,transform.y+height);
			glVertex2f(transform.x, transform.y);
		glEnd();
		glColor3f(1, 1, 1);
		glPopMatrix();
	}
	
	public Vector2f getTopLeft()
	{
		return topLeft;
	}
	
	public Vector2f getBottomLeft()
	{
		return bottomLeft;
	}
	
	public Vector2f getTopRight()
	{
		return topRight;
	}
	
	public Vector2f getBottomRight()
	{
		return bottomRight;
	}
}


//Rotation rectangle collision start
/*float axis1X = topRight.x - topLeft.x;
float axis1Y = topRight.y - topLeft.y;
float axis2X = topRight.x - bottomRight.x;
float axis2Y = topRight.y - bottomRight.y;
float axis3X = rectCollider.getTopLeft().x - rectCollider.getBottomLeft().x;
float axis3Y = rectCollider.getTopLeft().y - rectCollider.getBottomLeft().y;
float axis4X = rectCollider.getTopLeft().x - rectCollider.getTopRight().x;
float axis4Y = rectCollider.getTopLeft().x - rectCollider.getTopRight().x;
*/

/*System.out.println(topLeft.y <= rectCollider.bottomLeft.y);
System.out.println(bottomLeft.y >= rectCollider.topLeft.y);
System.out.println(bottomLeft.x <= rectCollider.bottomRight.x);
System.out.println(bottomRight.x >= rectCollider.bottomLeft.x);
System.out.println(rectCollider.topLeft.y < bottomLeft.y);
System.out.println(rectCollider.bottomLeft.y > topLeft.y);
System.out.println(rectCollider.bottomLeft.x < bottomRight.x);
System.out.println(rectCollider.bottomRight.x > bottomLeft.x);
System.out.println(System.lineSeparator());*/
