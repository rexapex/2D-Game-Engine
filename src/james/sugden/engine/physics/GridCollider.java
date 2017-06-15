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
import james.sugden.file_handling.ResourceManager;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.lwjgl.util.vector.Vector2f;

public class GridCollider extends Collider
{
	/**The coordinates of the vertices of the grid*/
	private Vector2f topLeft, topRight, bottomLeft, bottomRight;
	
	/**The number of vertical and horizontal splits of the grid*/
	private int horizontalSplits, verticalSplits;
	
	public void init()
	{
		super.init();
		
		super.typeName = "Grid Collider";
		
		this.isTrigger = true;
		this.topLeft = new Vector2f(-32, 32);
		this.topRight = new Vector2f(32, 32);
		this.bottomLeft = new Vector2f(-32, -32);
		this.bottomRight = new Vector2f(32, -32);
		this.horizontalSplits = this.verticalSplits = 5;
	}
	
	/**Collision with a rectangle*/
	public boolean collidesWith(RectCollider rectCollider) {return false;}
	
	/**Collision with a point*/
	public boolean collidesWith(float x, float y)
	{
		
		
		return false;
	}
	
	public void createEditingPanel(JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj)
	{
		JPanel pnlSplits = new JPanel();
			GridLayout lytSplits = new GridLayout(1, 2);
			pnlSplits.setLayout(lytSplits);
		SpinnerNumberModel mdlHorizontalSplits = new SpinnerNumberModel();
			mdlHorizontalSplits.setValue(horizontalSplits);
			JSpinner spnHorizontalSplits = new JSpinner(mdlHorizontalSplits);
		SpinnerNumberModel mdlVerticalSplits = new SpinnerNumberModel();
			mdlVerticalSplits.setValue(verticalSplits);
			JSpinner spnVerticalSplits = new JSpinner(mdlVerticalSplits);
		JLabel lblSplits = new JLabel("Splits (Horizontal, Vertical)");
		pnlSplits.add(spnHorizontalSplits);
		pnlSplits.add(spnVerticalSplits);
		
		JPanel pnlPosTopLeft = new JPanel();
			GridLayout lytPosTopLeft = new GridLayout(1, 2);
			pnlPosTopLeft.setLayout(lytPosTopLeft);
		SpinnerNumberModel mdlXTopLeft = new SpinnerNumberModel();
			mdlXTopLeft.setValue(topLeft.x);
			JSpinner spnXTopLeft = new JSpinner(mdlXTopLeft);
		SpinnerNumberModel mdlYTopLeft = new SpinnerNumberModel();
			mdlYTopLeft.setValue(topLeft.y);
			JSpinner spnYTopLeft = new JSpinner(mdlYTopLeft);
		JLabel lblPosTopLeft = new JLabel("Top Left (X, Y):");
		pnlPosTopLeft.add(spnXTopLeft);
		pnlPosTopLeft.add(spnYTopLeft);
			
		JPanel pnlPosTopRight = new JPanel();
			GridLayout lytPosTopRight = new GridLayout(1, 2);
			pnlPosTopRight.setLayout(lytPosTopRight);
		SpinnerNumberModel mdlXTopRight = new SpinnerNumberModel();
			mdlXTopRight.setValue(topRight.x);
			JSpinner spnXTopRight = new JSpinner(mdlXTopRight);
		SpinnerNumberModel mdlYTopRight = new SpinnerNumberModel();
			mdlYTopRight.setValue(topRight.y);
			JSpinner spnYTopRight = new JSpinner(mdlYTopRight);
		JLabel lblPosTopRight = new JLabel("Top Right (X, Y):");
		pnlPosTopRight.add(spnXTopRight);
		pnlPosTopRight.add(spnYTopRight);
		
		JPanel pnlPosBottomLeft = new JPanel();
			GridLayout lytPosBottomLeft = new GridLayout(1, 2);
			pnlPosBottomLeft.setLayout(lytPosBottomLeft);
		SpinnerNumberModel mdlXBottomLeft = new SpinnerNumberModel();
			mdlXBottomLeft.setValue(bottomLeft.x);
			JSpinner spnXBottomLeft = new JSpinner(mdlXBottomLeft);
		SpinnerNumberModel mdlYBottomLeft = new SpinnerNumberModel();
			mdlYBottomLeft.setValue(bottomLeft.y);
			JSpinner spnYBottomLeft = new JSpinner(mdlYBottomLeft);
		JLabel lblPosBottomLeft = new JLabel("Bottom Left (X, Y):");
		pnlPosBottomLeft.add(spnXBottomLeft);
		pnlPosBottomLeft.add(spnYBottomLeft);
		
		JPanel pnlPosBottomRight = new JPanel();
			GridLayout lytPosBottomRight = new GridLayout(1, 2);
			pnlPosBottomRight.setLayout(lytPosBottomRight);
		SpinnerNumberModel mdlXBottomRight = new SpinnerNumberModel();
			mdlXBottomRight.setValue(bottomRight.x);
			JSpinner spnXBottomRight = new JSpinner(mdlXBottomRight);
		SpinnerNumberModel mdlYBottomRight = new SpinnerNumberModel();
			mdlYBottomRight.setValue(bottomRight.y);
			JSpinner spnYBottomRight = new JSpinner(mdlYBottomRight);
		JLabel lblPosBottomRight = new JLabel("Bottom Right (X, Y):");
		pnlPosBottomRight.add(spnXBottomRight);
		pnlPosBottomRight.add(spnYBottomRight);
		
	//	JLabel lblIsTrigger = new JLabel("Is Trigger");
	//	JCheckBox btnIsTrigger = new JCheckBox();
	//	btnIsTrigger.setSelected(isTrigger);
		
		mdlHorizontalSplits.addChangeListener(e ->
		{
			horizontalSplits = mdlHorizontalSplits.getNumber().intValue();
		});
		mdlVerticalSplits.addChangeListener(e ->
		{
			verticalSplits = mdlVerticalSplits.getNumber().intValue();
		});
		mdlXTopLeft.addChangeListener(e ->
		{
			topLeft.x = mdlXTopLeft.getNumber().floatValue();
		});
		mdlYTopLeft.addChangeListener(e ->
		{
			topLeft.y = mdlYTopLeft.getNumber().floatValue();
		});
		mdlXTopRight.addChangeListener(e ->
		{
			topRight.x = mdlXTopRight.getNumber().floatValue();
		});
		mdlYTopRight.addChangeListener(e ->
		{
			topRight.y = mdlYTopRight.getNumber().floatValue();
		});
		mdlXBottomLeft.addChangeListener(e ->
		{
			bottomLeft.x = mdlXBottomLeft.getNumber().floatValue();
		});
		mdlYBottomLeft.addChangeListener(e ->
		{
			bottomLeft.y = mdlYBottomLeft.getNumber().floatValue();
		});
		mdlXBottomRight.addChangeListener(e ->
		{
			bottomRight.x = mdlXBottomRight.getNumber().floatValue();
		});
		mdlYBottomRight.addChangeListener(e ->
		{
			bottomRight.y = mdlYBottomRight.getNumber().floatValue();
		});
		
	//	btnIsTrigger.addChangeListener(e ->
		//{
		//	isTrigger = btnIsTrigger.isSelected();
	//	});
		
		pnl.add(lblSplits, constraints);
		pnl.add(pnlSplits, constraints);
		pnl.add(lblPosTopLeft, constraints);
		pnl.add(pnlPosTopLeft, constraints);
		pnl.add(lblPosTopRight, constraints);
		pnl.add(pnlPosTopRight, constraints);
		pnl.add(lblPosBottomLeft, constraints);
		pnl.add(pnlPosBottomLeft, constraints);
		pnl.add(lblPosBottomRight, constraints);
		pnl.add(pnlPosBottomRight, constraints);
	//	pnl.add(lblIsTrigger, constraints);
	//	pnl.add(btnIsTrigger, constraints);
	}
	
	public void updateAsGame(GameObject thisGameObject) {}
	
	public void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
	
	public void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject)
	{
		glPushMatrix();
		if(isTrigger)
			glColor3f(0, 1, 0);
		else
			glColor3f(1, 0, 0);
		glTranslatef(0, 0, -1);
		glBegin(GL_LINES);
		for(int i = 0; i <= horizontalSplits; i++)
		{
			float topLength = i*(topRight.x-topLeft.x)/horizontalSplits;
			float bottomLength = i*(bottomRight.x-bottomLeft.x)/horizontalSplits;
			glVertex2f(topLeft.x + topLength, topRight.y + (topLeft.y - topRight.y) * ((topRight.x - topLeft.x - topLength)/(topRight.x - topLeft.x)));
			glVertex2f(bottomLeft.x + bottomLength, bottomRight.y + (bottomLeft.y - bottomRight.y) * ((bottomRight.x - bottomLeft.x - bottomLength)/(bottomRight.x - bottomLeft.x)));
		}
		for(int i = 0; i <= verticalSplits; i++)
		{
			float leftLength = i*(topLeft.y-bottomLeft.y)/verticalSplits;
			float rightLength = i*(topRight.y-bottomRight.y)/verticalSplits;
			glVertex2f(bottomLeft.x + (topLeft.x - bottomLeft.x) * (leftLength/(topLeft.y - bottomLeft.y)), bottomLeft.y + leftLength);
			glVertex2f(bottomRight.x + (topRight.x - bottomRight.x) * (rightLength/(topRight.y - bottomRight.y)), bottomRight.y + rightLength);
		}
		glEnd();
		glColor3f(1, 1, 1);
		glPopMatrix();
	}
	
	public String[] getEditableFields(File fleProject)
	{
		return new String[]{String.valueOf(horizontalSplits), String.valueOf(verticalSplits), String.valueOf(topLeft.x), String.valueOf(topLeft.y),
				String.valueOf(topRight.x), String.valueOf(topRight.y), String.valueOf(bottomLeft.x), String.valueOf(bottomLeft.y), String.valueOf(bottomRight.x), String.valueOf(bottomRight.y)};
	}
	
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject)
	{
		horizontalSplits = Integer.valueOf(objs[0]);
		verticalSplits = Integer.valueOf(objs[1]);
		topLeft.x = Float.valueOf(objs[2]);
		topLeft.y = Float.valueOf(objs[3]);
		topRight.x = Float.valueOf(objs[4]);
		topRight.y = Float.valueOf(objs[5]);
		bottomLeft.x = Float.valueOf(objs[6]);
		bottomLeft.y = Float.valueOf(objs[7]);
		bottomRight.x = Float.valueOf(objs[8]);
		bottomRight.y = Float.valueOf(objs[9]);
	}
	
	public void close() {}

	@Override
	public boolean collidesWith(CircleCollider rectCollider) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean collidesWith(LineCollider rectCollider) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean collidesWith(PathCollider rectCollider) {
		// TODO Auto-generated method stub
		return false;
	}
}
