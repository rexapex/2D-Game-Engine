package james.sugden.engine.lighting;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.game_object.UpdatableComponent;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.file_handling.ResourceManager;
import james.sugden.utils.RenderingUtils;

public class PointLight extends UpdatableComponent
{
	/**The colour of the light*/
	private float red, green, blue;
	
	/**The position of the light - taken from the transform of the game object*/
	private float x, y;
	
	/**Strength of light and radius of view*/
	private float intensity;
	
	/**Attenuation/falloff of the light with distance*/
	private float constant, linear, quadratic;
	
	public PointLight()
	{
		super();
		red = green = blue = 1.0f;
		x = y = 0;
		intensity = 1;
		constant = 1;
		linear = 0.022f;
		quadratic = 0.0019f;
	}
	
	public PointLight(float red, float green, float blue)
	{
		super();
		this.red = red;
		this.blue = blue;
		this.green = green;
		this.intensity = 1;
		this.constant = 1;
		this.linear = 0.022f;
		this.quadratic = 0.0019f;
	}
	
	/**Deep copy constructor*/
	public PointLight(PointLight light)
	{
		super(light);
		this.x = light.x;
		this.y = light.y;
		this.red = light.red;
		this.blue = light.blue;
		this.green = light.green;
		this.intensity = light.intensity;
		this.linear = light.linear;
		this.constant = light.constant;
		this.quadratic = light.quadratic;
	}
	
	protected void init()
	{
		this.typeName = "Point Light";
	}
	
	public final void updateAsGame(GameObject thisGameObject)
	{
		x = thisGameObject.getTransform().x;
		y = thisGameObject.getTransform().y;
	}
	
	public final void drawArea()
	{
		RenderingUtils.drawFilledCircle(intensity, x, y);
	}
	
	public final void setPos(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public void createEditingPanel(JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj)
	{
		JLabel lblColour = new JLabel("Colour (Red, Green, Blue):");
			lblColour.setToolTipText("The colour of the light");
		JButton btnChooseColour = new JButton("Choose Colour");
		
		btnChooseColour.addActionListener(e ->
		{
			JDialog dlgColourChooser = new JDialog();
			dlgColourChooser.setAlwaysOnTop(true);
			dlgColourChooser.setTitle("Change colour of: " + identificationName);
			
			JColorChooser chrColour = new JColorChooser();
			chrColour.setPreviewPanel(new JPanel());
			chrColour.getSelectionModel().addChangeListener(e1 ->
			{
				Color colour = chrColour.getColor();
				red = colour.getRed()/255;
				green = colour.getGreen()/255;
				blue = colour.getBlue()/255;
			});
			
			dlgColourChooser.add(chrColour);
			dlgColourChooser.pack();
			dlgColourChooser.setVisible(true);
		});
		
		JPanel pnlAtten = new JPanel();
		GridLayout lytPos = new GridLayout(1, 0);
			pnlAtten.setLayout(lytPos);
		SpinnerNumberModel mdlC = new SpinnerNumberModel();
			mdlC.setValue(constant);
			JSpinner spnC = new JSpinner(mdlC);
			spnC.setToolTipText("Constant component");
		SpinnerNumberModel mdlL = new SpinnerNumberModel();
			mdlL.setValue(linear);
			JSpinner spnL = new JSpinner(mdlL);
			spnL.setToolTipText("Linear component");
		SpinnerNumberModel mdlQ = new SpinnerNumberModel();
			mdlQ.setValue(linear);
			JSpinner spnQ = new JSpinner(mdlQ);
			spnQ.setToolTipText("Quadratic component");
		JLabel lblAtten = new JLabel("Attenuation (C, L, Q):");
			lblAtten.setToolTipText("The attenuation/falloff of the light over distance");
			pnlAtten.add(spnC);
			pnlAtten.add(spnL);
			pnlAtten.add(spnQ);
		
		mdlC.addChangeListener(e ->
		{
			constant = mdlC.getNumber().floatValue();
		});
		mdlL.addChangeListener(e ->
		{
			linear = mdlL.getNumber().floatValue();
		});
		mdlQ.addChangeListener(e ->
		{
			quadratic = mdlQ.getNumber().floatValue();
		});
		
		SpinnerNumberModel mdlInten = new SpinnerNumberModel();
			mdlInten.setValue(intensity);
			JSpinner spnInten = new JSpinner(mdlInten);
		JLabel lblInten = new JLabel("Intensity:");
			lblInten.setToolTipText("The intensity of the light source");
		
		mdlInten.addChangeListener(e ->
		{
			intensity = mdlInten.getNumber().floatValue();
		});
		
		//constraints.gridy = 0;
		//constraints.gridx = 0;
		//constraints.anchor = GridBagConstraints.WEST;
		pnl.add(lblColour);
		//constraints.anchor = GridBagConstraints.EAST;
		//constraints.gridx++;
		pnl.add(btnChooseColour);
		//constraints.anchor = GridBagConstraints.WEST;
		//constraints.gridy++;
		//constraints.gridx = 0;
		pnl.add(lblAtten);
		//constraints.anchor = GridBagConstraints.EAST;
		//constraints.gridx++;
		pnl.add(pnlAtten);
		///constraints.anchor = GridBagConstraints.WEST;
		//constraints.gridy++;
		//constraints.gridx = 0;
		pnl.add(lblInten);
		//constraints.anchor = GridBagConstraints.EAST;
		///constraints.gridx++;
		pnl.add(spnInten);
	}
	
	public String[] getEditableFields(File fleProject)
	{
		return new String[]{new String(red + "," + green + "," + blue), new String(constant + "," + linear + "," + quadratic), new String(intensity + "")};
	}
	
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject)
	{
		String obj1 = objs[0];
		red = Float.valueOf(obj1.split(",")[0]);
		green = Float.valueOf(obj1.split(",")[1]);
		blue = Float.valueOf(obj1.split(",")[2]);
		
		String obj2 = objs[1];
		constant = Float.valueOf(obj2.split(",")[0]);
		linear = Float.valueOf(obj2.split(",")[1]);
		quadratic = Float.valueOf(obj2.split(",")[2]);
		
		String obj3 = objs[2];
		intensity = Float.valueOf(obj3);
	}
	
	public void close() {}
	public void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
	public void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
	
	public float getRed()
	{
		return red;
	}
	
	public float getGreen()
	{
		return green;
	}
	
	public float getBlue()
	{
		return blue;
	}
	
	public float getX()
	{
		return x;
	}
	
	public float getY()
	{
		return y;
	}
	
	public float getIntensity()
	{
		return intensity;
	}
	
	public float getAttenConstant()
	{
		return constant;
	}
	
	public float getAttenLinear()
	{
		return linear;
	}
	
	public float getAttenQuadratic()
	{
		return quadratic;
	}
}
