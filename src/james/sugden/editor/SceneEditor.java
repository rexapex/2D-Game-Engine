package james.sugden.editor;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;

import james.sugden.engine.Lookup;
import james.sugden.engine.MainAsGame;
import james.sugden.engine.Scene;
import james.sugden.engine.Setup;
import james.sugden.engine.animation.AnimationRenderer;
import james.sugden.engine.animation.AnimationRenderer.AnimationTexture;
import james.sugden.engine.animation.Animator;
import james.sugden.engine.animation.Animator.Animation;
import james.sugden.engine.animation.Animator.Connector;
import james.sugden.engine.audio.AudioListener;
import james.sugden.engine.audio.AudioSource;
import james.sugden.engine.camera.Camera;
import james.sugden.engine.camera.DragCamera;
import james.sugden.engine.camera.FollowCamera;
import james.sugden.engine.game_object.Component;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.InputHandler;
import james.sugden.engine.input.KeyboardHandler;
import james.sugden.engine.input.KeyboardHandler.KeyBinding;
import james.sugden.engine.input.MouseHandler.MouseBinding;
import james.sugden.engine.input.form.EEventObject;
import james.sugden.engine.input.form.UIManager;
import james.sugden.engine.lighting.PointLight;
import james.sugden.engine.lighting.SpotLight;
import james.sugden.engine.physics.CircleCollider;
import james.sugden.engine.physics.RectCollider;
import james.sugden.engine.rendering.ImageRenderer;
import james.sugden.engine.rendering.ParticleEmitter;
import james.sugden.engine.scripts.Script;
import james.sugden.file_handling.ProjectFileHandler;
import james.sugden.file_handling.ResourceManager;
import james.sugden.utils.PrintStreamCapturer;
import james.sugden.utils.ProjectList.Project;
import james.sugden.utils.RenderingUtilsNEW;
import james.sugden.utils.ResourceBrowser;
import james.sugden.utils.Utils.BooleanHolder;
import james.sugden.utils.Utils.IntegerHolder;

public final class SceneEditor
{
	/**The main window of the scene editor*/
	private JFrame window = null;
	
	/**The canvas the opengl context is embedded onto*/
	private Canvas glCanvas;
	
	/**When set to true the editor will pause and will be run as game*/
	private volatile boolean runAsGame;
	private volatile boolean running;
	
	/**The scene being edited*/
	private Scene scene;
	
	/**The drag camera used by the scene in edit mode
	 * The default game camera chosen by the user*/
	private Camera editCamera, defaultGameCamera;
	
	/**Determines whether the scenes grid lines are drawn*/
	private boolean drawGrid;
	private float gridLineSpacingX, gridLineSpacingY;
	/**Determines whether snapping to grid lines is enabled while editing*/
	private boolean transformSnap;
	/**If true, lighting is applied while running in edit mode*/
	private boolean doLighting;
	
	/**Monitors input in the opengl window*/
	private InputHandler input;
	
	/**The project being edited*/
	private File fleProject;
	
	/**Keeps track of current user interfaces and handles loading/creatiung user interfaces*/
	private UIManager uiManager;
	
	/**A list of all resources loaded into the scene
	 * Imported resources are put here*/
	private ResourceManager resourceManager;
	
	/**A panel containing all components of the standard layout*/
	private Container pnlStdWindow;
	
	private JScrollPane sclGameObjectViewer, sclGameObjectInspector, sclConsole, sclDocumentationPanel;
	private JPanel pnlGameObjectInspector, pnlConsole, pnlDocumentation, pnlOpenglContext;
	private JList<GameObject> lstGameObject;
	private JPanel pnlAnimationEditor;
	private JTabbedPane pnlCentre, pnlBottom;
	/**Panel components are placed on*/
	private JTabbedPane pnlCodeEditor;
	/**Right panel used for game object inspector and user interface editor*/
	private JTabbedPane pnlRight;
	
	/**Resource browser instance which displays the currently imported resources to the user*/
	private ResourceBrowser resourceBrowser;
	
	/**The list model used in the game object viewer scroll pane*/
	private DefaultListModel<GameObject> lstModelGameObject;
	
	/**The object currently selected for inspection*/
	private GameObject gameObjectSelected;
	
	/**Variable which signifies the opengl canvas has changed size and the context needs to change size*/
	public boolean needUpdateViewport;
	
	/**The world coordinates of the mouse*/
	//private Vector4f mouseWorldCoords;
	
	/**Initialise the scene editor and load up the game specified*/
	public final void init()//, Graphics2D gSplash)	//Passed details of the splash screen so the splash can be updated as things load
	{
		WindowPreferences.initToDefault();
		
		window = new JFrame();
		Project project = ProjectFileHandler.setup(window);
		if(!project.edit)
			System.exit(0);
		fleProject = project.fleProject;
		resourceManager = new ResourceManager(fleProject);
		input = new InputHandler();
		uiManager = new UIManager();
		running = true;
		
		Lookup.init(input, scene, null);
		
		uiManager.init();
		
		gameObjectSelected = null;
		//mouseWorldCoords = null;
		
		runAsGame = false;
		doLighting = false;
		drawGrid = false;
		transformSnap = false;
		gridLineSpacingX = gridLineSpacingY = 30;
		
		/**Scene loading block*/
		//MainAsEditor.updateSplash(splash, gSplash, 10, "Finding Scene");
		{
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
			    JOptionPane.showMessageDialog(window, trace, e.toString(), JOptionPane.ERROR_MESSAGE);
			}
		//	MainAsEditor.updateSplash(splash, gSplash, 20, "Opening Project");
			this.scene = new Scene();
			if(lastSceneOpened != null)
				ProjectFileHandler.openProject(scene, fleProject, new File(fleProject.getAbsolutePath() + "/scenes/"+lastSceneOpened+".jps"), resourceManager, input);
			this.scene.initAsEditor();
			this.editCamera = new DragCamera(0, 0, 0);
			this.scene.setCamera(editCamera);
		}
		
	//	MainAsEditor.updateSplash(splash, gSplash, 50, "Initialising UI");
		
		pnlStdWindow = new JPanel();
		
		pnlCentre = new JTabbedPane();
		pnlBottom = new JTabbedPane();
		pnlRight = new JTabbedPane();
			pnlCentre.setBackground(WindowPreferences.getClrBackground());
			pnlBottom.setBackground(WindowPreferences.getClrBackground());
			pnlRight.setBackground(WindowPreferences.getClrBackground());
		
		pnlGameObjectInspector = new JPanel();
		pnlGameObjectInspector.setLayout(new BoxLayout(pnlGameObjectInspector, BoxLayout.Y_AXIS));
		sclGameObjectInspector = new JScrollPane(pnlGameObjectInspector);
			pnlGameObjectInspector.setBackground(WindowPreferences.getClrBackground());
		
		pnlConsole = new JPanel(new BorderLayout());
		sclConsole = new JScrollPane(pnlConsole);
		pnlDocumentation = new JPanel(new BorderLayout());
		sclDocumentationPanel = new JScrollPane(pnlDocumentation);
			pnlConsole.setBackground(WindowPreferences.getClrBackground());
			pnlDocumentation.setBackground(WindowPreferences.getClrBackground());
		
		pnlOpenglContext = new JPanel(new BorderLayout());
		pnlAnimationEditor = new JPanel(new BorderLayout());
		pnlCodeEditor = new JTabbedPane();
			pnlOpenglContext.setBackground(WindowPreferences.getClrBackground());
			pnlAnimationEditor.setBackground(WindowPreferences.getClrBackground());
			pnlCodeEditor.setBackground(WindowPreferences.getClrBackground());
		
		lstModelGameObject = new DefaultListModel<>();
		lstGameObject = new JList<>(lstModelGameObject);
		sclGameObjectViewer = new JScrollPane(lstGameObject);
		GameObjectListCellRenderer cellRenderer = new GameObjectListCellRenderer();
		lstGameObject.setCellRenderer(cellRenderer);
		lstGameObject.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		lstGameObject.setVisibleRowCount(-1);
		DropTargetListener l = new DropTargetListener()
				{
					@Override
					public void dragEnter(DropTargetDragEvent dtde) {}
					@Override
					public void dragOver(DropTargetDragEvent dtde) {}
					@Override
					public void dropActionChanged(DropTargetDragEvent dtde) {}
					@Override
					public void dragExit(DropTargetEvent dte) {}
					@Override
					public void drop(DropTargetDropEvent event)
					{
						String text = "Error";
						
						try
						{
							text = ((String)event.getTransferable().getTransferData(
							        new DataFlavor("application/x-java-jvm-local-objectref; class=java.lang.String"))).split("/")[1].split(".jpo")[0];
						} catch (ClassNotFoundException | UnsupportedFlavorException | IOException | ArrayIndexOutOfBoundsException e)
						{
							e.printStackTrace();
						}
						
						System.out.println(text);
						
						GameObject go = new GameObject();
						go.setPrefab(text);
						try
						{
							ProjectFileHandler.openGameObjectPrefab(go, fleProject, resourceManager);
							scene.addGameObject(go);
							createGameObjectViewer();
							go.setupComponents();
						} catch (NumberFormatException e)
						{
							e.printStackTrace();
						} catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				};
		new DropTarget(lstGameObject, DnDConstants.ACTION_COPY_OR_MOVE, l, true, null);
		
		//MainAsEditor.updateSplash(splash, gSplash, 60, "Building UI");
		
		createWindow();
		createMenuBar();
		createGameObjectInspector(null);
		createGameObjectViewer();
		createFileBrowser();
	//	createConsole();
	//	createDocumentationPanel();
	//	createAnimationEditor(null);
	//	createCodeEditor(null);
		createGameToolbar();
		//MainAsEditor.updateSplash(splash, gSplash, 90, "Initialising OpenGL");
		createOpenGLCanvas();
		createSplitPanes();
		
		makePanelFullscreen(pnlStdWindow);
		
		//window.setVisible(true);
		//window.revalidate();
		//window.repaint();
	}
	
	/**Start running the scene in editor mode*/
	private final void startScene()
	{
		updateScene();
	}
	
	/**Update the scene in editor mode*/
	private final void updateScene()
	{
		//Whether a game object is being moved using the on screen arrows
		boolean gameObjectMoveX = false, gameObjectMoveY = false;
		
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		while(running)
		{
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glLoadIdentity();
			
			input.update();
			
			//In game mode, the Game class provides methods or computing world position of mouse
			int worldx = (int)((input.getMouseX()/editCamera.getTransform().scaleX - editCamera.getTransform().x));
			int worldy = (int)((input.getMouseY()/editCamera.getTransform().scaleY - editCamera.getTransform().y));
			
			if(needUpdateViewport)
			{
				needUpdateViewport = false;
				//Setup.initGLProjection(glCanvas.getWidth(), glCanvas.getHeight());
			}
			
			if(runAsGame)
			{
				MainAsGame.startAsGameFromEditor(scene, input, fleProject);
				Lookup.init(input, scene, null);
				glDisable(GL_STENCIL_TEST);
			} else
			{
				if(doLighting && !scene.isLightingSetup())
					scene.setupLights();
				//mouseWorldCoords = Utils.calcWorldCoords(Mouse.getX(), Mouse.getY(), Mouse.getDX(), Mouse.getDY());
				editCamera.updateAsEditor(null, null);
			}
			
			String oldGameObjectSelectedName = "";
			if(gameObjectSelected != null)
				oldGameObjectSelectedName = gameObjectSelected.getName();
			gameObjectSelected = scene.updateAsEditor(gameObjectSelected, worldx, worldy);
			if(gameObjectSelected != null && !gameObjectSelected.getName().equals(oldGameObjectSelectedName))	//If the names are different then the game object selected has changed
				createGameObjectInspector(gameObjectSelected);					//Therefore update the inspection panel to display new selected object
			
			//Moved from GameObject updateAsEditor to SceneEditor updateScene because no callback method could be implemented in GameObject method
			if(gameObjectSelected != null && Mouse.isButtonDown(0))	//Allows translation of game object using arrows
			{
				if(!gameObjectMoveX && worldx > (gameObjectSelected.getTransform().x - 15) && worldx < (gameObjectSelected.getTransform().x + 15) &&
						worldy >= gameObjectSelected.getTransform().y && worldy < gameObjectSelected.getTransform().y + 45)	//Vertical
				{
					//gameObjectSelected.getTransform().y += Lookup.getInput().getMouseWorldDY();
					gameObjectMoveY = true;
				} else if(!gameObjectMoveY && worldx >= gameObjectSelected.getTransform().x && worldx < gameObjectSelected.getTransform().x + 45 &&
						worldy > gameObjectSelected.getTransform().y-15 && worldy < gameObjectSelected.getTransform().y+15)	//Horizontal
				{
					//gameObjectSelected.getTransform().x += Lookup.getInput().getMouseWorldDX();
					gameObjectMoveX = true;
				}
			}
			else
			{
				gameObjectMoveX = false;
				gameObjectMoveY = false;
			}
			
			/**If the editor is clicking an arrow, allow move the game object by the change in mouse position*/
			if(gameObjectMoveX)
			{
				if(transformSnap)
					gameObjectSelected.getTransform().x = Math.round(worldx / gridLineSpacingX) * gridLineSpacingX;
				else
					gameObjectSelected.getTransform().x += Lookup.getInput().getMouseDX() / editCamera.getTransform().scaleX;
			}
			else if(gameObjectMoveY)
			{
				if(transformSnap)
					gameObjectSelected.getTransform().y = Math.round(worldy / gridLineSpacingY) * gridLineSpacingY;
				else
					gameObjectSelected.getTransform().y += Lookup.getInput().getMouseDY() / editCamera.getTransform().scaleY;
			}
			
			
			
			
			resourceManager.update();
			
			drawScene();
			
			input.setLastPressed();
			
			Display.sync(60);
			Display.update();
		}
		closeScene();
	}
	
	/**Draws the game when being run in edit mode*/
	private final void drawScene()
	{
		scene.drawAsEditor(gameObjectSelected, doLighting);
		
		if(drawGrid)	//Draw grid lines
		{
			glPushMatrix();
			glLoadIdentity();
			glBegin(GL_LINES);
			float spacing = gridLineSpacingX * editCamera.getTransform().scaleX;
			for(float i = editCamera.getTransform().x - spacing - Display.getWidth()/2; i < Display.getWidth()/2; i += spacing)
			{
				glVertex2f(i, -Display.getHeight()/2);
				glVertex2f(i, Display.getHeight()/2);
			}
			spacing = gridLineSpacingY * editCamera.getTransform().scaleY;
			for(float i = editCamera.getTransform().y - spacing - Display.getHeight()/2; i < Display.getHeight()/2; i += spacing)
			{
				glVertex2f(-Display.getWidth()/2, i);
				glVertex2f(Display.getWidth()/2, i);
			}
			glEnd();
			glPopMatrix();
		}
	}
	
	/**Closes the game when being run in edit mode*/
	private final void closeScene()
	{
		resourceManager.close();
		Display.destroy();
		AL.destroy();
		System.exit(0);
	}
	
	
	
	
	
	
	
	
	
	/**Creates the jframe and the opengl context*/
	private final void createWindow()
	{
		window.setTitle("Scene Editor - " + fleProject.getName() + " - " + scene.getName());
		window.setSize(800, 600);
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		window.getContentPane().setBackground(WindowPreferences.getClrBackground());
		window.getContentPane().setForeground(WindowPreferences.getClrForeground());
		
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				File fleLastScene = new File(fleProject.getAbsolutePath()+"/last_scene_edited.txt");
				try {
					if(scene != null && (scene.getName().equals("") || scene.getName() == null))
					{
						scene.setName(JOptionPane.showInputDialog(window, "Please enter a name for the scene", "Unsaved Scene",
								JOptionPane.INFORMATION_MESSAGE).replaceAll(" ", File.pathSeparator));
					}
					if(JOptionPane.showConfirmDialog(window, "Would you like to save the current project?", "Save Project", JOptionPane.YES_NO_OPTION)
							== JOptionPane.YES_OPTION)
					{
						ProjectFileHandler.saveProject(scene, new File(fleProject.getAbsolutePath()), resourceManager, input);
					}
					running = false;
					FileWriter wrtLastScene = new FileWriter(fleLastScene);
					wrtLastScene.write(scene.getName());
					wrtLastScene.close();
					if(runAsGame)
						runAsGame = false;
				//	System.exit(0);
				} catch (IOException e1) {
					e1.printStackTrace();
					StringBuilder sb = new StringBuilder(e1.toString());
				    for (StackTraceElement ste : e1.getStackTrace()) {
				        sb.append("\n\tat ");
				        sb.append(ste);
				    }
				    String trace = sb.toString();
				    JOptionPane.showMessageDialog(window, trace, e1.toString(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	//	window.setVisible(true);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**Creates the menu bar at the top of the window*/
	private final void createMenuBar()
	{
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);	//Stops the canvas being drawn over the menus
		
		JMenuBar menuBar = new JMenuBar();
			JMenu mnuFile = new JMenu("File");
			{
				//JMenuItem itmNewProject = new JMenuItem("New Project");
				//JMenuItem itmOpenProject = new JMenuItem("Open Project");
				JMenuItem itmSaveProject = new JMenuItem("Save Project");
				//JMenuItem itmSaveProjectAs = new JMenuItem("Save Project As");
				JMenuItem itmNewScene = new JMenuItem("New Scene");
				JMenuItem itmOpenScene = new JMenuItem("Open Scene");
				JMenuItem itmSaveScene = new JMenuItem("Save Scene");
				//JMenuItem itmSaveSceneAs = new JMenuItem("Save Scene As");
				JMenu mnuImport = new JMenu("Import");
				JMenu mnuExport = new JMenu("Export");
				
				//Create import menu
				{
					JMenuItem itmImportImage = new JMenuItem("Image");
					JMenuItem itmImportSound = new JMenuItem("Sound");
					
					itmImportImage.addActionListener(e ->
					{
						JFileChooser chrImportImage = new JFileChooser("Import Image");
						chrImportImage.setMultiSelectionEnabled(true);
						int choice = chrImportImage.showDialog(null, "Import");
						if(choice == JFileChooser.APPROVE_OPTION)
						{
							File[] imgs = chrImportImage.getSelectedFiles();
							for(File fleImg : imgs)
							{
								String pthImage = fleImg.getAbsolutePath();
								pthImage.replaceAll(" ", File.pathSeparator);
								File fleImage = new File(pthImage);
								String name = fleImage.getName().replaceAll(" ", "_");
								String pthCopy = fleProject.getAbsolutePath() + "/res/images/" + name;
								pthCopy.replaceAll(" ", File.pathSeparator);
								File fleCopy = new File(pthCopy);
								if(!fleCopy.exists())
								{
									try {
										Files.copy(Paths.get(pthImage), Paths.get(pthCopy),
												new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES});
										resourceBrowser.updateBrowser(fleCopy.getAbsolutePath());
										resourceManager.addImportedTexturePath(fleCopy.getName());
									} catch (Exception e1) {
										e1.printStackTrace();
										StringBuilder sb = new StringBuilder(e1.toString());
									    for (StackTraceElement ste : e1.getStackTrace()) {
									        sb.append("\n\tat ");
									        sb.append(ste);
									    }
									    String trace = sb.toString();
									    JOptionPane.showMessageDialog(window, trace, e1.toString(), JOptionPane.ERROR_MESSAGE);
									}
								} else
								{
									int choice2 = JOptionPane.showConfirmDialog(window, "The file already exists. Would you like to replace it?");
									if(choice2 == JOptionPane.YES_OPTION)
									{
										try {
											File fleExistingFile = new File(pthCopy);
											fleExistingFile.delete();
											Files.copy(Paths.get(pthImage), Paths.get(pthCopy),
													new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES});
											resourceBrowser.updateBrowser(fleCopy.getAbsolutePath());
											resourceManager.addImportedTexturePath(fleCopy.getName());
										} catch (Exception e1) {
											e1.printStackTrace();
											StringBuilder sb = new StringBuilder(e1.toString());
										    for (StackTraceElement ste : e1.getStackTrace()) {
										        sb.append("\n\tat ");
										        sb.append(ste);
										    }
										    String trace = sb.toString();
										    JOptionPane.showMessageDialog(window, trace, e1.toString(), JOptionPane.ERROR_MESSAGE);
										}
									} else
									{
										resourceBrowser.updateBrowser(fleCopy.getAbsolutePath());
										resourceManager.addImportedTexturePath(fleCopy.getName());
									}
								}
							}
						}
					});
					itmImportSound.addActionListener(e ->
					{
						JFileChooser chrImportSound = new JFileChooser("Import Sound");
						chrImportSound.setMultiSelectionEnabled(true);
						int choice = chrImportSound.showDialog(null, "Import");
						if(choice == JFileChooser.APPROVE_OPTION)
						{
							File[] sounds = chrImportSound.getSelectedFiles();
							for(File fleSound : sounds)
							{
								String pthSound = fleSound.getAbsolutePath();
								pthSound.replaceAll(" ", File.pathSeparator);
								File fle = new File(pthSound);
								String name = fle.getName().replaceAll(" ", "_");
								String pthCopy = fleProject.getAbsolutePath() + "/res/sounds/" + name;
								pthCopy.replaceAll(" ", File.pathSeparator);
								File fleCopy = new File(pthCopy);
								if(!fleCopy.exists())
								{
									try {
										Files.copy(Paths.get(pthSound), Paths.get(pthCopy),
												new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES});
										resourceBrowser.updateBrowser(fleCopy.getAbsolutePath());
										resourceManager.addImportedAudioBufferPath(fleCopy.getName());
									} catch (Exception e1) {
										e1.printStackTrace();
										StringBuilder sb = new StringBuilder(e1.toString());
									    for(StackTraceElement ste : e1.getStackTrace())
									    {
									        sb.append("\n\tat ");
									        sb.append(ste);
									    }
									    String trace = sb.toString();
									    JOptionPane.showMessageDialog(window, trace, e1.toString(), JOptionPane.ERROR_MESSAGE);
									}
								} else
								{
									resourceBrowser.updateBrowser(fleCopy.getAbsolutePath());
									resourceManager.addImportedAudioBufferPath(fleCopy.getName());
								}
							}
						}
					});
					
					mnuImport.add(itmImportImage);
					mnuImport.add(itmImportSound);
				}
				
				
				
				//Create export menu
				{
					JMenuItem itmExportAsJar = new JMenuItem("Jar");
					
					itmExportAsJar.addActionListener(e ->
					{
						try {
							exportAsJar();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					});
					
					mnuExport.add(itmExportAsJar);
				}
				
				itmOpenScene.addActionListener(e ->
				{
					//JOptionPane.showMessageDialog(window, "Feature not currently available", "Feature Unavailable", JOptionPane.INFORMATION_MESSAGE);
					ProjectFileHandler.saveScene(scene, fleProject, resourceManager);
					
					JFileChooser chrOpenScene = new JFileChooser("Choose Scene");
					chrOpenScene.setCurrentDirectory(new File(fleProject.getAbsolutePath() + "/scenes"));
					int choice = chrOpenScene.showDialog(null, "Open");
					if(choice == JFileChooser.APPROVE_OPTION)
					{
						gameObjectSelected = null;
						scene = new Scene();
						input = new InputHandler();
						resourceManager = new ResourceManager(fleProject);
						ProjectFileHandler.openProject(scene, fleProject, chrOpenScene.getSelectedFile(), resourceManager, input);
						scene.initAsEditor();
						editCamera = new DragCamera(0, 0, 0);
						scene.setCamera(editCamera);
						//ProjectFileHandler.saveScene(scene, fleProject, resourceManager);
						createGameObjectViewer();
						createGameObjectInspector(null);
						createConsole();
						createDocumentationPanel();
						createAnimationEditor(null);
						createCodeEditor(null);
					}
				});
				
				itmNewScene.addActionListener(e ->
				{
					//JOptionPane.showMessageDialog(window, "Feature not currently available", "Feature Unavailable", JOptionPane.INFORMATION_MESSAGE);
					ProjectFileHandler.saveScene(scene, fleProject, resourceManager);
					String sceneName = JOptionPane.showInputDialog(window, "Choose a name for the new scene", "Name Scene", JOptionPane.INFORMATION_MESSAGE);
					File fleNewScene = new File(fleProject.getAbsolutePath() + ".jps");
					if(!fleNewScene.exists())
					{
						try {
							gameObjectSelected = null;
							fleNewScene.createNewFile();
							//resourceManager = new ResourceManager(fleProject);
							scene = new Scene();
							scene.initAsEditor();
							scene.setName(sceneName);
							editCamera = new DragCamera(0, 0, 0);
							scene.setCamera(editCamera);
							ProjectFileHandler.saveScene(scene, fleProject, resourceManager);
							createGameObjectViewer();
							createGameObjectInspector(null);
							createConsole();
							createDocumentationPanel();
							createAnimationEditor(null);
							createCodeEditor(null);
							pnlGameObjectInspector.revalidate();
							pnlGameObjectInspector.repaint();
							window.revalidate();
							window.repaint();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				});
				
				itmSaveProject.addActionListener(e ->
				{
					if(scene.getName().equals("") || scene.getName() == null)
						scene.setName(JOptionPane.showInputDialog(window, "Please enter a name for the scene", "Unsaved Scene",
								JOptionPane.INFORMATION_MESSAGE).replaceAll(" ", File.pathSeparator));
					ProjectFileHandler.saveProject(scene, fleProject, resourceManager, input);
				});
				
				itmSaveScene.addActionListener(e ->
				{
					if(scene.getName().equals("") || scene.getName() == null)
						scene.setName(JOptionPane.showInputDialog(window, "Please enter a name for the scene", "Unsaved Scene",
								JOptionPane.INFORMATION_MESSAGE).replaceAll(" ", File.pathSeparator));
					ProjectFileHandler.saveScene(scene, fleProject, resourceManager);
				});
				
			//	mnuFile.add(itmNewProject);
			//	mnuFile.add(itmOpenProject);
				mnuFile.add(itmSaveProject);
			//	mnuFile.add(itmSaveProjectAs);
				mnuFile.addSeparator();
				mnuFile.add(itmNewScene);
				mnuFile.add(itmOpenScene);
				mnuFile.add(itmSaveScene);
			//	mnuFile.add(itmSaveSceneAs);
				mnuFile.addSeparator();
				mnuFile.add(mnuImport);
				mnuFile.add(mnuExport);
			}
			menuBar.add(mnuFile);
		
			JMenu mnuAdd = new JMenu("Add");
			{
				JMenuItem itmAddGameObject = new JMenuItem("Game Object");
			//	JMenuItem itmAddComponent = new JMenuItem("Component");
				
				itmAddGameObject.addActionListener(e ->
				{
					GameObject newGameObject = new GameObject();
					newGameObject.initAsEditor();
					generateUniqueGameObjectName(newGameObject, "New_Game_Object");
					
					lstModelGameObject.addElement(newGameObject);
					scene.addGameObject(newGameObject);
				});
				
				mnuAdd.add(itmAddGameObject);
			//	mnuAdd.add(itmAddComponent);
			}
			menuBar.add(mnuAdd);
			
			JMenu mnuSettings = new JMenu("Settings");
			{
				//JMenuItem itmCamera = new JMenuItem("Camera");
				JMenuItem itmInput = new JMenuItem("Input");
				//JMenuItem itmNetworking = new JMenuItem("Networking");
				
				itmInput.addActionListener(e ->
				{
					JDialog dlgInput = new JDialog(window);
					dlgInput.setLayout(new BorderLayout());
					dlgInput.setTitle("Input");
					dlgInput.setSize(600, 450);
					dlgInput.setBackground(WindowPreferences.getClrBackground());
					
					JPanel pnlInput = new JPanel(new GridLayout(0, 1));
					
					for(KeyboardHandler.KeyBinding key : input.getKeyBindings())
					{
						JPanel pnlKey = new JPanel(new GridLayout(1, 0));
						JLabel lblKey = new JLabel(key.getName());
						JTextField txtKey = new JTextField(Keyboard.getKeyName(key.getID()));
						
						txtKey.addKeyListener(new KeyListener()
						{
							public void keyPressed(KeyEvent e1){}
							public void keyReleased(KeyEvent e1)
							{
								txtKey.setText(Keyboard.getKeyName(Keyboard.getKeyIndex(String.valueOf(KeyEvent.getKeyText(e1.getKeyCode()).toUpperCase()))));
								input.setKeyBinding1(key.getName(), Keyboard.getKeyIndex(String.valueOf(KeyEvent.getKeyText(e1.getKeyCode()).toUpperCase())));
							}
							public void keyTyped(KeyEvent e1) {}
						});
						
						pnlKey.addMouseListener(new MouseAdapter()
						{
							public void mousePressed(MouseEvent e1)
							{
								if(e1.isPopupTrigger())
								{
									showMenu(e1);
								}
							}
							
							public void mouseReleased(MouseEvent e1)
							{
								if(e1.isPopupTrigger())
								{
									showMenu(e1);
								}
							}
							
							private void showMenu(MouseEvent e1)
							{
								JPopupMenu mnuPopup = new JPopupMenu();
								JMenuItem itmDelete = new JMenuItem("Delete");
								
								itmDelete.addActionListener(e2 ->
								{
									if(JOptionPane.showConfirmDialog(window, "Are you sure you want to delete this input method?") == JOptionPane.YES_OPTION)
									{
										input.removeKeyBinding(lblKey.getText());
										dlgInput.setEnabled(false);
										dlgInput.setVisible(false);
										itmInput.doClick();
									}
								});
								
								mnuPopup.add(itmDelete);
								
								try
								{
									mnuPopup.show(pnlKey, e1.getX(), e1.getY());
								} catch(NullPointerException e)
								{
									System.err.println("Tried to show menu for unselected input method");
									StringBuilder sb = new StringBuilder(e.toString());
								    for (StackTraceElement ste : e.getStackTrace()) {
								        sb.append("\n\tat ");
								        sb.append(ste);
								    }
								    String trace = sb.toString();
								    JOptionPane.showMessageDialog(window, trace, e.toString(), JOptionPane.ERROR_MESSAGE);
								}
							}
						});
						
						pnlKey.add(lblKey);
						pnlKey.add(txtKey);
						
						pnlInput.add(pnlKey);
					}
					
					JButton btnAddInput = new JButton("Add Input");
					btnAddInput.addActionListener(e1 ->
					{
						String name = JOptionPane.showInputDialog(dlgInput, "Name:", "Name Input", JOptionPane.INFORMATION_MESSAGE);
						if(name != null && name.trim() != "")
						{
							input.addKeyBinding(name, 0);
							dlgInput.setEnabled(false);
							dlgInput.setVisible(false);
							itmInput.doClick();
						}
					});
					pnlInput.add(btnAddInput);
					
					dlgInput.add(pnlInput);
					dlgInput.setVisible(true);
				});
				
				//mnuSettings.add(itmCamera);
				mnuSettings.add(itmInput);
				//mnuSettings.add(itmNetworking);
			}
			menuBar.add(mnuSettings);
			
			JMenu mnuUI = new JMenu("GUI");	//Options for creating a user interface
			{
				JMenuItem itmOpenGUIView = new JMenuItem("Open GUI View");
				itmOpenGUIView.addActionListener(e ->
				{
					JPanel guiToolbox = createGUIToolbox();
					pnlRight.addTab("GUI Toolbox", guiToolbox);	//Add the GUI toolbox panel to the right tabbed panel
					pnlRight.setSelectedComponent(guiToolbox);
					
					JPanel guiEditor = new JPanel();
					pnlCentre.addTab("GUI Editor", guiEditor);
					pnlCentre.setSelectedComponent(guiEditor);
				});
				mnuUI.add(itmOpenGUIView);
			}
			menuBar.add(mnuUI);
			
			JMenu mnuAbout = new JMenu("About");
			{
				JMenuItem itmAbout = new JMenuItem("About");
				itmAbout.addActionListener(e ->
				{
					JOptionPane.showMessageDialog(window, "Origami Sheep Engine v1.0.\nCreated by and property of James Sugden.", "About", JOptionPane.INFORMATION_MESSAGE);
				});
				mnuAbout.add(itmAbout);
			}
			menuBar.add(mnuAbout);
		
		window.setJMenuBar(menuBar);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**Creates the left panel which displays all of the game objects in the scene*/
	private final void createGameObjectViewer()
	{
		lstModelGameObject.clear();
		System.out.println("Created game object viewer");
		
		for(GameObject o : scene.getGameObjects())
		{
			lstModelGameObject.addElement(o);
			System.out.println(scene.getName() + " " + o.getName());
		}
				
		lstGameObject.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					showMenu();
				} else
				{
					if(lstGameObject.isEnabled())
					{
						gameObjectSelected = lstGameObject.getSelectedValue();
						createGameObjectInspector(lstGameObject.getSelectedValue());
					}
				}
			}
			
			public void mouseReleased(MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					showMenu();
				}
				{
					if(lstGameObject.isEnabled())
					{
						gameObjectSelected = lstGameObject.getSelectedValue();
						createGameObjectInspector(lstGameObject.getSelectedValue());
					}
				}
			}
			
			private void showMenu()
			{
				JPopupMenu mnuPopup = new JPopupMenu();
				JMenuItem itmDuplicate = new JMenuItem("Duplicate");
				//JMenuItem itmCopy = new JMenuItem("Copy");
				JMenuItem itmRename = new JMenuItem("Rename");
				JMenuItem itmDelete = new JMenuItem("Delete");
				
				itmDuplicate.addActionListener(e ->
				{
					GameObject duplicate = new GameObject(lstGameObject.getSelectedValue());
					generateUniqueGameObjectName(duplicate, duplicate.getName());
					duplicate.getTransform().x += 3;
					duplicate.getTransform().y += 3;
					lstModelGameObject.addElement(duplicate);
					scene.addGameObject(duplicate);
				});
				itmRename.addActionListener(e ->
				{
					String newName;
					if((newName = JOptionPane.showInputDialog(window, "Enter a new name", "Rename", JOptionPane.DEFAULT_OPTION)) != null)
					{
						generateUniqueGameObjectName(lstGameObject.getSelectedValue(), newName);
						createGameObjectInspector(lstGameObject.getSelectedValue());
					}
				});
				itmDelete.addActionListener(e ->
				{
					if(JOptionPane.showConfirmDialog(window, "Are you sure you want to delete this game object?") == JOptionPane.YES_OPTION)
					{
						GameObject gameObject = lstGameObject.getSelectedValue();
						lstModelGameObject.remove(lstGameObject.getSelectedIndex());
						scene.removeGameObject(gameObject);
						createGameObjectInspector(null);
					}
				});
				
				mnuPopup.add(itmDuplicate);
				//mnuPopup.add(itmCopy);
				mnuPopup.add(itmRename);
				mnuPopup.add(itmDelete);
				
				try
				{
					mnuPopup.show(lstGameObject, 5, lstGameObject.getCellBounds(lstGameObject.getSelectedIndex(), lstGameObject.getSelectedIndex()).y);
				} catch(NullPointerException e)
				{
					System.err.println("Tried to show menu for unselected game object");
					StringBuilder sb = new StringBuilder(e.toString());
				    for (StackTraceElement ste : e.getStackTrace()) {
				        sb.append("\n\tat ");
				        sb.append(ste);
				    }
				    String trace = sb.toString();
				    JOptionPane.showMessageDialog(window, trace, e.toString(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**Creates the right panel which displays the components of the selected game object*/
	private final void createGameObjectInspector(GameObject gameObject)
	{
		pnlGameObjectInspector.removeAll();
		pnlGameObjectInspector.setBackground(WindowPreferences.getClrBackground());
		if(gameObject != null)
		{
			createAnimationEditor(gameObject);
			createCodeEditor(gameObject);
			
			Border bdrPanel = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			
			JPanel pnlName = new JPanel(new GridLayout(0, 2));
			pnlName.setBorder(bdrPanel);
			pnlName.setBackground(WindowPreferences.getClrBackground());
			JTextField txtName = new JTextField(gameObject.getName());
			JTextField txtTag = new JTextField(gameObject.getTag());
			JTextField txtPrefab = new JTextField(gameObject.getPrefab());
			JButton btnSaveToPrefab = new JButton("Save to Prefab");
				btnSaveToPrefab.setToolTipText("Saves the current game object as the prefab specified");
			JPanel pnlPrefab = new JPanel(new GridLayout(1, 2));
				pnlPrefab.add(txtPrefab);
				pnlPrefab.add(btnSaveToPrefab);
			
			txtName.addKeyListener(new KeyListener()	//Renaming the game object
			{
				public void keyReleased(KeyEvent e)
				{
					generateUniqueGameObjectName(gameObject, txtName.getText());
				}
				public void keyPressed(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			txtTag.addKeyListener(new KeyListener()		//Setting the tag of the game object
			{
				public void keyReleased(KeyEvent e)
				{
					gameObject.setTag(txtTag.getText());
				}
				public void keyPressed(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			txtPrefab.addKeyListener(new KeyListener()	//Changing the game object's prefab file, blank field results in no prefab field
			{
				public void keyReleased(KeyEvent e)
				{
					for(File file : ProjectFileHandler.findImportedResourcesInDir(fleProject.getAbsolutePath() + "/res/prefabs"))
					{
						if(file.getName().equals(txtPrefab.getText() + ".jpo"))	//Test if prefab is in use and if it is then don't use it
						{
							JOptionPane.showMessageDialog(window, "Prefab file already exists");
							return;
						}
					}
					gameObject.setPrefab(txtPrefab.getText());
				}
				public void keyPressed(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			btnSaveToPrefab.addActionListener(e ->		//Saves the changes made to the game object to the prefab file
			{
				if(gameObject.getPrefab() != "" && gameObject.getPrefab() != null)
					ProjectFileHandler.saveGameObjectPrefab(gameObject, fleProject, resourceManager);
			});
			
			pnlName.add(new JLabel("Name:"));
			pnlName.add(txtName);
			pnlName.add(new JLabel("Tag:"));
			pnlName.add(txtTag);
			pnlName.add(new JLabel("Prefab:"));
			pnlName.add(pnlPrefab);
			pnlGameObjectInspector.add(pnlName);
			
			JPanel pnlTransform = new JPanel();
			pnlTransform.setBorder(BorderFactory.createTitledBorder(bdrPanel, "Transform",
					TitledBorder.CENTER, TitledBorder.TOP,new Font("times new roman", Font.BOLD, 16), Color.BLACK));
			pnlTransform.setBackground(WindowPreferences.getClrBackground());
			GridLayout lytTransform = new GridLayout(3, 2);
			pnlTransform.setLayout(lytTransform);
			{
				JPanel pnlPos = new JPanel();
					GridLayout lytPos = new GridLayout(1, 0);
				//	lytPos.setHgap(20);
					pnlPos.setLayout(lytPos);
				SpinnerNumberModel mdlX = new SpinnerNumberModel();
					mdlX.setValue(gameObject.getTransform().x);
					JSpinner spnX = new JSpinner(mdlX);
				SpinnerNumberModel mdlY = new SpinnerNumberModel();
					mdlY.setValue(gameObject.getTransform().y);
					JSpinner spnY = new JSpinner(mdlY);
				JLabel lblPos = new JLabel("Position (X, Y):");
					lblPos.setToolTipText("The on-screen co-ordinates of the game object");
				pnlPos.add(spnX);
				pnlPos.add(spnY);
				
				JPanel pnlRotation = new JPanel();
					GridLayout lytRotation = new GridLayout(1, 0);
				//	lytRotation.setHgap(10);
					pnlRotation.setLayout(lytRotation);
				SpinnerNumberModel mdlRotation = new SpinnerNumberModel();
					mdlRotation.setValue(gameObject.getTransform().rotation);
					JSpinner spnRotation = new JSpinner(mdlRotation);
				JLabel lblRotation = new JLabel("Rotation:");
					lblRotation.setToolTipText("The on-screen rotation (degrees) of the game object");
				pnlRotation.add(spnRotation);
				
				JPanel pnlScale = new JPanel();
					GridLayout lytScale = new GridLayout(1, 0);
				//	lytScale.setHgap(20);
					pnlScale.setLayout(lytScale);
				SpinnerNumberModel mdlScaleX = new SpinnerNumberModel(gameObject.getTransform().scaleX, -25, 25, 0.25f);
					JSpinner spnScaleX = new JSpinner(mdlScaleX);
				SpinnerNumberModel mdlScaleY = new SpinnerNumberModel(gameObject.getTransform().scaleY, -25, 25, 0.25f);
					JSpinner spnScaleY = new JSpinner(mdlScaleY);
				JLabel lblScale = new JLabel("Scale (X, Y):");
					lblScale.setToolTipText("The on-screen scale of the game object");
				pnlScale.add(spnScaleX);
				pnlScale.add(spnScaleY);
				
				mdlX.addChangeListener(e ->
				{
					gameObject.getTransform().x = mdlX.getNumber().floatValue();
				});
				mdlY.addChangeListener(e ->
				{
					gameObject.getTransform().y = mdlY.getNumber().floatValue();
				});
				mdlRotation.addChangeListener(e ->
				{
					gameObject.getTransform().rotation = mdlRotation.getNumber().floatValue();
				});
				mdlScaleX.addChangeListener(e ->
				{
					gameObject.getTransform().scaleX = mdlScaleX.getNumber().floatValue();
				});
				mdlScaleY.addChangeListener(e ->
				{
					gameObject.getTransform().scaleY = mdlScaleY.getNumber().floatValue();
				});
				
				pnlTransform.add(lblPos);
				pnlTransform.add(pnlPos);
				pnlTransform.add(lblRotation);
				pnlTransform.add(pnlRotation);
				pnlTransform.add(lblScale);
				pnlTransform.add(pnlScale);
			}
			pnlGameObjectInspector.add(pnlTransform);
			
			JPanel pnlDepth = new JPanel(new GridLayout(1, 0));
			pnlDepth.setBorder(BorderFactory.createTitledBorder(bdrPanel, "Depth",
					TitledBorder.CENTER, TitledBorder.TOP,new Font("times new roman", Font.BOLD, 16), Color.BLACK));
			pnlDepth.setBackground(WindowPreferences.getClrBackground());
			GridLayout lytDepth = new GridLayout(1, 3);
			pnlDepth.setLayout(lytDepth);
			{
				JCheckBox chkUseDepthValue = new JCheckBox();
					chkUseDepthValue.setSelected(gameObject.getUseDepthValue());
					chkUseDepthValue.setToolTipText("If ticked, the depth value will be the user set depth value, else, one will be generated at runtime");
				JLabel lblDepthValue = new JLabel("Depth Value:");
					lblDepthValue.setToolTipText("Allows a discrete z buffer to be set for the image");
				SpinnerNumberModel mdlDepthValue = new SpinnerNumberModel();
					mdlDepthValue.setValue(gameObject.getDepthValue());
					mdlDepthValue.setStepSize(0.05f);
					mdlDepthValue.setMaximum(1.0f);
					mdlDepthValue.setMinimum(0.0f);
					JSpinner spnDepthValue = new JSpinner(mdlDepthValue);
					spnDepthValue.setEnabled(gameObject.getUseDepthValue());
				
				chkUseDepthValue.addActionListener(e ->
				{
					gameObject.setUseDepthValue(chkUseDepthValue.isSelected());
					spnDepthValue.setEnabled(gameObject.getUseDepthValue());
				});
				
				mdlDepthValue.addChangeListener(e ->
				{
					gameObject.setDepthValue(mdlDepthValue.getNumber().floatValue());
				});
				
				pnlDepth.add(lblDepthValue);
				pnlDepth.add(chkUseDepthValue);
				pnlDepth.add(spnDepthValue);
			}
			styleAllComponents(pnlDepth);
			pnlGameObjectInspector.add(pnlDepth);
			
			
			for(Component component : gameObject.getComponents())
			{
				JPanel pnlComponent = new JPanel();
					pnlComponent.setBackground(WindowPreferences.getClrBackground());
					GridLayout lytComponent = new GridLayout(0, 2);
				//	GridBagLayout lytComponent = new GridBagLayout();
				//	GridBagConstraints lytConstraints = new GridBagConstraints();
				//	lytConstraints.insets = new Insets(0, 0, 5, 0);
				//	lytConstraints.anchor = GridBagConstraints.NORTH;
				//	lytConstraints.weighty = 1;
					
					//lytComponent.setVgap(10);
					pnlComponent.setLayout(lytComponent);
				
				EEventObject eventObj = new EEventObject();	//Used so components can send ui update requests without knowing about the editor
				eventObj.setListener(e ->
				{
					switch(e)
					{
					case OPEN_ANIMATION_EDITOR:
						createAnimationEditor(gameObject);
						pnlCentre.addTab("Animation Editor", pnlAnimationEditor);
						pnlCentre.setSelectedComponent(pnlAnimationEditor);
						break;
					case OPEN_DOCUMENTATION:
						createDocumentationPanel();
						pnlBottom.addTab("Documentation", sclDocumentationPanel);
						pnlBottom.setSelectedComponent(sclDocumentationPanel);
						break;
					case OPEN_CODE_EDITOR:
						createCodeEditor(gameObject);
						pnlCentre.addTab("Code Editor", pnlCodeEditor);
						pnlCentre.setSelectedComponent(pnlCodeEditor);
						break;
					}
				});
				
				component.createEditingPanel(pnlComponent, null, resourceManager, eventObj);
				
				pnlComponent.setBorder(BorderFactory.createTitledBorder(bdrPanel, component.getTypeName() + " - " + component.getIdentificationName(),
						TitledBorder.CENTER, TitledBorder.TOP, new Font("times new roman", Font.BOLD, 16), Color.BLACK));
				
				styleAllComponents(pnlComponent);
				
				
				pnlComponent.addMouseListener(new MouseAdapter()
				{
					public void mousePressed(MouseEvent e)
					{
						if(e.isPopupTrigger())
						{
							showMenu(e);
						}
					}
					
					public void mouseReleased(MouseEvent e)
					{
						if(e.isPopupTrigger())
						{
							showMenu(e);
						}
					}
					
					private void showMenu(MouseEvent e)
					{
						JPopupMenu mnuPopup = new JPopupMenu();
						JMenuItem itmRename = new JMenuItem("Rename");
						JMenuItem itmDelete = new JMenuItem("Delete");
						
						itmRename.addActionListener(e1 ->
						{
							String newName;
							if((newName = JOptionPane.showInputDialog(window, "Enter a new name", "Rename", JOptionPane.DEFAULT_OPTION)) != null)
							{
								generateUniqueComponentName(gameObject, component, newName);
								createGameObjectInspector(gameObject);
							}
						});
						itmDelete.addActionListener(e1 ->
						{
							if(JOptionPane.showConfirmDialog(window, "Are you sure you want to remove this component") == JOptionPane.YES_OPTION)
							{
								if(component instanceof AnimationRenderer)
								{
									for(Component c : gameObject.getComponents())
									{
										if(c instanceof Animator)
											((Animator)c).removeAnimation(((AnimationRenderer)component));
									}
								}
								gameObject.removeComponent(component);
								createGameObjectInspector(gameObject);
							}
						});
						
						if(component instanceof Camera)
						{
							JMenuItem itmSetMainCamera = new JMenuItem("Set As Main Camera");
							
							itmSetMainCamera.addActionListener(e1 ->
							{
								defaultGameCamera = (Camera)component;
							});
							
							mnuPopup.add(itmSetMainCamera);
						}
						
						mnuPopup.add(itmRename);
						mnuPopup.add(itmDelete);
						
						try
						{
							mnuPopup.show(pnlComponent, e.getX(), e.getY());
						} catch(NullPointerException e1)
						{
							System.err.println("Tried to show menu for unselected game object");
							StringBuilder sb = new StringBuilder(e1.toString());
						    for (StackTraceElement ste : e1.getStackTrace()) {
						        sb.append("\n\tat ");
						        sb.append(ste);
						    }
						    String trace = sb.toString();
						    JOptionPane.showMessageDialog(window, trace, e1.toString(), JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				
				pnlGameObjectInspector.add(pnlComponent);
			}
			
			
			
			JPanel pnlAddComponent = new JPanel(new GridLayout(0, 1));
			pnlAddComponent.setBackground(WindowPreferences.getClrBackground());
			pnlAddComponent.setBorder(BorderFactory.createTitledBorder(bdrPanel, "Add Component",
			TitledBorder.CENTER, TitledBorder.TOP, new Font("times new roman", Font.BOLD, 16), Color.BLACK));
			
			JButton btnRendering = new JButton("Rendering");
			JButton btnAnimation = new JButton("Animation");
			JButton btnPhysics = new JButton("Physics");
			JButton btnAudio = new JButton("Audio");
			JButton btnLights = new JButton("Lights");
			JButton btnScripts = new JButton("Scripts");
			JButton btnCameras = new JButton("Cameras");
			
			JButton btnBack = new JButton("Back");	//When in a sub menu allows returns to main menu
			btnBack.addActionListener(e ->
			{
				pnlAddComponent.removeAll();
				pnlAddComponent.add(btnRendering);
				pnlAddComponent.add(btnAnimation);
				pnlAddComponent.add(btnPhysics);
				pnlAddComponent.add(btnAudio);
				pnlAddComponent.add(btnLights);
				pnlAddComponent.add(btnScripts);
				pnlAddComponent.add(btnCameras);
				
				pnlAddComponent.revalidate();
				pnlAddComponent.repaint();
			});
			
			btnRendering.addActionListener(e1 ->
			{
				JButton btnImageRenderer = new JButton("Image Renderer");
				JButton btnParticleEmitter = new JButton("Particle Emitter");
				
				btnImageRenderer.addActionListener(e2 ->
				{
					pnlAddComponent.removeAll();
					ImageRenderer r = new ImageRenderer();
					generateUniqueComponentName(gameObject, r, "new_image_renderer");
					gameObject.addComponent(r);
					createGameObjectInspector(gameObject);
					pnlAddComponent.revalidate();
					pnlAddComponent.repaint();
				});
				btnParticleEmitter.addActionListener(e2 ->
				{
					pnlAddComponent.removeAll();
					ParticleEmitter p = new ParticleEmitter();
					generateUniqueComponentName(gameObject, p, "new_particle_emitter");
					gameObject.addComponent(p);
					createGameObjectInspector(gameObject);
					pnlAddComponent.revalidate();
					pnlAddComponent.repaint();
				});
				
				pnlAddComponent.removeAll();
				pnlAddComponent.add(btnImageRenderer);
				pnlAddComponent.add(btnParticleEmitter);
				pnlAddComponent.add(btnBack);
				pnlAddComponent.revalidate();
				pnlAddComponent.repaint();
			});
			btnAnimation.addActionListener(e1 ->
			{
				JButton btnRenderer = new JButton("Animation");
				JButton btnAnimator = new JButton("Animator");
				
				btnRenderer.addActionListener(e2 ->
				{
					pnlAddComponent.removeAll();
					AnimationRenderer r = new AnimationRenderer();
					generateUniqueComponentName(gameObject, r, "new_animation_renderer");
					for(Component c : gameObject.getComponents())
					{
						if(c instanceof Animator)
						{
							((Animator)c).addAnimation(r);
							break;
						}
					}
					gameObject.addComponent(r);
					createGameObjectInspector(gameObject);
					pnlAddComponent.revalidate();
					pnlAddComponent.repaint();
				});
				btnAnimator.addActionListener(e2 ->
				{
					boolean hasAnimator = false;
					ArrayList<AnimationRenderer> animations = new ArrayList<>();
					for(Component c : gameObject.getComponents())
					{
						if(c instanceof Animator)
							hasAnimator = true;
						else if(c instanceof AnimationRenderer)
							animations.add((AnimationRenderer)c);
					}
					if(!hasAnimator)
					{
						pnlAddComponent.removeAll();
						Animator a = new Animator();
						generateUniqueComponentName(gameObject, a, "new_animator");
						for(AnimationRenderer animation : animations)
						{
							a.addAnimation(animation);
						}
						gameObject.addComponent(a);
						createGameObjectInspector(gameObject);
						pnlAddComponent.revalidate();
						pnlAddComponent.repaint();
					} else
						JOptionPane.showMessageDialog(window, "Each game object can only contain one animator", "Animator Limit Reached", JOptionPane.INFORMATION_MESSAGE);
				});
				
				pnlAddComponent.removeAll();
				pnlAddComponent.add(btnRenderer);
				pnlAddComponent.add(btnAnimator);
				pnlAddComponent.add(btnBack);
				pnlAddComponent.revalidate();
				pnlAddComponent.repaint();
			});
			btnPhysics.addActionListener(e1 ->
			{
				JButton btnRectCollider = new JButton("Rect Collider");
				JButton btnCircleCollider = new JButton("Circle Collider");
				//JButton btnLineCollider = new JButton("Line Collider");
				//JButton btnPathCollider = new JButton("Path Collider");
				
				btnRectCollider.addActionListener(e2 ->
				{
					pnlAddComponent.removeAll();
					RectCollider r = new RectCollider();
					generateUniqueComponentName(gameObject, r, "new_rect_collider");
					gameObject.addComponent(r);
					createGameObjectInspector(gameObject);
					pnlAddComponent.revalidate();
					pnlAddComponent.repaint();
				});
				btnCircleCollider.addActionListener(e2 ->
				{
					pnlAddComponent.removeAll();
					CircleCollider r = new CircleCollider();
					generateUniqueComponentName(gameObject, r, "new_circle_collider");
					gameObject.addComponent(r);
					createGameObjectInspector(gameObject);
					pnlAddComponent.revalidate();
					pnlAddComponent.repaint();
				});
			/*	btnLineCollider.addActionListener(e2 ->
				{
					pnlAddComponent.removeAll();
					RectCollider r = new RectCollider();
					generateUniqueComponentName(gameObject, r, "new_line_collider");
					gameObject.addComponent(r);
					createGameObjectInspector(gameObject);
					pnlAddComponent.revalidate();
					pnlAddComponent.repaint();
				});
				btnPathCollider.addActionListener(e2 ->
				{
					pnlAddComponent.removeAll();
					RectCollider r = new RectCollider();
					generateUniqueComponentName(gameObject, r, "new_path_collider");
					gameObject.addComponent(r);
					createGameObjectInspector(gameObject);
					pnlAddComponent.revalidate();
					pnlAddComponent.repaint();
				});*/
				
				pnlAddComponent.removeAll();
				pnlAddComponent.add(btnRectCollider);
				pnlAddComponent.add(btnCircleCollider);
				//pnlAddComponent.add(btnLineCollider);
				//pnlAddComponent.add(btnPathCollider);
				pnlAddComponent.add(btnBack);
				pnlAddComponent.revalidate();
				pnlAddComponent.repaint();
			});
			btnAudio.addActionListener(e1 ->
			{
				JButton btnAudioSource = new JButton("Audio Source");
				JButton btnAudioListener = new JButton("Audio Listener");
				
				btnAudioSource.addActionListener(e2 ->
				{
					pnlAddComponent.removeAll();
					AudioSource s = new AudioSource();
					generateUniqueComponentName(gameObject, s, "new_audio_source");
					gameObject.addComponent(s);
					createGameObjectInspector(gameObject);
					pnlAddComponent.revalidate();
					pnlAddComponent.repaint();
				});
				btnAudioListener.addActionListener(e2 ->
				{
					pnlAddComponent.removeAll();
					AudioListener l = new AudioListener();
					generateUniqueComponentName(gameObject, l, "new_audio_listener");
					gameObject.addComponent(l);
					createGameObjectInspector(gameObject);
					pnlAddComponent.revalidate();
					pnlAddComponent.repaint();
				});
				
				pnlAddComponent.removeAll();
				pnlAddComponent.add(btnAudioSource);
				pnlAddComponent.add(btnAudioListener);
				pnlAddComponent.add(btnBack);
				pnlAddComponent.revalidate();
				pnlAddComponent.repaint();
			});
			btnLights.addActionListener(e ->
			{
				JButton btnPointLight = new JButton("Point Light");
				JButton btnSpotLight = new JButton("Spot Light");
				
				btnPointLight.addActionListener(e2 ->
				{
					pnlAddComponent.removeAll();
					PointLight l = new PointLight();
					generateUniqueComponentName(gameObject, l, "new_point_light");
					gameObject.addComponent(l);
					createGameObjectInspector(gameObject);
					pnlAddComponent.revalidate();
					pnlAddComponent.repaint();
				});
				btnSpotLight.addActionListener(e2 ->
				{
					pnlAddComponent.removeAll();
					SpotLight l = new SpotLight();
					generateUniqueComponentName(gameObject, l, "new_spot_light");
					gameObject.addComponent(l);
					createGameObjectInspector(gameObject);
					pnlAddComponent.revalidate();
					pnlAddComponent.repaint();
				});
				
				pnlAddComponent.removeAll();
				pnlAddComponent.add(btnPointLight);
				pnlAddComponent.add(btnSpotLight);
				pnlAddComponent.add(btnBack);
				pnlAddComponent.revalidate();
				pnlAddComponent.repaint();
			});
			btnScripts.addActionListener(e1 ->
			{
				pnlAddComponent.removeAll();
				Script s = new Script();
				generateUniqueComponentName(gameObject, s, "new_script");
				gameObject.addComponent(s);
				createGameObjectInspector(gameObject);
				pnlAddComponent.revalidate();
				pnlAddComponent.repaint();
			});
			btnCameras.addActionListener(e2 ->
			{
				FollowCamera a = new FollowCamera(0, 0, 0);
				generateUniqueComponentName(gameObject, a, "new_follow_camera");
				gameObject.addComponent(a);
				createGameObjectInspector(gameObject);
				pnlAddComponent.revalidate();
				pnlAddComponent.repaint();
			});
			
			pnlAddComponent.removeAll();
			pnlAddComponent.add(btnRendering);
			pnlAddComponent.add(btnAnimation);
			pnlAddComponent.add(btnPhysics);
			pnlAddComponent.add(btnAudio);
			pnlAddComponent.add(btnLights);
			pnlAddComponent.add(btnScripts);
			pnlAddComponent.add(btnCameras);
			
			pnlAddComponent.revalidate();
			pnlAddComponent.repaint();
			
			pnlGameObjectInspector.add(pnlAddComponent);
			pnlGameObjectInspector.revalidate();
			pnlGameObjectInspector.repaint();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/**Creates the bottom panel which shows files in the current project*/
	private final void createFileBrowser()
	{
		resourceBrowser = new ResourceBrowser(fleProject.getAbsolutePath() + "/res");
	}
	
	private final void createConsole()
	{
		JTextArea txtConsole = new JTextArea();
		
		System.setOut(new PrintStreamCapturer(txtConsole, System.out));
		System.setErr(new PrintStreamCapturer(txtConsole, System.err, "[ERROR] "));
		
		pnlConsole.add(txtConsole, BorderLayout.CENTER);
	}
	
	private final void createDocumentationPanel()
	{
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Documentation");
		{
			DefaultMutableTreeNode lookupNode = new DefaultMutableTreeNode("james.sugden.engine.Lookup");
			{
				DefaultMutableTreeNode getSceneNode = new DefaultMutableTreeNode("Scene getScene()");
				DefaultMutableTreeNode getGameNode = new DefaultMutableTreeNode("Game getGame()");
				DefaultMutableTreeNode getInputNode = new DefaultMutableTreeNode("InputHandler getInput()");
				DefaultMutableTreeNode getRandomNode = new DefaultMutableTreeNode("Random getRandom()");
				DefaultMutableTreeNode getGameObjectWithNameNode = new DefaultMutableTreeNode("GameObject getGameObjectWithName(String name)");
				DefaultMutableTreeNode getGameObjectsWithTagNode = new DefaultMutableTreeNode("GameObject[] getGameObjectsWithTag(String tag)");
				
				lookupNode.add(getSceneNode);
				lookupNode.add(getGameNode);
				lookupNode.add(getInputNode);
				lookupNode.add(getRandomNode);
				lookupNode.add(getGameObjectWithNameNode);
				lookupNode.add(getGameObjectsWithTagNode);
			}
			rootNode.add(lookupNode);
			
			DefaultMutableTreeNode timeNode = new DefaultMutableTreeNode("james.sugden.engine.Time");
			{
				DefaultMutableTreeNode getThisTime = new DefaultMutableTreeNode("long getThisTime()");
				DefaultMutableTreeNode getLastTime = new DefaultMutableTreeNode("long getLastTime()");
				DefaultMutableTreeNode getDelta = new DefaultMutableTreeNode("int getDelta()");
				
				timeNode.add(getThisTime);
				timeNode.add(getLastTime);
				timeNode.add(getDelta);
			}
			rootNode.add(timeNode);
			
			DefaultMutableTreeNode gameNode = new DefaultMutableTreeNode("james.sugden.engine.Game");
			{
				DefaultMutableTreeNode loadSceneNode = new DefaultMutableTreeNode("void loadScene(String name)");
				
				gameNode.add(loadSceneNode);
			}
			rootNode.add(gameNode);
			
			DefaultMutableTreeNode sceneNode = new DefaultMutableTreeNode("james.sugden.engine.Scene");
			{
				DefaultMutableTreeNode setCameraNode = new DefaultMutableTreeNode("void setCamera(Camera camera)");
				
				sceneNode.add(setCameraNode);
			}
			rootNode.add(sceneNode);
			
			DefaultMutableTreeNode inputNode = new DefaultMutableTreeNode("james.sugden.engine.input.InputHandler");
			{
				DefaultMutableTreeNode isKeyDownNode = new DefaultMutableTreeNode("boolean isKeyDown(String name)");
				DefaultMutableTreeNode wasKeyDownLastUpdateNode = new DefaultMutableTreeNode("boolean wasKeyDownLastUpdate(String name)");
				
				inputNode.add(isKeyDownNode);
				inputNode.add(wasKeyDownLastUpdateNode);
			}
			rootNode.add(inputNode);
			
			DefaultMutableTreeNode gameObjectNode = new DefaultMutableTreeNode("james.sugden.engine.game_object.GameObject");
			{
				DefaultMutableTreeNode getRectColliderWithNameNode = new DefaultMutableTreeNode("RectCollider getRectColliderWithName(String name)");
				DefaultMutableTreeNode getAudioSourceWithNameNode = new DefaultMutableTreeNode("AudioSource getAudioSourceWithName(String name)");
				DefaultMutableTreeNode getImageRendererWithNameNode = new DefaultMutableTreeNode("ImageRenderer getImageRendererWithName(String name)");
				
				gameObjectNode.add(getRectColliderWithNameNode);
				gameObjectNode.add(getAudioSourceWithNameNode);
				gameObjectNode.add(getImageRendererWithNameNode);
			}
			rootNode.add(gameObjectNode);
		}
		
		JTree treeDocumentation = new JTree(rootNode);
		pnlDocumentation.add(treeDocumentation, BorderLayout.CENTER);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	private final void createAnimationEditor(GameObject gameObject)
	{
		if(gameObject != null)
		{
			pnlAnimationEditor.removeAll();
			
			JPanel pnlAnimations = new JPanel(new GridLayout(0, 1));
			pnlAnimationEditor.add(pnlAnimations, BorderLayout.WEST);
			
			for(Component c : gameObject.getComponents())
			{
				if(c instanceof Animator)
				{
					Animator animator = (Animator)c;
					
					for(Animation animation : animator.getAnimations())
					{
						/**
						 * Add connector between animations (not to itself)
						 * Edit connections between animations
						 * Delete unwanted connections
						 * */
						
						JLabel lblAnimation = new JLabel();
						lblAnimation.setName(animation.animationRenderer.getIdentificationName());
						
						TitledBorder border;
						if(animator.getAnimations()[animator.getFirstAnimation()].equals(animation))
							border = BorderFactory.createTitledBorder("START: " + animation.animationRenderer.getIdentificationName());
						else
							border = BorderFactory.createTitledBorder(animation.animationRenderer.getIdentificationName());
						
						Border margin = new EmptyBorder(15, 0, 15, 0);
						lblAnimation.setBorder(new CompoundBorder(border, margin));
						
						
						pnlAnimations.add(lblAnimation);
						
						//Set the image in lblAnimation, on a timer if possible so plays the animation
						if(animation.animationRenderer != null)
						{
							if(animation.animationRenderer.getAnimationTextures() != null)
							{
								final IntegerHolder ih = new IntegerHolder();
								ih.i = 0;
								
								//Should be safe to cast to int as will be very unlikely to not be in range
								new Timer((int)animation.animationRenderer.getTimeDelay(), e ->
								{
									if(ih.i >= ((AnimationRenderer)animation.animationRenderer).getAnimationTextures().length)
										ih.i = 0;
									AnimationTexture animTex = ((AnimationRenderer)animation.animationRenderer).getAnimationTextures()[ih.i];
									lblAnimation.setIcon(new ImageIcon(animTex.img));
									lblAnimation.setMinimumSize(new Dimension(animTex.img.getWidth(null) + 20, animTex.img.getHeight(null) + 20));
									lblAnimation.setPreferredSize(new Dimension(animTex.img.getWidth(null) + 20, animTex.img.getHeight(null) + 20));
									ih.i++;
								}).start();
							}
							else if(animation.animationRenderer.getTexture() != null)
							{
								try
								{
									Image img = ImageIO.read(new File(animation.animationRenderer.getTexture().getPath()));
									lblAnimation.setIcon(new ImageIcon(img));
								} catch (IOException e1)
								{
									e1.printStackTrace();
									StringBuilder sb = new StringBuilder(e1.toString());
								    for(StackTraceElement ste : e1.getStackTrace())
								    {
								        sb.append("\n\tat ");
								        sb.append(ste);
								    }
								    String trace = sb.toString();
								    JOptionPane.showMessageDialog(window, trace, e1.toString(), JOptionPane.ERROR_MESSAGE);
								}
							}
							
							lblAnimation.addMouseListener(new MouseListener()	//Creates menus which allows editing of connectors
							{
								public void mouseClicked(MouseEvent e) {}
								public void mouseEntered(MouseEvent e) {}
								public void mouseExited(MouseEvent e) {}
								
								public void mousePressed(MouseEvent e)
								{
									if(e.isPopupTrigger())
									{
										showMenu(e);
									}
								}
								
								public void mouseReleased(MouseEvent e)
								{
									if(e.isPopupTrigger())
									{
										showMenu(e);
									}
								}
								
								private void showMenu(MouseEvent e)
								{
									JPopupMenu mnuOptions = new JPopupMenu();
									JMenuItem itmSetAsStart = new JMenuItem("Set As Start");
									JMenuItem mnuConnect = new JMenu("Connect");	//Allows the user to connect two animation renderers together
									JMenuItem mnuEditPathTo = new JMenu("Edit Path To");
									JMenuItem mnuDelete = new JMenu("Delete");	//Allows the user to delete a connection between two animation renderers
									
									for(Connector connector : animation.getConnectors())	//Find the connectors belonging to the animation
									{
										JMenuItem itmConnectorEdit = new JMenuItem(connector.animation.animationRenderer.getIdentificationName());
										JMenuItem itmConnectorDelete = new JMenuItem(connector.animation.animationRenderer.getIdentificationName());
										
										itmConnectorEdit.addActionListener(e1 ->
										{
											createAnimationConstraintsEditor(connector);
										});
										
										itmConnectorDelete.addActionListener(e1 ->
										{
											if(JOptionPane.showConfirmDialog(window, "Are you sure you want to delete this connector?") == JOptionPane.YES_OPTION)
											{
												animation.removeConnector(connector);
												createAnimationEditor(gameObject);
											}
										});
										
										mnuEditPathTo.add(itmConnectorEdit);
										mnuDelete.add(itmConnectorDelete);
									}
									
									for(Animation a : ((Animator)c).getAnimations())	//Find the animation renderers belonging to the animation
									{
										if(a.equals(animation))							//Animation cannot connect to itself so skip it
											continue;
										
										JMenuItem itmConnect = new JMenuItem(a.animationRenderer.getIdentificationName());
										
										itmConnect.addActionListener(e1 ->
										{
											animation.addConnector(a);
											createAnimationEditor(gameObject);
										});
										
										mnuConnect.add(itmConnect);
									}
									
									
									itmSetAsStart.addActionListener(e1 ->
									{
										for(java.awt.Component component : pnlAnimations.getComponents())
										{
											if(animator.getAnimations()[animator.getFirstAnimation()].animationRenderer.getIdentificationName().equals(component.getName()))
											{
												((JLabel)component).setBorder(BorderFactory.createTitledBorder(component.getName()));		//Remove the word START from the previous start animation
												break;
											}
										}
										
										animator.setFirstAnimation(animation);
										lblAnimation.setBorder(BorderFactory.createTitledBorder("START: " + lblAnimation.getName()));		//Set the border title of the new start animation
									});
									
									mnuOptions.add(itmSetAsStart);
									mnuOptions.add(mnuConnect);
									mnuOptions.add(mnuDelete);
									mnuOptions.add(mnuEditPathTo);
									mnuOptions.show(lblAnimation, e.getX(), e.getY());
								}
							});
						}
					}
					break;				//Can only have one animator per game object
				}
			}
			
			Canvas cnvArrows = new Canvas()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void paint(Graphics g)
				{
					Color[] colours = new Color[]{Color.BLACK, Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.GRAY};
					
					for(Component c : gameObject.getComponents())
					{
						if(c instanceof Animator)
						{
							Animator animator = (Animator)c;
							int numAnimations = animator.getAnimations().length;
							
							for(int i = 0; i < animator.getAnimations().length; i++)
							{
								Animation animation = animator.getAnimations()[i];
								JComponent compFrom = (JComponent)((JPanel)pnlAnimationEditor.getComponents()[0]).getComponents()[i];
								int lineY = (int)(compFrom.getY() + (compFrom.getHeight() * 0.8f) * i / (numAnimations) + (compFrom.getHeight() * 0.1f));
								
								if(animation.getConnectors().size() > 0)				//Draw a line from the start point of the animation connector
								{
									g.setColor(colours[i % colours.length]);
									g.drawLine(0, lineY, 100 + i * 100, lineY);
								}
								
								for(Connector connector : animation.getConnectors())
								{
									for(int j = 0; j < ((JPanel)pnlAnimationEditor.getComponents()[0]).getComponents().length; j++)			//For each component in the panel
									{
										java.awt.Component comp = ((JPanel)pnlAnimationEditor.getComponents()[0]).getComponents()[j];		//Get the correct component
										
										if(comp.getName().equals(connector.animation.animationRenderer.getIdentificationName()))			//Check the unique name is correct
										{
											int lineToY = (int)(comp.getY() + (comp.getHeight() * 0.8f) * i / (numAnimations) + (comp.getHeight() * 0.1f));
											int triHeight = comp.getHeight() / (numAnimations * 4);
											
											//If the unique name matches, then draw a line to the component
											g.drawLine(0, lineToY, 100 + i * 100, lineToY);
											g.fillPolygon(new int[]{0, triHeight*2, triHeight*2}, new int[]{lineToY, lineToY+triHeight, lineToY-triHeight}, 3);				//Draw an arrow pointing into correct animation
		
											g.setColor(colours[i % colours.length]);
											g.drawLine(100 + i * 100, lineY, 100 + i * 100, lineToY);
											
											break;
										}
									}
								}
							}
						}
					}
				}
			};
			
			cnvArrows.setBackground(Color.WHITE);
			pnlAnimationEditor.add(cnvArrows, BorderLayout.CENTER);
						
			//pnlAnimationEditor.revalidate();
			//pnlAnimationEditor.repaint();
		}
	}
	
	
	/**Creates the animation editing window*/
	/*private final void createAnimationEditorOld(GameObject gameObject)
	{
		if(gameObject != null)
		{
			pnlAnimationEditor.removeAll();
			for(Component c : gameObject.getComponents())
			{
				if(c instanceof Animator)
				{
					for(Animation animation : ((Animator)c).getAnimations())
					{
						JInternalFrame frame = new JInternalFrame();
						frame.setLocation(animation.x, animation.y);
						frame.setFrameIcon(null);
						frame.setSize(200, 100);
						frame.setLayout(new BorderLayout());
						frame.setTitle(animation.animationRenderer.getIdentificationName());
						frame.setBackground(WindowPreferences.getClrBackground());
						if(animation.animationRenderer != null)
						{
							if(animation.animationRenderer instanceof AnimationRenderer && ((AnimationRenderer)animation.animationRenderer).getAnimationTextures() != null)
							{
								JLabel lbl = new JLabel();
								frame.add(lbl);
								class IntegerHolder
								{
									int i;
								}
								final IntegerHolder ih = new IntegerHolder();
								ih.i = 0;
								new Timer(250, e ->
								{
									if(ih.i >= ((AnimationRenderer)animation.animationRenderer).getAnimationTextures().length)
										ih.i = 0;
									AnimationTexture animTex = ((AnimationRenderer)animation.animationRenderer).getAnimationTextures()[ih.i];
									lbl.setIcon(new ImageIcon(animTex.img));
									ih.i ++;
								}).start();;
							}
							else if(animation.animationRenderer.getTexture() != null)
							{
								try {
									Image img = ImageIO.read(new File(animation.animationRenderer.getTexture().getPath()));
									JLabel lbl = new JLabel();
									lbl.setIcon(new ImageIcon(img));
									frame.add(lbl);
								} catch (IOException e1) {
									e1.printStackTrace();
									StringBuilder sb = new StringBuilder(e1.toString());
								    for (StackTraceElement ste : e1.getStackTrace()) {
								        sb.append("\n\tat ");
								        sb.append(ste);
								    }
								    String trace = sb.toString();
								    JOptionPane.showMessageDialog(window, trace, e1.toString(), JOptionPane.ERROR_MESSAGE);
								}
							}
						}
						
						frame.setVisible(true);
						
						frame.addComponentListener(new ComponentListener()	//Saves information about the windows
						{
							public void componentHidden(ComponentEvent e) {}
							public void componentMoved(ComponentEvent e)
							{
								animation.x = frame.getX();
								animation.y = frame.getY();
							}
							public void componentResized(ComponentEvent e) {}
							public void componentShown(ComponentEvent e) {}
						});
						
						frame.addMouseListener(new MouseListener()	//Creates menus which allows editing of connectors
						{
							public void mouseClicked(MouseEvent e) {}
							public void mouseEntered(MouseEvent e) {}
							public void mouseExited(MouseEvent e) {}
							
							public void mousePressed(MouseEvent e)
							{
								if(e.isPopupTrigger())
								{
									showMenu(e);
								}
							}
							
							public void mouseReleased(MouseEvent e)
							{
								if(e.isPopupTrigger())
								{
									showMenu(e);
								}
							}
							
							private void showMenu(MouseEvent e)
							{
								JPopupMenu mnuOptions = new JPopupMenu();
								JMenuItem itmSetAsStart = new JMenuItem("Set As Start");
								JMenuItem mnuConnect = new JMenu("Connect");	//Allows the user to connect two animation renderers together
								JMenuItem mnuEditPathTo = new JMenu("Edit Path To");
								JMenuItem mnuDelete = new JMenu("Delete");	//Allows the user to delete a connection between two animation renderers
								
								for(Connector connector : animation.getConnectors())	//Find the connectors belonging to the animation
								{
									JMenuItem itmConnectorEdit = new JMenuItem(connector.animation.animationRenderer.getIdentificationName());
									JMenuItem itmConnectorDelete = new JMenuItem(connector.animation.animationRenderer.getIdentificationName());
									
									itmConnectorEdit.addActionListener(e1 ->
									{
										createAnimationConstraintsEditor(connector);
									});
									
									itmConnectorDelete.addActionListener(e1 ->
									{
										if(JOptionPane.showConfirmDialog(window, "Are you sure you want to delete this connector?") == JOptionPane.YES_OPTION)
										{
											animation.removeConnector(connector);
										}
									});
									
									mnuEditPathTo.add(itmConnectorEdit);
									mnuDelete.add(itmConnectorDelete);
								}
								
								for(Animation a : ((Animator)c).getAnimations())	//Find the animation renderers belonging to the animation
								{
									JMenuItem itmConnect = new JMenuItem(a.animationRenderer.getIdentificationName());
									
									itmConnect.addActionListener(e1 ->
									{
										animation.addConnector(a);
									});
									
									mnuConnect.add(itmConnect);
								}
								
								
								itmSetAsStart.addActionListener(e1 ->
								{
									((Animator)c).setFirstAnimation(animation);
								});
								
								mnuOptions.add(itmSetAsStart);
								mnuOptions.add(mnuConnect);
								mnuOptions.add(mnuDelete);
								mnuOptions.add(mnuEditPathTo);
								mnuOptions.show(frame, e.getX(), e.getY());
							}
						});
						
						pnlAnimationEditor.add(frame);
					}
					break;
				}
			}
			pnlAnimationEditor.revalidate();
			pnlAnimationEditor.repaint();
		}
	}*/
	
	/**Creates a dialogue which allows the user to edit the constraints of animations in an animator*/
	private void createAnimationConstraintsEditor(Connector connector)
	{
		JDialog dlgConstraints = new JDialog();
		dlgConstraints.setTitle("Edit Constraints");
		dlgConstraints.setSize(600, 450);
		dlgConstraints.setVisible(true);
		
		JPanel pnlConstraints = new JPanel(new GridLayout(0, 1));
		
		for(int i = 0; i < connector.variables.size(); i++)
		{
			Object var = connector.variables.get(i);
			Object val = connector.variableValues.get(i);
			String type = connector.types.get(i);
			JPanel pnlVar = new JPanel(new GridLayout(1, 0));
			JComboBox<Object> cmbVar = new JComboBox<>();
			JComboBox<Object> cmbVal = new JComboBox<>();
			
			if(type.equals("INPUT"))
			{
				for(KeyBinding keyBinding : input.getKeyBindings())
				{
					cmbVar.addItem(keyBinding.getName());
				}
				for(MouseBinding mouseBinding : input.getMouseBindings())
				{
					cmbVar.addItem(mouseBinding.getName());
				}
				cmbVal.addItem("true");
				cmbVal.addItem("false");
			}
			cmbVar.setSelectedItem(var);
			cmbVal.setSelectedItem(val);
			
			cmbVar.addActionListener(e ->
			{
				connector.setConstraintVariable(cmbVar.getSelectedItem(), cmbVal.getSelectedItem());
			});
			
			cmbVal.addActionListener(e ->
			{
				connector.setConstraintValue(cmbVar.getSelectedItem(), cmbVal.getSelectedItem());
			});
			
			/**Used for deleting constraints*/
			MouseListener mouseListener = new MouseAdapter()
			{
				public void mousePressed(MouseEvent e1)
				{
					if(e1.isPopupTrigger())
					{
						showMenu(e1);
					}
				}
				
				public void mouseReleased(MouseEvent e1)
				{
					if(e1.isPopupTrigger())
					{
						showMenu(e1);
					}
				}
				
				private void showMenu(MouseEvent e1)
				{
					JPopupMenu mnuPopup = new JPopupMenu();
					JMenuItem itmDelete = new JMenuItem("Delete");
					
					itmDelete.addActionListener(e2 ->
					{
						if(JOptionPane.showConfirmDialog(window, "Are you sure you want to delete this constraint?") == JOptionPane.YES_OPTION)
						{
							connector.removeContraint(var);
							dlgConstraints.setEnabled(false);
							dlgConstraints.setVisible(false);
							createAnimationConstraintsEditor(connector);
						}
					});
					
					mnuPopup.add(itmDelete);
					
					try
					{
						mnuPopup.show(pnlVar, e1.getX(), e1.getY());
					} catch(NullPointerException e)
					{
						System.err.println("Tried to show menu for unselected constraint");
					}
				}
			};
			
			cmbVar.addMouseListener(mouseListener);
			cmbVal.addMouseListener(mouseListener);
			
			pnlVar.add(cmbVar);
			pnlVar.add(cmbVal);
			
			pnlConstraints.add(pnlVar);
		}
		
		JButton btnAddConstraint = new JButton("Add Constraint");
		
		btnAddConstraint.addActionListener(e2 ->
		{
			Object value = JOptionPane.showInputDialog(dlgConstraints, "Select the type of constraint", "Constraint Type", JOptionPane.QUESTION_MESSAGE,
					null, new String[]{"Input", "Field"}, "Input");
			if(value != null)
			{
				if(String.valueOf(value).equals("Input"))
				{
					connector.addConstraint("INPUT", null, null);
					dlgConstraints.setEnabled(false);
					dlgConstraints.setVisible(false);
					createAnimationConstraintsEditor(connector);
				} else
				{
					JOptionPane.showMessageDialog(window, "Field constraints are not currently supported", "Unsupported: Fields", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		pnlConstraints.add(btnAddConstraint);
		
		dlgConstraints.add(pnlConstraints);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**Creates and returns a panel which displays and allows editing of user interfaces*/
	public final JPanel createGUIEditor()	//TODO - Make private once used
	{
		JPanel guiEditor = new JPanel();
		
		if(uiManager.getCurrentUI() != null)
		{
			/*for(UIComponent comp : uiManager.getCurrentUI().getUIComponents())
			{
				
			}*/
		}
		
		return guiEditor;
	}
	
	
	
	/**Creates and returns a panel which contains the GUI toolbox*/
	private final JPanel createGUIToolbox()
	{
		JPanel pnlToolbox = new JPanel(new GridLayout(0, 1));
		
		JPanel pnlContainers = new JPanel();
		pnlContainers.setLayout(new BoxLayout(pnlContainers, BoxLayout.Y_AXIS));
		JPanel pnlInputs = new JPanel();
		pnlInputs.setLayout(new BoxLayout(pnlInputs, BoxLayout.Y_AXIS));
		
		//Containers
		JButton btnAddPanel = new JButton("Add Panel");
		JButton btnAddTabPanel = new JButton("Add Tab Panel");
		JButton btnAddListBox = new JButton("Add List Box");
		JButton btnAddDialogue = new JButton("Add Dialogue");
		
		//Inputs
		JButton btnAddButton = new JButton("Add Button");
		
		
		btnAddPanel.setAlignmentX(JButton.CENTER_ALIGNMENT);
		btnAddTabPanel.setAlignmentX(JButton.CENTER_ALIGNMENT);
		btnAddListBox.setAlignmentX(JButton.CENTER_ALIGNMENT);
		btnAddDialogue.setAlignmentX(JButton.CENTER_ALIGNMENT);
		
		btnAddButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		
		Border bdrPanel = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		pnlContainers.setBorder(BorderFactory.createTitledBorder(bdrPanel, "Containers",
				TitledBorder.CENTER, TitledBorder.TOP, new Font("times new roman", Font.BOLD, 16), Color.BLACK));
		pnlInputs.setBorder(BorderFactory.createTitledBorder(bdrPanel, "Input Methods",
				TitledBorder.CENTER, TitledBorder.TOP, new Font("times new roman", Font.BOLD, 16), Color.BLACK));
		
		pnlContainers.add(btnAddPanel);
		pnlContainers.add(btnAddTabPanel);
		pnlContainers.add(btnAddListBox);
		pnlContainers.add(btnAddDialogue);
		
		pnlInputs.add(btnAddButton);
		
		pnlToolbox.add(pnlContainers);
		pnlToolbox.add(pnlInputs);
		/*Panel				#
		Tab Manager
		Button              #
		Check Box
		Combo Box
		Label
		List Box			-
		Spinner
		Image
		Radio Button
		Text Box
		Percentage Bar
		Menu Bar			
		Menu Item
		Dialogue			-*/
		
		return pnlToolbox;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**Creates the tool bar used to manipulate what is drawn in the opengl context*/
	private final void createGameToolbar()
	{
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		
		JButton btnRun = new JButton("Run");
		JButton btnGrid = new JButton("Grid");
		JButton btnSnap = new JButton("Snap");
		JButton btnLighting = new JButton("Lighting");
		
		btnRun.addActionListener(e ->
		{
			if(runAsGame == false)
			{
				scene.setCamera(defaultGameCamera);		//Disable drag camera and allow the user selected camera to take over
				pnlBottom.addTab("Console", sclConsole);//Add the console to the bottom panel
				createConsole();						//Console created before game is run so script compile errors are displayed to the user
				runAsGame = true;
				btnRun.setText("Stop");
				pnlBottom.setSelectedComponent(sclConsole);
				lstGameObject.setEnabled(false);
				pnlGameObjectInspector.setEnabled(false);
				resourceBrowser.setEnabled(false);
				pnlDocumentation.setEnabled(false);
				pnlOpenglContext.setEnabled(false);
				pnlCentre.setEnabled(false);
				pnlBottom.setEnabled(false);
				createGameObjectInspector(null);
				//makePanelFullscreen(pnlCentre);
				glCanvas.requestFocus();
			} else
			{
				scene.setCamera(editCamera);
				runAsGame = false;
				btnRun.setText("Run");
				lstGameObject.setEnabled(true);
				pnlGameObjectInspector.setEnabled(true);
				resourceBrowser.setEnabled(true);
				pnlConsole.setEnabled(true);
				pnlDocumentation.setEnabled(true);
				pnlOpenglContext.setEnabled(true);
				pnlCentre.setEnabled(true);
				pnlBottom.setEnabled(true);
				MainAsGame.stopGame();
				//makePanelFullscreen(pnlStdWindow);
				createGameObjectInspector(gameObjectSelected);
			}
		});
		
		btnGrid.addActionListener(e ->
		{
			if(drawGrid)
			{
				drawGrid = false;
			} else if(!runAsGame)
			{
				drawGrid = true;
			}
		});
		
		btnSnap.addActionListener(e ->
		{
			if(transformSnap)
			{
				transformSnap = false;
			} else if(!runAsGame)
			{
				transformSnap = true;
			}
		});
		
		btnLighting.addActionListener(e ->
		{
			if(doLighting)
			{
				doLighting = false;
			} else if(!runAsGame)
			{
				doLighting = true;
			}
		});
		
		toolbar.add(btnRun);
		toolbar.addSeparator();
		toolbar.add(btnGrid);
		toolbar.addSeparator();
		toolbar.add(btnSnap);
		toolbar.addSeparator();
		toolbar.add(btnLighting);
		
		pnlOpenglContext.add(toolbar, BorderLayout.NORTH);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private final void createCodeEditor(GameObject gameObject)
	{
		createDocumentationPanel();
		pnlBottom.addTab("Documentaion", sclDocumentationPanel);//Add the documentation to the bottom panel
		CodeEditor.createCodeEditor(gameObject, pnlCodeEditor);	//Calls convenience method for creating code editor
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**Creates the canvas used to hold the opengl context*/
	private final void createOpenGLCanvas()
	{
		/*canvas.addComponentListener(new ComponentListener()
		{
			public void componentHidden(ComponentEvent e)
			{
				needUpdateViewport = true;
			}
			public void componentMoved(ComponentEvent e)
			{
				needUpdateViewport = true;
			}
			public void componentResized(ComponentEvent e)
			{
				needUpdateViewport = true;
			}
			public void componentShown(ComponentEvent e)
			{
				needUpdateViewport = true;
			}
		});*/
		final BooleanHolder created = new BooleanHolder(false);
		glCanvas = new Canvas()
		{
			private static final long serialVersionUID = 1L;
			public final void addNotify()
			{
                super.addNotify();
                if(!created.b)
                {
                	createOpenGLContext();
                	created.b = true;
                }
            }
            public final void removeNotify()
            {
            	//Display.destroy();
                super.removeNotify();
            }
        };
        glCanvas.setSize(800, 600);
		pnlOpenglContext.add(glCanvas, BorderLayout.CENTER);
		glCanvas.setFocusable(true);
		glCanvas.requestFocus();
		glCanvas.setIgnoreRepaint(true);
        window.setVisible(true);
	}
	
	
	
	
	
	
	
	/**Creates the opengl context and adds it to a canvas*/
	private final void createOpenGLContext()
	{
		Thread gameThread = new Thread()
		{
			public void run()
			{
				try {
					Display.setParent(glCanvas);
					Display.create();
					Setup.initGLProjection();
					Setup.initGLRendering();
					Setup.initALAudio();
					RenderingUtilsNEW.createVBOCircle(20);	//Create useful vbos for later use
					glDisable(GL_STENCIL_TEST);
					startScene();
				} catch (LWJGLException e) {
					e.printStackTrace();
					System.err.println("Failed to initialize opengl display");
					StringBuilder sb = new StringBuilder(e.toString());
				    for (StackTraceElement ste : e.getStackTrace()) {
				        sb.append("\n\tat ");
				        sb.append(ste);
				    }
				    String trace = sb.toString();
				    JOptionPane.showMessageDialog(window, trace, e.toString(), JOptionPane.ERROR_MESSAGE);
					window.setEnabled(false);
					Display.destroy();
					System.exit(1);
				}
			}
		};
		gameThread.start();
	}
	
	
	
	
	
	
	
	
	
	/**Creates split panes so all panels are resizable and then adds them to the window*/
	private final void createSplitPanes()	//TODO - Keep Marker Here!
	{
		sclGameObjectViewer.setMinimumSize(new Dimension(window.getWidth()/8, window.getHeight()/2));
	//	pnlOpenglContext.setMinimumSize(new Dimension(window.getWidth()/2, window.getHeight()/2));
		sclGameObjectInspector.setMinimumSize(new Dimension(window.getWidth()/4, window.getHeight()/2));
		sclGameObjectInspector.setMaximumSize(new Dimension(window.getWidth()/3, window.getHeight()/2));
		//sclFileBrowser.setMinimumSize(new Dimension(window.getWidth(), window.getHeight()/4));
		
		pnlRight.addTab("Game Object Inspector", sclGameObjectInspector);
		pnlRight.setPreferredSize(new Dimension(window.getWidth()/4, window.getHeight()/2));
		pnlRight.setMinimumSize(new Dimension(window.getWidth()/4, window.getHeight()/2));
		pnlRight.addMouseListener(new MouseListener()		//Allows closing of tabs by right clicking and pressing close
        {
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e)
			{
				if(e.isPopupTrigger())	//Cause a menu to pop up if right mouse click is pressed
				{
					JPopupMenu mnuPopup = new JPopupMenu();
					JMenuItem itmCloseTab = new JMenuItem("Close Tab");
					itmCloseTab.addActionListener(e1 ->
					{
						if(pnlRight.getSelectedIndex() > 0)					//Doesn't allow deletion of opengl window
							pnlRight.remove(pnlRight.getSelectedIndex());
					});
					
					mnuPopup.add(itmCloseTab);
					mnuPopup.show(pnlRight, e.getX(), e.getY());
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
        });
		
		pnlCentre.addTab("OpenGL Window", pnlOpenglContext);
        pnlCentre.setPreferredSize(new Dimension(800, 3*window.getHeight()/4));
        pnlCentre.setMinimumSize(new Dimension(700, 3*window.getHeight()/5));
        pnlCentre.addMouseListener(new MouseListener()		//Allows closing of tabs by right clicking and pressing close
        {
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e)
			{
				if(e.isPopupTrigger())	//Cause a menu to pop up if right mouse click is pressed
				{
					JPopupMenu mnuPopup = new JPopupMenu();
					JMenuItem itmCloseTab = new JMenuItem("Close Tab");
					itmCloseTab.addActionListener(e1 ->
					{
						if(pnlCentre.getSelectedIndex() > 0)					//Doesn't allow deletion of opengl window
							pnlCentre.remove(pnlCentre.getSelectedIndex());
					});
					
					mnuPopup.add(itmCloseTab);
					mnuPopup.show(pnlCentre, e.getX(), e.getY());
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
        });
        
        pnlBottom.addTab("File Browser", resourceBrowser);
        pnlBottom.setPreferredSize(new Dimension(2*window.getWidth()/3, window.getHeight()/4));
        pnlBottom.setMaximumSize(new Dimension(2*window.getWidth()/3, window.getHeight()/4));
        pnlBottom.setMinimumSize(new Dimension(3*window.getWidth()/5, window.getHeight()/4));
        pnlBottom.addMouseListener(new MouseListener()		//Allows closing of tabs by right clicking and pressing close
        {
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e)
			{
				if(e.isPopupTrigger())	//Cause a menu to pop up if right mouse click is pressed
				{
					JPopupMenu mnuPopup = new JPopupMenu();
					JMenuItem itmCloseTab = new JMenuItem("Close Tab");
					itmCloseTab.addActionListener(e1 ->
					{
						if(pnlBottom.getSelectedIndex() > 0)					//Doesn't allow deletion of file browser
							pnlBottom.remove(pnlBottom.getSelectedIndex());
					});
					
					mnuPopup.add(itmCloseTab);
					mnuPopup.show(pnlBottom, e.getX(), e.getY());
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
        });
		
		JSplitPane pnlSplitHorizontal1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, sclGameObjectViewer, pnlCentre);
		JSplitPane pnlSplitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, pnlSplitHorizontal1, pnlBottom);
		JSplitPane pnlSplitHorizontal2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlSplitVertical, pnlRight);
		
		pnlSplitHorizontal1.setDividerLocation(0.25);
		pnlSplitVertical.setDividerLocation(0.825);
		
		
		//window.add(pnlSplitHorizontal2);
		pnlStdWindow = pnlSplitHorizontal2;
		//pnlStdWindow.add(pnlSplitHorizontal2);
	}
	
	private void makePanelFullscreen(Container pnl)
	{
		if(window.getContentPane().getComponents().length > 0)
			window.getContentPane().removeAll();
		
		window.add(pnl);
		
		window.setVisible(true);
		window.revalidate();
		window.repaint();
	}
	
	
	
	
	
	
	
	
	
	
	/**Export the loaded project as a game
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException */
	private final void exportAsJar() throws FileNotFoundException, IOException, URISyntaxException
	{
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "james.sugden.engine.MainAsGame");
		JarOutputStream target = new JarOutputStream(new FileOutputStream("C:/Users/James/Desktop/output.jar"), manifest);
		System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		addJarEntry(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()), target);
		target.close();
		
	}
	
	private final void addJarEntry(File source, JarOutputStream target) throws IOException
	{
		BufferedInputStream in = null;
		try
		{
			if(source.isDirectory())
			{
				String name = source.getPath().replace("/", "/");
				System.out.println(name);
				if(!name.isEmpty())
				{
					if(!name.endsWith("/"))
						name += "/";
					JarEntry entry = new JarEntry(name);
			        entry.setTime(source.lastModified());
			        target.putNextEntry(entry);
			        target.closeEntry();
				}
				for(File nestedFile: source.listFiles())
				{
					addJarEntry(nestedFile, target);
					return;
			    }
			}
			else
			{
				JarEntry entry = new JarEntry(source.getPath().replace("/", "/"));
			    entry.setTime(source.lastModified());
			    target.putNextEntry(entry);
			    in = new BufferedInputStream(new FileInputStream(source));
			    
			    byte[] buffer = new byte[1024];
			    while(true)
			    {
			    	int count = in.read(buffer);
			    	if(count == -1)
			    		break;
			    	target.write(buffer, 0, count);
			    }
			    target.closeEntry();
			}
		}
		finally
	    {
	    	if(in != null)
	    		in.close();
	    }
	}
	
	
	
	
	
	
	
	
	private final void styleAllComponents(JComponent rootComp)
	{
		java.awt.Component[] comps = rootComp.getComponents();
		//System.err.println(rootComp.getName());
		for(java.awt.Component comp : comps)
		{
			//System.err.println(comp.getName());
			
		    if(comp instanceof JComponent)
		    {
		    	((JComponent)comp).setBackground(WindowPreferences.getClrBackground());
		    	((JComponent)comp).setForeground(WindowPreferences.getClrForeground());
		    	
			    if(comp instanceof JPanel)
			    {
			    	LayoutManager lyt = ((JComponent)comp).getLayout();
			    	//if(lyt != null)
			    	//	System.out.println("Layout: " + lyt.getClass());
			    	if(lyt instanceof GridLayout)
			    	{
			    //		int numRows = ((GridLayout)lyt).getRows();
			    //		comp.setMinimumSize(new Dimension(0, numRows * 30));
			    //		comp.setMaximumSize(new Dimension(window.getWidth(), numRows * 30));
			    //		comp.setPreferredSize(new Dimension(comp.getWidth(), numRows * 30));
			    //		System.out.println(comp.getName() + " numRows " + numRows + " size" + numRows * 30);
			    	}
			    	
			    	styleAllComponents((JComponent)comp);
			    }
		    }
		}
	}
	
	
	
	
	
	
	
	
	
	
	/**Generates a unique name from the*/
	private final void generateUniqueComponentName(GameObject gameObject, Component component, String defaultName)
	{
		String actualName = defaultName;
		int count = 0;
		boolean foundUniqueName;
		do
		{
			foundUniqueName = true;
			for(Component c : gameObject.getComponents())
			{
				if(c.getIdentificationName().equals(actualName))
				{
					actualName = defaultName + "_" + ++count;
					foundUniqueName = false;
					break;
				}
			}
		} while(!foundUniqueName);
		component.setIdentificationName(actualName);
	}
	
	/**Generates a unique name from the*/
	private final void generateUniqueGameObjectName(GameObject gameObject, String defaultName)
	{
		String actualName = defaultName;
		int count = 0;
		boolean foundUniqueName;
		do
		{
			foundUniqueName = true;
			for(GameObject o : scene.getGameObjects())
			{
				if(o != gameObject && o.getName().equals(actualName))
				{
					actualName = defaultName + "_" + ++count;
					foundUniqueName = false;
					break;
				}
			}
		} while(!foundUniqueName);
		gameObject.setName(actualName);
	}
	
	private final class GameObjectListCellRenderer extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = 1L;
		
		public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if(value instanceof GameObject)
			{
				GameObject gameObject = (GameObject)value;
				setText(gameObject.getName());
			}
			return this;
		}
	}
}

//Code editor colouring attempt
/*StyledDocument docCode = txtCode.getStyledDocument();
Style keywordStyle = txtCode.addStyle("keyword", null);
StyleConstants.setForeground(keywordStyle, Color.MAGENTA);
StyleConstants.setBold(keywordStyle, true);

txtCode.addKeyListener(new KeyListener()
{
	private String word = "";
	
	public void keyPressed(KeyEvent e)
	{
		if(Character.isAlphabetic(e.getKeyChar()))
		{
			word += e.getKeyChar();
			checkKeyWord();
		} else
		{
			word = "";
		}
	}
	
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e){}
	
	private void checkKeyWord()
	{
		for(String keyword : keywords)
		{
			if(keyword.equals(word))
			{
				try {
					docCode.insertString(docCode.getLength(), word, keywordStyle);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	}
});*/
