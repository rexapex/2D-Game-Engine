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

public class SpotLight extends UpdatableComponent
{
	/**The colour of the light*/
	private float red, green, blue;
	
	/**The position of the light - taken from the transform of the game object*/
	private float x, y;
	
	/**The direction of the light*/
	private float dirx, diry;
	
	/**Strength of light and radius of view*/
	private float intensity;
	
	/**Attenuation/falloff of the light with distance*/
	private float constant, linear, quadratic;
	
	float cosInnerCutoff;	//Cosine of the inner cutoff angle
	float cosOuterCutoff;	//Cosine of the outer cutoff angle
	
	public SpotLight()
	{
		super();
		red = green = blue = 1.0f;
		x = y = diry = 0;
		dirx = 1;
		intensity = 1;
		constant = 1;
		linear = 0.022f;
		quadratic = 0.0019f;
		cosInnerCutoff = 0.9f;
		cosOuterCutoff = 0.6f;
	}
	
	public SpotLight(float red, float green, float blue)
	{
		super();
		this.x = this.y = this.diry = 0;
		this.dirx = 1;
		this.red = red;
		this.blue = blue;
		this.green = green;
		this.intensity = 1;
		this.constant = 1;
		this.linear = 0.022f;
		this.quadratic = 0.0019f;
		this.cosInnerCutoff = 0.9f;
		this.cosOuterCutoff = 0.6f;
	}
	
	/**Deep copy constructor*/
	public SpotLight(SpotLight light)
	{
		super(light);
		this.x = light.x;
		this.y = light.y;
		this.dirx = light.dirx;
		this.diry = light.diry;
		this.red = light.red;
		this.blue = light.blue;
		this.green = light.green;
		this.intensity = light.intensity;
		this.linear = light.linear;
		this.constant = light.constant;
		this.quadratic = light.quadratic;
		this.cosInnerCutoff = light.cosInnerCutoff;
		this.cosOuterCutoff = light.cosOuterCutoff;
	}
	
	protected void init()
	{
		this.typeName = "Spot Light";
	}
	
	public final void updateAsGame(GameObject thisGameObject)
	{
		x = thisGameObject.getTransform().x;
		y = thisGameObject.getTransform().y;
	}
	
	public final void drawArea()
	{
		//RenderingUtils.drawFilledCircle(intensity, x, y);
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
		
		JPanel pnlDir = new JPanel();
		GridLayout lytDir = new GridLayout(1, 0);
			pnlDir.setLayout(lytDir);
		SpinnerNumberModel mdlDirX = new SpinnerNumberModel();
			mdlDirX.setValue(dirx);
			JSpinner spnDirX = new JSpinner(mdlDirX);
		SpinnerNumberModel mdlDirY = new SpinnerNumberModel();
			mdlDirY.setValue(diry);
			JSpinner spnDirY = new JSpinner(mdlDirY);
		JLabel lblDir = new JLabel("Direction (X, Y):");
			lblDir.setToolTipText("The direction of the light represented as a line segment");
			pnlDir.add(spnDirX);
			pnlDir.add(spnDirY);
		
		mdlDirX.addChangeListener(e ->
		{
			dirx = mdlDirX.getNumber().floatValue();
		});
		mdlDirY.addChangeListener(e ->
		{
			diry = mdlDirY.getNumber().floatValue();
		});
		
		JPanel pnlCutoff = new JPanel();
		GridLayout lytCutoff = new GridLayout(1, 0);
			pnlCutoff.setLayout(lytCutoff);
		SpinnerNumberModel mdlInner = new SpinnerNumberModel();
			mdlInner.setValue(Math.acos(cosInnerCutoff));
			JSpinner spnInner = new JSpinner(mdlInner);
			spnInner.setToolTipText("Inner Cutoff Angle");
		SpinnerNumberModel mdlOuter = new SpinnerNumberModel();
			mdlOuter.setValue(Math.acos(cosOuterCutoff));
			JSpinner spnOuter = new JSpinner(mdlOuter);
			spnOuter.setToolTipText("Outer Cutoff Angle");
		JLabel lblCutoff = new JLabel("Cutoff Angle (I, O):");
			lblCutoff.setToolTipText("The cutoff angles of the spot light wedge");
			pnlCutoff.add(spnInner);
			pnlCutoff.add(spnOuter);
		
		mdlInner.addChangeListener(e ->
		{
			cosInnerCutoff = (float)Math.cos(mdlInner.getNumber().floatValue());
		});
		mdlOuter.addChangeListener(e ->
		{
			cosOuterCutoff = (float)Math.cos(mdlOuter.getNumber().floatValue());
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
		
		pnl.add(lblColour);
		pnl.add(btnChooseColour);
		pnl.add(lblAtten);
		pnl.add(pnlAtten);
		pnl.add(lblDir);
		pnl.add(pnlDir);
		pnl.add(lblCutoff);
		pnl.add(pnlCutoff);
		pnl.add(lblInten);
		pnl.add(spnInten);
	}
	
	public String[] getEditableFields(File fleProject)
	{
		return new String[]{new String(red + "," + green + "," + blue), new String(constant + "," + linear + "," + quadratic), new String(intensity + ""), new String(dirx + "," + diry), new String(cosInnerCutoff  + "," + cosOuterCutoff)};
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
		
		String obj4 = objs[3];
		dirx = Float.valueOf(obj4.split(",")[0]);
		diry = Float.valueOf(obj4.split(",")[1]);
		
		String obj5 = objs[4];
		cosInnerCutoff = Float.valueOf(obj5.split(",")[0]);
		cosOuterCutoff = Float.valueOf(obj5.split(",")[1]);
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
	
	public float getDirX()
	{
		return dirx;
	}
	
	public float getDirY()
	{
		return diry;
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
	
	public float getCosInnerCutoff()
	{
		return cosInnerCutoff;
	}
	
	public float getCosOuterCutoff()
	{
		return cosOuterCutoff;
	}
}
