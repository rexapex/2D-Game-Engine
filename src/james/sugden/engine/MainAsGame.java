package james.sugden.engine;

import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.glEnable;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import james.sugden.engine.input.InputHandler;

public class MainAsGame
{
	/**The executing game*/
	private static Game game;
	
	/**Run the game in game mode. Scripts and input are enabled.*/
	/*public final static void main(String[] args)
	{
		if(args == null || args.length < 1)
			return;
		
		//URL url = null;
		File fleProject = null;
		try
		{
			//url = MainAsGame.class.getResource("game/");
			//fleProject = new File(url.getPath());
			fleProject = new File(args[0]);
		} catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, "Error: Project not found: " + args[0]);
		    //JOptionPane.showMessageDialog(null, url + " path = " + url.getPath() + " not found!");
		}
		
		
	}*/
	
	public final static void startAsGameFromSelector(Window parent, String projectPath)
	{
		File fleProject = null;
		try
		{
			fleProject = new File(projectPath);
		} catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, "Error: Project not found: " + projectPath);
		}
		
		try
		{
			JDialog dlgOptions = new JDialog(parent, JDialog.ModalityType.DOCUMENT_MODAL);
			JComboBox<String> cmbResolution = new JComboBox<String>();
			JRadioButton rdbFullscreen = new JRadioButton("Fullscreen");
			JButton btnContinue = new JButton("Continue");
			JPanel pnlContent = new JPanel();
			pnlContent.add(rdbFullscreen);
			pnlContent.add(cmbResolution);
			pnlContent.add(btnContinue);
			
			dlgOptions.getContentPane().add(pnlContent, BorderLayout.CENTER);
			
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			
			for(int i = 0; i < modes.length; i++)
			{
			    DisplayMode current = modes[i];
			    cmbResolution.addItem(current.getWidth() + "x" + current.getHeight() + "x" + current.getBitsPerPixel() + " " + current.getFrequency() + "Hz");
			}
			
			final JLabel fullscreen = new JLabel();
			final JLabel mode = new JLabel();
			
			btnContinue.addActionListener(e ->
			{
				mode.setText("" + cmbResolution.getSelectedIndex());
				fullscreen.setText("" + rdbFullscreen.isSelected());
				dlgOptions.dispose();
			});
			
			dlgOptions.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					int confirm = JOptionPane.showOptionDialog(dlgOptions, "Are You Sure You Want to Exit", "Exit?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
	                if(confirm == 0)
	                {
	                   System.exit(0);
	                }
				}
			});
			
			dlgOptions.setTitle("Options");
			dlgOptions.pack();
			dlgOptions.setLocationRelativeTo(null);
			dlgOptions.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dlgOptions.setAlwaysOnTop(true);
			dlgOptions.setVisible(true);
			
			Display.setFullscreen(Boolean.valueOf(fullscreen.getText()));
		    Display.setDisplayMode(modes[Integer.valueOf(mode.getText())]);
		    Display.create();
		} catch (LWJGLException e)
		{
		    e.printStackTrace();
		    System.exit(1);
		}
		
		//TODO - REPLACE LAST SCENE OPENED HERE WITH FIRST SCENE AS SET BY THE USER
		
		File fleLastScene = new File(fleProject.getAbsolutePath()+"/last_scene_edited.txt");
		String lastSceneOpened = null;
		try {
			BufferedReader rdrLastScene = new BufferedReader(new FileReader(fleLastScene));
			lastSceneOpened = rdrLastScene.readLine();
			rdrLastScene.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("FAILED AT READING LAST SCENE");
			StringBuilder sb = new StringBuilder(e.toString());
		    for (StackTraceElement ste : e.getStackTrace()) {
		        sb.append("\n\tat ");
		        sb.append(ste);
		    }
		    String trace = sb.toString();
		    JOptionPane.showMessageDialog(null, trace, e.toString(), JOptionPane.ERROR_MESSAGE);
		}
		
		glEnable(GL_STENCIL_TEST);
		Setup.initGLProjection();
		Setup.initGLRendering();
		Setup.initALAudio();
				
		game = new Game(fleProject);
		game.init(null, null);
		game.switchToNewScene(lastSceneOpened);
		game.startAsGame();
	}
	
	public final static void startAsGameFromEditor(Scene scene, InputHandler input, File fleProject)
	{
		glEnable(GL_STENCIL_TEST);
		Scene newScene = new Scene(scene);
		game = new Game(fleProject);
		game.init(newScene, input);
		Lookup.init(input, newScene, game);
		newScene.initAsGame();
		game.startAsGame();
	}
	
	public static final void stopGame()
	{
		game.stopGame();
	}
}
