package james.sugden.engine.camera;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import james.sugden.engine.Time;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.engine.maths.MathsUtils;
import james.sugden.engine.maths.Transform;
import james.sugden.file_handling.ResourceManager;

public class FollowCamera extends Camera
{
	private float offsetX, offsetY, scaleX, scaleY;
	
	public FollowCamera()
	{
		super();
		
		this.offsetX = this.offsetY = 0.0f;
		this.scaleX = this.scaleY = 1.0f;
		
		this.typeName = "Follow Camera";
	}
	
	public FollowCamera(float x, float y, float z)
	{
		super(x, y, z);
		
		this.offsetX = this.offsetY = 0.0f;
		this.scaleX = this.scaleY = 1.0f;
		
		this.typeName = "Follow Camera";
	}
	
	public FollowCamera(Transform t)
	{
		this(t.x, t.y, t.rotation);
	}
	
	public FollowCamera(FollowCamera cam)
	{
		this(cam.transform);
		
		this.offsetX = cam.offsetX;
		this.offsetY = cam.offsetY;
		this.scaleX = cam.scaleX;
		this.scaleY = cam.scaleY;
		this.identificationName = cam.identificationName;
	}
	
	@Override
	public void createEditingPanel(final JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj)
	{
		JPanel pnlOffset = new JPanel();
			GridLayout lytOffset = new GridLayout(1, 0);
			//lytOffset.setHgap(20);
			pnlOffset.setLayout(lytOffset);
		SpinnerNumberModel mdlX = new SpinnerNumberModel();
			mdlX.setValue(offsetX);
			JSpinner spnX = new JSpinner(mdlX);
		SpinnerNumberModel mdlY = new SpinnerNumberModel();
			mdlY.setValue(offsetY);
			JSpinner spnY = new JSpinner(mdlY);
		JLabel lblPos = new JLabel("Offset (X, Y):");
			lblPos.setToolTipText("The co-ordinates of the camera relative to the game object");
		pnlOffset.add(spnX);
		pnlOffset.add(spnY);
		
		JPanel pnlScale = new JPanel();
			GridLayout lytScale = new GridLayout(1, 0);
			//lytOffset.setHgap(20);
			pnlScale.setLayout(lytScale);
		SpinnerNumberModel mdlScaleX = new SpinnerNumberModel();
			mdlScaleX.setValue(scaleX);
			mdlScaleX.setStepSize(0.1f);
			JSpinner spnScaleX = new JSpinner(mdlScaleX);
		SpinnerNumberModel mdlScaleY = new SpinnerNumberModel();
			mdlScaleY.setValue(scaleY);
			mdlScaleY.setStepSize(0.1f);
			JSpinner spnScaleY = new JSpinner(mdlScaleY);
		JLabel lblScale = new JLabel("Zoom (X, Y):");
			lblScale.setToolTipText("Low values zoom out, high values zoom in");
		pnlScale.add(spnScaleX);
		pnlScale.add(spnScaleY);
		
		mdlX.addChangeListener(e ->
		{
			offsetX = mdlX.getNumber().floatValue();
		});
		mdlY.addChangeListener(e ->
		{
			offsetY = mdlY.getNumber().floatValue();
		});
		
		mdlScaleX.addChangeListener(e ->
		{
			scaleX = mdlScaleX.getNumber().floatValue();
		});
		mdlScaleY.addChangeListener(e ->
		{
			scaleY = mdlScaleY.getNumber().floatValue();
		});
		
		pnl.add(lblPos);
		pnl.add(pnlOffset);
		pnl.add(lblScale);
		pnl.add(pnlScale);
	}
	
	public final void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
	
	@Override
	public final void updateAsGame(GameObject thisGameObject)
	{
		transform.x = MathsUtils.lerp(transform.x, -thisGameObject.getTransform().x + offsetX, 0.25f);
		transform.y = MathsUtils.lerp(transform.y, -thisGameObject.getTransform().y + offsetY, 0.25f);
		transform.scaleX = scaleX;
		transform.scaleY = scaleY;
	}
	
	public void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
	
	@Override
	public String[] getEditableFields(File fleProject)
	{
		return new String[]{""+transform.x, ""+transform.y, ""+offsetX, ""+offsetY, ""+scaleX, ""+scaleY};
	}
	
	@Override
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject)
	{
		transform.x = Float.valueOf(objs[0]);
		transform.y = Float.valueOf(objs[1]);
		offsetX = Float.valueOf(objs[2]);
		offsetY = Float.valueOf(objs[3]);
		scaleX = Float.valueOf(objs[4]);
		scaleY = Float.valueOf(objs[5]);
	}
}
