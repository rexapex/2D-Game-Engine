package james.sugden.engine.scripts;

import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.game_object.UpdatableComponent;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.file_handling.ResourceManager;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.TransferHandler;

public class Script extends UpdatableComponent	//Script component used by the game engine
{
	/**The file of the script*/
	private File fleScript;
	
	/**An instance of the script which can be run*/
	private JPGEngineScript engineScript;
	
	public Script()
	{
		super();
	}
	
	/**Deep copy constructor*/
	public Script(Script s)
	{
		super(s);
		
		this.fleScript = s.fleScript;
		this.engineScript = null;
	}
	
	protected void init()
	{
		this.typeName = "Script";
		this.fleScript = null;
	}
	
	public final void initEngineScript(GameObject thisGameObject)
	{
		if(engineScript != null)
		{
			Thread t = new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							engineScript.init(thisGameObject);
						}
					});
			t.start();
			try
			{
				t.join();						//Join the thread so execution pauses until the script init has finished
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		else
			System.out.println("Script - " + identificationName + " - is null");
	}
	
	public void updateAsGame(GameObject thisGameObject)
	{
		if(engineScript != null)
		{
			Thread t = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					engineScript.update(thisGameObject);
				}
			});
			t.start();
			try
			{
				t.join();						//Join the thread so execution pauses until the script update has finished
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void updateAsEditor(GameObject thisGameObject, GameObject selectedGameObject)
	{
		
	}
	
	public void drawAsEditor(GameObject thisGameObject, GameObject selectedGameObject) {}
	
	public void createEditingPanel(JPanel pnl, final GridBagConstraints constraints, ResourceManager resourceManager, EEventObject eventObj)
	{
		JLabel lblScriptFile = new JLabel("Script File:");
		
		JPanel pnlFile = new JPanel(new GridLayout(1, 2));
		JTextField txtFile = new JTextField();
		JButton btnEditFile = new JButton("Edit File");
		pnlFile.add(txtFile);
		pnlFile.add(btnEditFile);
		
		if(fleScript != null)
			txtFile.setText(fleScript.getName());
		
		
		txtFile.setTransferHandler(new TransferHandler("text"));	//Allow dropping of text onto the text field
		
		txtFile.addKeyListener(new KeyAdapter()
		{
			public void keyReleased(KeyEvent e)
			{
				File newScriptFile = null;
				if((newScriptFile = resourceManager.doesScriptExist(txtFile.getText())) != null)	//Returns absolute file so set this script to it
					fleScript = newScriptFile;
			}
		});
		
		btnEditFile.addActionListener(e ->
		{
			if(!txtFile.getText().trim().equals(""))
			{
				fleScript = new File(txtFile.getText());
				if(!fleScript.exists())		//If script file does not exist then create it
				{
					txtFile.setText(txtFile.getText().replaceAll(" ", "_"));
					fleScript = resourceManager.createScriptFile(txtFile.getText());
				}
				
				eventObj.fireEvent(EEventObject.EEventType.OPEN_CODE_EDITOR);	//Load the code editor
			} else
			{
				JOptionPane.showMessageDialog(pnl, "Must give the script a name", "Script Name", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		pnl.add(lblScriptFile);
		pnl.add(pnlFile);
	}
	
	public String[] getEditableFields(File fleProject)
	{
		return fleScript != null ? new String[]{fleScript.getName()} : new String[]{null};
	}
	
	public void setEditableFields(String[] objs, ResourceManager resourceManager, File fleProject)
	{
		fleScript = new File(fleProject.getAbsolutePath() + "/res/scripts/" + objs[0]);
	}
	
	public void close() {}
	
	public File getScriptFile()
	{
		return fleScript;
	}
	
	public void setJPGEngineScript(JPGEngineScript engineScript)
	{
		this.engineScript = engineScript;
	}
}
