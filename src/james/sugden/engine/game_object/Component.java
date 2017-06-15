package james.sugden.engine.game_object;

import java.awt.GridBagConstraints;
import java.io.File;

import javax.swing.JPanel;

import james.sugden.engine.input.form.EEventObject;
import james.sugden.file_handling.ResourceManager;

public abstract class Component
{
	/**The name of the type of component*/
	protected String typeName;
	
	/**The name given by the user/system*/
	protected String identificationName;
	
	public Component()
	{
		init();
	}
	
	/**Deep copy constructor*/
	public Component(Component c)
	{
		this.typeName = c.typeName;
		this.identificationName = c.identificationName;
	}
	
	/**Initialise the component
	 * Should set the display name of the component*/
	protected abstract void init();
	
	/**Creates a panel which allows editing of the component in the scene editor*/
	public abstract void createEditingPanel(final JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj);
	
	/**Returns the editable fields for the specific component type which can be edited in the scene editor*/
	public abstract String[] getEditableFields(File fleProject);
	
	/**Sets the values of the editable fields
	 * Must be in same order they are returned in*/
	public abstract void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject);
	
	/**Returns the type name specific to the component type*/
	public final String getTypeName()
	{
		return typeName;
	}
	
	/**Returns the identification name specific to the component type*/
	public final String getIdentificationName()
	{
		return identificationName;
	}
	
	/**Allows the system/user to set the identification name*/
	public final void setIdentificationName(String name)
	{
		identificationName = name;
	}
	
	/**Called when the program is closed down*/
	public abstract void close();
	
	public abstract void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject);
	public abstract void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject);
}
