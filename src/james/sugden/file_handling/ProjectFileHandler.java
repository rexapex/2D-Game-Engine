package james.sugden.file_handling;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileSystemView;

import james.sugden.engine.MainAsGame;
import james.sugden.engine.Scene;
import james.sugden.engine.animation.AnimationRenderer;
import james.sugden.engine.animation.Animator;
import james.sugden.engine.audio.AudioListener;
import james.sugden.engine.audio.AudioSource;
import james.sugden.engine.camera.FollowCamera;
import james.sugden.engine.game_object.Component;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.input.InputHandler;
import james.sugden.engine.input.KeyboardHandler;
import james.sugden.engine.input.MouseHandler;
import james.sugden.engine.lighting.PointLight;
import james.sugden.engine.lighting.SpotLight;
import james.sugden.engine.physics.CircleCollider;
import james.sugden.engine.physics.GridCollider;
import james.sugden.engine.physics.RectCollider;
import james.sugden.engine.rendering.ImageRenderer;
import james.sugden.engine.rendering.ParticleEmitter;
import james.sugden.engine.scripts.Script;
import james.sugden.utils.ProjectList;
import james.sugden.utils.ProjectList.Project;

public class ProjectFileHandler
{
	public static final String version = "v1.0";
	
	/**Loads the project selected and returns it's path*/
	public static final Project setup(JFrame frame)
	{
		/**Used to store the path
		 * Needed because value can change when final*/
		//final JTextField txtPath = new JTextField();
		
		//String dirData = System.getenv("APPDATA");
		String dirData = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
		File dirJSGames = new File(dirData + "/JPGames");
		if(!dirJSGames.exists())
		{
			dirJSGames.mkdirs();
		}
		
		
		
		
		//Game Engine File Creation
		File dirJPGGameEngine = new File(dirJSGames.getAbsolutePath() + "/JPGEngine");
		if(!dirJPGGameEngine.exists())
			dirJPGGameEngine.mkdirs();
		
		
		//Project Directory Creation
		File dirProjects = new File(dirJSGames.getAbsolutePath() + "/Projects");
		if(!dirProjects.exists())
			dirProjects.mkdirs();
		
		
		
		//Project Creation/Opening
		
		/**Make the user interface look like a program of the current operating system*/
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		
		
		JDialog projectChooser = new JDialog(frame, JDialog.ModalityType.APPLICATION_MODAL);
		//projectChooser.setAlwaysOnTop(true);
		
		projectChooser.setTitle("Open/Create Project");
		projectChooser.setSize(400, 150);
		projectChooser.setLocationRelativeTo(null);
		projectChooser.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		projectChooser.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				int confirm = JOptionPane.showOptionDialog(projectChooser, "Are You Sure You Want to Exit", "Exit?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                if(confirm == 0)
                {
                   System.exit(0);
                }
			}
		});
		
		
		
		
		ProjectList lstProjects = new ProjectList();
		JScrollPane sclProjects = new JScrollPane(lstProjects);
		sclProjects.setBorder(BorderFactory.createEmptyBorder());
		projectChooser.getContentPane().add(sclProjects, BorderLayout.CENTER);
		
		//Add projects from the projects folder to the projects list
		for(File fleProject : dirProjects.listFiles())
		{
			//File fleInfo = new File(fleProject.getAbsolutePath() + File.separator + "info.txt");
			Project proj1 = lstProjects.new Project(version, fleProject.getName(), fleProject.getAbsolutePath(), "");
			lstProjects.addElement(proj1);
		}
		
		final Project proj = lstProjects.new Project("", "", "", "");
		
		lstProjects.addNewProjectButton();
		lstProjects.addProjectOpenedListener(lstProjects.new ProjectOpenedListener()
				{
					@Override
					public final void projectOpened(Project project)			//Sets the final proj vaariable values
					{
						proj.fleProject = project.fleProject;
						proj.strDateCreated = project.strDateCreated;
						proj.strEngineVersion = project.strEngineVersion;
						proj.strName = project.strName;
						proj.edit = project.edit;
						
						System.out.println("Edit: " + project.edit);
						
						if(!project.edit)
						{
							//MainAsGame.main(new String[]{project.fleProject.getAbsolutePath()});
							projectChooser.setAlwaysOnTop(false);
							MainAsGame.startAsGameFromSelector(projectChooser, project.fleProject.getAbsolutePath());
							frame.dispose();
							projectChooser.dispose();								//Dispose of this dialogue so control returns to scene editor
						}
						
						projectChooser.dispose();								//Dispose of this dialogue so control returns to scene editor
					}
				});
		
		projectChooser.pack();
		projectChooser.setSize(new Dimension((int)(projectChooser.getWidth() + 150), (int)(projectChooser.getHeight() + 25)));
		projectChooser.setVisible(true);
		
		return proj;
	}
	
	public static final void createProject(File fleProject) throws IOException
	{
		fleProject.mkdir();
		
		String dirProject = fleProject.getAbsolutePath();
		
		File fleVersion = new File(dirProject+"/version.txt");
		FileWriter wrtVersion = new FileWriter(fleVersion);
		wrtVersion.write(version);
		wrtVersion.close();
		
		File fleLastSceneEdited = new File(dirProject+"/last_scene_edited.txt");
		fleLastSceneEdited.createNewFile();
		
		File fleSettings = new File(dirProject + "/settings");
		if(!fleSettings.exists())
			fleSettings.mkdirs();
		
		File fleInput = new File(dirProject+"/settings/input.txt");
		fleInput.createNewFile();
		
		File fleScenes = new File(dirProject + "/scenes");
		if(!fleScenes.exists())
			fleScenes.mkdir();
		
		File fleRes = new File(dirProject + "/res");
		if(!fleRes.exists())
			fleRes.mkdir();
		
		File fleImages = new File(dirProject + "/res/images");
		if(!fleImages.exists())
			fleImages.mkdir();
		
		File fleSounds = new File(dirProject + "/res/sounds");
		if(!fleSounds.exists())
			fleSounds.mkdir();
		
		File fleScripts = new File(dirProject + "/res/scripts");
		if(!fleScripts.exists())
			fleScripts.mkdir();
		
		File fleShaders = new File(dirProject + "/res/shaders");
		if(!fleShaders.exists())
			fleShaders.mkdir();
		
		File fleStrings = new File(dirProject + "/res/strings");
		if(!fleStrings.exists())
			fleStrings.mkdir();
		
		File flePrefabs = new File(dirProject + "/res/prefabs");
		if(!flePrefabs.exists())
			flePrefabs.mkdir();
	}
	
	public static final void saveProject(Scene scene, File fleProject, ResourceManager resourceManager, InputHandler input)
	{
		try {
			System.out.println(fleProject.getAbsolutePath());
			File fleTemp  = new File(fleProject.getAbsolutePath()+"/settings/input" + "_temp" + Math.round(System.nanoTime()) + Math.round(System.nanoTime()) + ".txt");
			if(!fleTemp.exists())
				fleTemp.createNewFile();
			BufferedWriter wrtInput = new BufferedWriter(new FileWriter(fleTemp));
			
			String output = "";
			for(KeyboardHandler.KeyBinding keyBinding : input.getKeyBindings())
			{
				output += "<Input type=\"key\" name=\"" + keyBinding.getName() + "\" id=\"" + keyBinding.getID() + "\"/>" + "\r\n";
			}
			for(MouseHandler.MouseBinding keyBinding : input.getMouseBindings())
			{
				output += "<Input type=\"mouse\" name=\"" + keyBinding.getName() + "\" id=\"" + keyBinding.getID() + "\"/>" + "\r\n";
			}
			wrtInput.write(output);
			
			wrtInput.close();
			
			File fleInput = new File(fleProject.getAbsolutePath()+"/settings/input.txt");
			if(fleInput.exists())
				fleInput.delete();
			fleTemp.renameTo(fleInput);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		saveScene(scene, fleProject, resourceManager);
	}
	
	public static final void openProject(Scene scene, File fleProject, File fleScene, ResourceManager resourceManager, InputHandler input)
	{
		try {
			//Add all images to the imported images list of the resource manager
			for(File file : findImportedResourcesInDir(fleProject.getAbsolutePath() + "/res/images"))
			{
				String relPath = file.getAbsolutePath().substring((fleProject.getAbsolutePath() + "/res/images").length() + 1).replaceAll("\\\\", "/");
				if(!resourceManager.getImportedTexturePaths().contains(relPath))
				{
					resourceManager.addImportedTexturePath(relPath);
				}
			}
			//Add all audio buffers to the imported audio buffers list of the resource manager
			for(File file : findImportedResourcesInDir(fleProject.getAbsolutePath() + "/res/sounds"))
			{
				String relPath = file.getAbsolutePath().substring((fleProject.getAbsolutePath() + "/res/sounds").length() + 1).replaceAll("\\\\", "/");
				if(!resourceManager.getImportedAudioBufferPaths().contains(relPath))
				{
					resourceManager.addImportedAudioBufferPath(relPath);
				}
			}
			
			
			
			BufferedReader rdrInput = new BufferedReader(new FileReader(fleProject.getAbsolutePath()+"/settings/input.txt"));
			
			String line = "";
			while((line = rdrInput.readLine()) != null && input != null)
			{
				if((line = line.trim()).startsWith("<Input "))
				{
					String type = (line.split("type=\"")[1]).split("\"")[0];
					String name = (line.split("name=\"")[1]).split("\"")[0];
					String id = (line.split("id=\"")[1]).split("\"")[0];
					if(type.equals("key"))
					{
						input.addKeyBinding(name, Integer.valueOf(id));
					} else if(type.equals("mouse"))
					{
						input.addMouseBinding(name, Integer.valueOf(id));
					}
				}
			}
			
			rdrInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		openScene(scene, fleProject, fleScene, resourceManager);
	}
	
	public static final void openScene(Scene scene, File fleProject, File fleScene, ResourceManager resourceManager)
	{
		try {
			BufferedReader rdrScene = new BufferedReader(new FileReader(fleScene));
			
			List<GameObject> gameObjects = new ArrayList<>();
			Component component = null;
			List<String> fields = new ArrayList<>();
			String line;
			while((line = rdrScene.readLine()) != null)
			{
				if((line = line.trim()).startsWith("<Scene"))				//Scene opening tag
				{
					String[] args = line.split("<Scene")[1].split(" ");
					for(String arg : args)
					{
						if(arg.contains("name=\""))
						{
							arg = arg.split("name=\"")[1].split("\"")[0];	//Split the name of the scene from the scene tag
							scene.setName(arg);
						}
					}
				} else if((line = line.trim()).startsWith("<Settings"))
				{
					
				} else if((line = line.trim()).startsWith("<GameObject"))	//Game object opening tag
				{
					String[] args = (line.split("<GameObject")[1]).split(" ");
					String name = "", tag = "";
					for(String arg : args)
					{
						if(arg.startsWith("name=\""))
							name = arg.replace("name=\"", "").replace("\"", "");
						if(arg.contains("tag=\""))
							tag = (arg.split("tag=\"")[1]).split("\"")[0];
					}
					GameObject o = new GameObject(name, tag);
					gameObjects.add(o);
				} else if((line = line.trim()).startsWith("<&GameObject"))	//& means the game object is a prefab so load it from the prefabs folder
				{
					String[] args = (line.split("<&GameObject")[1]).split(" ");
					String name = "", tag = "", prefab = "";
					for(String arg : args)
					{
						//System.out.println(arg);
						if(arg.startsWith("name=\""))
							name = arg.replace("name=\"", "").replace("\"", "");
						else if(arg.contains("tag=\""))
						{
							tag = (arg.split("tag=\"")[1]);
							//System.out.println(tag);
							if(!tag.equals("\""))
								tag = tag.split("\"")[0];
							else
								tag = "";
						}
						else if(arg.contains("prefab=\""))
							prefab = (arg.split("prefab=\"")[1]).split("\"")[0];
					}
					GameObject o = new GameObject(name, tag);
					o.setPrefab(prefab);
					openGameObjectPrefab(o, fleProject, resourceManager);
					gameObjects.add(o);
					//scene.addGameObject(o);
				} else if((line = line.trim()).startsWith("<Transform "))
				{
					String[] args = line.split("<Transform")[1].split(" ");
					for(String arg : args)
					{
						if(arg.startsWith("pos=\""))
						{
							String[] split = arg.replace("pos=\"", "").split(",");
							gameObjects.get(gameObjects.size()-1).getTransform().x = Float.valueOf(split[0]);
							gameObjects.get(gameObjects.size()-1).getTransform().y = Float.valueOf(split[1].split("\"")[0]);
						}
						else if(arg.startsWith("rotation=\""))
						{
							gameObjects.get(gameObjects.size()-1).getTransform().rotation = Float.valueOf(arg.split("rotation=\"")[1].split("\"")[0]);
						}
						else if(arg.startsWith("scale=\""))
						{
							gameObjects.get(gameObjects.size()-1).getTransform().scaleX = Float.valueOf(arg.split("scale=\"")[1].split(",")[0]);
							gameObjects.get(gameObjects.size()-1).getTransform().scaleY = Float.valueOf(arg.split(",")[1].split("\"")[0]);
						}
					}
				} else if((line = line.trim()).startsWith("<&Transform "))			//If there is a transform overwrite
				{
					String[] args = line.split("<&Transform")[1].split(" ");
					for(String arg : args)
					{
						if(arg.startsWith("pos=\""))
						{
							String[] split = arg.replace("pos=\"", "").split(",");
							gameObjects.get(gameObjects.size()-1).getTransform().x = Float.valueOf(split[0]);
							gameObjects.get(gameObjects.size()-1).getTransform().y = Float.valueOf(split[1].split("\"")[0]);
						}
						else if(arg.startsWith("rotation=\""))
						{
							gameObjects.get(gameObjects.size()-1).getTransform().rotation = Float.valueOf(arg.split("rotation=\"")[1].split("\"")[0]);
						}
						else if(arg.startsWith("scale=\""))
						{
							gameObjects.get(gameObjects.size()-1).getTransform().scaleX = Float.valueOf(arg.split("scale=\"")[1].split(",")[0]);
							gameObjects.get(gameObjects.size()-1).getTransform().scaleY = Float.valueOf(arg.split(",")[1].split("\"")[0]);
						}
					}
				} else if((line = line.trim()).startsWith("<Depth "))
				{
					String[] args = line.split("<Depth")[1].split(" ");
					for(String arg : args)
					{
						if(arg.startsWith("use=\""))
						{
							gameObjects.get(gameObjects.size()-1).setUseDepthValue(Boolean.valueOf(arg.split("use=\"")[1].split("\"")[0]));
						}
						else if(arg.startsWith("value=\""))
						{
							gameObjects.get(gameObjects.size()-1).setDepthValue(Float.valueOf(arg.split("value=\"")[1].split("\"")[0]));
						}
					}
				} else if((line = line.trim()).startsWith("<&Depth "))			//If there is a transform overwrite
				{
					String[] args = line.split("<&Depth")[1].split(" ");
					for(String arg : args)
					{
						if(arg.startsWith("use=\""))
						{
							gameObjects.get(gameObjects.size()-1).setUseDepthValue(Boolean.valueOf(arg.split("use=\"")[1].split("\"")[0]));
						}
						else if(arg.startsWith("value=\""))
						{
							gameObjects.get(gameObjects.size()-1).setDepthValue(Float.valueOf(arg.split("value=\"")[1].split("\"")[0]));
						}
					}
				} else if((line = line.trim()).startsWith("<Component"))
				{
					String[] args = (line.split("<Component ")[1]).split(" ");
					String type = "", name = "";
					for(String arg : args)
					{
						if(arg.startsWith("type=\""))
							type = arg.replace("type=\"", "").replace("\"", "").replace(">", "");
						if(arg.startsWith("name=\""))
							name = arg.replace("name=\"", "").replace("\"", "").replace(">", "");
					}
					Component c = null;
					switch(type)
					{
					case "Image_Renderer":
						c = new ImageRenderer();
						break;
					case "Animation_Renderer":
						c = new AnimationRenderer();
						break;
					case "Animator":
						c = new Animator();
						break;
					case "Rect_Collider":
						c = new RectCollider();
						break;
					case "Grid_Collider":
						c = new GridCollider();
						break;
					case "Circle_Collider":
						c = new CircleCollider();
						break;
					case "Script":
						c = new Script();
						break;
					case "Particle_Emitter":
						c = new ParticleEmitter();
						break;
					case "Point_Light":
						c = new PointLight();
						break;
					case "Spot_Light":
						c = new SpotLight();
						break;
					case "Audio_Source":
						c = new AudioSource();
						break;
					case "Audio_Listener":
						c = new AudioListener();
						break;
					case "Follow_Camera":
						c = new FollowCamera();
						break;
					}
					if(c != null)
					{
						c.setIdentificationName(name);
						gameObjects.get(gameObjects.size()-1).addComponent(c);
						component = c;
					}
				} else if((line = line.trim()).startsWith("<&Component"))
				{
					String[] args = (line.split("<&Component ")[1]).split(" ");
					String name = "";
					for(String arg : args)
					{
						if(arg.startsWith("name=\""))
							name = arg.replace("name=\"", "").replace("\"", "").replace(">", "");
					}
					//System.out.println("name = " + name);
					component = gameObjects.get(gameObjects.size()-1).getComponentWithName(name);	//Find the component from the game object
					for(String obj : component.getEditableFields(fleProject))
					{
						fields.add(obj);															//Fill the fields list with the prefab default fields
					}
				} else if((line = line.trim()).startsWith("<Field"))
				{
					String arg = line.split("value=\"")[1].split("\"")[0];
					fields.add(arg);
				} else if((line = line.trim()).startsWith("<&Field"))
				{
					int no = Integer.valueOf(line.split("<&Field")[1].split(" ")[0]);
					String arg = line.split("value=\"")[1].split("\"")[0];
					fields.set(no, arg);
				}
				else if((line = line.trim()).startsWith("</Component"))
				{
					if(component != null)
					{
						component.setEditableFields(fields.toArray(new String[fields.size()]), resourceManager, fleProject);
						component = null;
					}
					fields.clear();
				}
				else if((line = line.trim()).startsWith("</&Component"))
				{
					if(component != null)
					{
						//for(String field : fields)
						//{
						//	System.err.println(field);
						//}
						component.setEditableFields(fields.toArray(new String[fields.size()]), resourceManager, fleProject);
						component = null;
					}
					fields.clear();
				}
				else if((line = line.trim()).startsWith("</GameObject"))		//End of a game object tag
				{
					GameObject o = gameObjects.remove(gameObjects.size()-1);	//Retrieve the game object from the list
					o.setupComponents();										//Set up the components now they have all be loaded
					scene.addGameObject(o);										//Add the game object to the scene
				}
				else if((line = line.trim()).startsWith("</&GameObject"))		//End of a game object prefab reference tag
				{
					GameObject o = gameObjects.remove(gameObjects.size()-1);	//Retrieve the game object from the list
					o.setupComponents();										//Set up the components now they have all be loaded
					scene.addGameObject(o);										//Add the game object to the scene
				}
			}
			
			rdrScene.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static final void saveScene(Scene scene, File fleProject, ResourceManager resourceManager)
	{
		File fleTemp  = new File(fleProject.getAbsolutePath() + "/scenes/" + scene.getName() + "_temp" + Math.round(System.nanoTime()) + Math.round(System.nanoTime()) + ".jps");
		if(!fleTemp.exists())	//Create a temporary scene file using random numbers to pad the ending of the temporary name
		{
			try {
				fleTemp.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Failed to create temp file");
				return;
			}
		}
		
		try {
			BufferedWriter wrtScene = new BufferedWriter(new FileWriter(fleTemp));
			
			String codeToWrite = "";
			
			codeToWrite += "<Scene name=\"" + scene.getName() + "\" ver=\"" + version + "\">" + "\r\n";
			codeToWrite += "	<Settings>" + "\r\n";
			
			codeToWrite += "	</Settings>" + "\r\n";
			
			for(GameObject gameObject : scene.getGameObjects())	//Write an entry into the scene file for each game object
			{
				if(gameObject.getPrefab() == null || gameObject.getPrefab().equals(""))	//If the game object is not and instance of a prefab
				{																		//Then write the game object directly to the scene file
					codeToWrite += "	<GameObject name=\"" + gameObject.getName() + "\" tag=\"" + gameObject.getTag() + "\">" + "\r\n";	
					codeToWrite += "		<Transform pos=\"" + gameObject.getTransform().x + "," + gameObject.getTransform().y + "\" " +
							"rotation=\"" + gameObject.getTransform().rotation + "\" scale=\"" + gameObject.getTransform().scaleX + "," +
							gameObject.getTransform().scaleY + "\"/>" + "\r\n";
					codeToWrite += "		<Depth use=\"" + gameObject.getUseDepthValue() + "\" " + "value=\"" + gameObject.getDepthValue() + "\"/>" + "\r\n";
					for(Component component : gameObject.getComponents())	//Write each component of the game object as a component tag
					{
						codeToWrite += "		<Component type=\"" + component.getTypeName().replaceAll(" ", "_") + "\" name=\"" +
								component.getIdentificationName().replaceAll(" ", "_") + "\">" + "\r\n";	//Convert all spaces in names to underscores
						for(Object obj : component.getEditableFields(fleProject))	//getEditableFields returns all fields to save as an object array
						{
							codeToWrite += "			<Field value=\"" + obj + "\"/>" + "\r\n";	//Fields of components are written in specific order
						}
						codeToWrite += "		</Component>" + "\r\n";
					}
					codeToWrite += "	</GameObject>" + "\r\n";
				} else	//Else if the game object is an instance of a prefab
				{
					codeToWrite += writePrefabOverrides(gameObject, fleProject, resourceManager);	//Then write overwrites of the prefab to the scene file
				}
			}
			
			codeToWrite += "</Scene>";	//Append a scene end tag to the code to write
			
			wrtScene.write(codeToWrite);	//Write the code the scene file and then close the buffered writer
			wrtScene.close();
			
			File fleScene = new File(fleProject.getAbsolutePath() + "/scenes/" + scene.getName() + ".jps");	//Delete the original scene file with the same name
			if(fleScene.exists())
				fleScene.delete();
			fleTemp.renameTo(fleScene);		//Convert the temporary file's name to the name of the original scene file
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to write to scene file");
			return;
		}
	}
	
	
	
	
	/**Writes overwrites of the prefab to the scene file for a specific instance of the prefab
	 * Does not write new Components, only overwrites fields in already existing components
	 * @throws IOException 
	 * @throws NumberFormatException */
	private final static String writePrefabOverrides(GameObject gameObject, File fleProject, ResourceManager resourceManager) throws NumberFormatException, IOException
	{
		//Add a game object reference tag to the start of the game object
		String codeToWrite = "	<&GameObject name=\"" + gameObject.getName() + "\" tag=\"" + gameObject.getTag() + "\" prefab=\"" + gameObject.getPrefab() + "\">";
		
		if(!(new File(fleProject.getAbsolutePath() + "/res/prefabs/" + gameObject.getPrefab() + ".jpo").exists()))	//If the prefab doesn't exist then create it using the game object
		{
			saveGameObjectPrefab(gameObject, fleProject, resourceManager);	//Creates the prafab file for a game object if one does not exist
		} else	//If the prefab file already exists, then write overwrites to the scene file if the instance differs from the prefab
		{
			BufferedReader rdrGameObject = new BufferedReader(new FileReader(new File(fleProject.getAbsolutePath() + "/res/prefabs/" + gameObject.getPrefab() + ".jpo")));
			
			Component component = null;					//When overwriting, needs a reference to the component being overwritten
			int fieldPos = 0;							//The position is the editable fields array of the current field
			boolean componentOverwritten = false;		//If the component tag has been overwritten
			
			String line;
			while((line = rdrGameObject.readLine()) != null)	//Read the prefab so the position of the game object can be overwritten
			{
				if((line = line.trim()).startsWith("<Transform "))
				{
					String[] args = line.split("<Transform")[1].split(" ");
					float x = 0, y = 0, rotation = 0, scaleX = 0, scaleY = 0;	//Default prefab transformation
					for(String arg : args)				//Find the default prefab transformation and set values to variables
					{
						if(arg.startsWith("pos=\""))	//Default prefab position
						{
							String[] split = arg.replace("pos=\"", "").split(",");
							x = Float.valueOf(split[0]);
							y = Float.valueOf(split[1].split("\"")[0]);
						}
						else if(arg.startsWith("rotation=\""))	//Default prefab rotation
						{
							rotation = Float.valueOf(arg.split("rotation=\"")[1].split("\"")[0]);
						}
						else if(arg.startsWith("scale=\""))	//Default prefab scale
						{
							scaleX = Float.valueOf(arg.split("scale=\"")[1].split(",")[0]);
							scaleY = Float.valueOf(arg.split(",")[1].split("\"")[0]);
						}
					}
					
					if(x != gameObject.getTransform().x || y != gameObject.getTransform().y ||	//If the transformation of the prefab differs from the instance
							rotation != gameObject.getTransform().rotation ||
							scaleX != gameObject.getTransform().scaleX || scaleY != gameObject.getTransform().scaleY)
					{
						codeToWrite += "\r\n" + "		<&Transform";	//Then add a transform overwrite tag
						
						if(x != gameObject.getTransform().x || y != gameObject.getTransform().y)	//If the position of the instance differs from the prefab
						{
							codeToWrite += " pos=\"" + gameObject.getTransform().x + "," + gameObject.getTransform().y + "\"";	//Overwrite position
						}
						if(rotation != gameObject.getTransform().rotation)	//If the rotation of the instance differs from the prefab
						{
							codeToWrite += " rotation=\"" + gameObject.getTransform().rotation + "\"";	//Overwrite rotation
						}
						if(scaleX != gameObject.getTransform().scaleX || scaleY != gameObject.getTransform().scaleY)	//If the scale of the instance differs from the prefab
						{
							codeToWrite += " scale=\"" + gameObject.getTransform().scaleX + "," + gameObject.getTransform().scaleY + "\"";	//Overwrite scale
						}
						
						codeToWrite += "/>";	//Add ending tag to the end of the transform tag
					}
				} else if((line = line.trim()).startsWith("<Depth "))
				{
					String[] args = line.split("<Depth")[1].split(" ");
					boolean useDepthValue = false;
					float depthValue = 0;
					for(String arg : args)				//Find the default prefab transformation and set values to variables
					{
						if(arg.startsWith("use=\""))	//Whether to use user set depth value
						{
							useDepthValue = Boolean.valueOf(arg.split("use=\"")[1].split("\"")[0]);
						}
						else if(arg.startsWith("value=\""))	//The depth value set by the user
						{
							depthValue = Float.valueOf(arg.split("value=\"")[1].split("\"")[0]);
						}
					}
					
					if(useDepthValue != gameObject.getUseDepthValue() || depthValue != gameObject.getDepthValue())		//If the depth settings of prefab differ from the instance
					{
						codeToWrite += "\r\n" + "		<&Depth";	//Then add a depth overwrite tag
						
						if(useDepthValue != gameObject.getUseDepthValue())	//If the use depth value boolean of the instance differs from the prefab
						{
							codeToWrite += " use=\"" + gameObject.getUseDepthValue() + "\"";	//Overwrite use depth value
						}
						if(depthValue != gameObject.getDepthValue())		//If the depth value of the instance differs from the prefab
						{
							codeToWrite += " value=\"" + gameObject.getDepthValue() + "\"";		//Overwrite depth value
						}
						
						codeToWrite += "/>";	//Add ending tag to the end of the transform tag
					}
				} else if((line = line.trim()).startsWith("<Component "))	//If the component is found, find the matching component from the game object
				{
					String name = line.split("name=\"")[1].split("\"")[0];	//Get the name from the component
					for(Component c : gameObject.getComponents())	//Cycle through all components in the game object
					{
						if(name.equals(c.getIdentificationName()))	//If the name in the prefab file matches the name of the component
						{
							component = c;	//Set the current component being overwritten to the matching component of the game object
							fieldPos = -1;	//Array start at 0 but var is incremented before being used so starts at -1
							componentOverwritten = false;
							break;			//Break since names are unique being components
						}
					}
				} else if((line = line.trim()).startsWith("<Field "))	//If the field has a different value then overwrite it
				{
					String value = line.split("value=\"")[1].split("\"")[0];	//Get the value of the field from the field tag
					fieldPos ++;												//Increase the field position because a new field has been found
					if(component != null)
					{
						//System.out.println(String.valueOf(component.getEditableFields(fleProject)[fieldPos]) + "---" + value);
						if(!String.valueOf(component.getEditableFields(fleProject)[fieldPos]).equals(value))	//If the value of the prefab doesn't equal the value of the object
						{
							if(!componentOverwritten)
							{
								componentOverwritten = true;	//The component has now been overwritten so set the boolean to true
								codeToWrite += "\r\n" + "		<&Component type=\"" + component.getTypeName() + "\" name=\"" + component.getIdentificationName() + "\">";
							}
							codeToWrite += "\r\n" + "			<&Field" + fieldPos + " value=\"" + component.getEditableFields(fleProject)[fieldPos] + "\"/>";	//Overwrite the value in the scene file
						}
						if(fieldPos == component.getEditableFields(fleProject).length-1 && componentOverwritten)	//The last field has been reached
						{
							codeToWrite += "\r\n" + "		</&Component>";	//So end the component
						}
					}
				}
			}
			rdrGameObject.close();	//Close the prefab buffered reader
		}
		
		codeToWrite += "\r\n" + "	</&GameObject>" + "\r\n";	//Append a game object end tag to the code to write
		
		return codeToWrite;	//Return the code to write so it can be appended to the full code to write
	}
	
	
	
	
	
	
	
	/**Saves the game object as a jpo file*/
	public static final void saveGameObjectPrefab(GameObject gameObject, File fleProject, ResourceManager resourceManager)
	{
		File fleTemp  = new File(fleProject.getAbsolutePath() + "/res/prefabs/" + gameObject.getPrefab() + "_temp" + Math.round(System.nanoTime()) + Math.round(System.nanoTime()) + ".jpo");
		if(!fleTemp.exists())
		{
			try {
				fleTemp.createNewFile();	//Creates a temporary file to write to so the file doesn't end up deleted if the program crashes
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Failed to create temp file");
				return;
			}
		}
		
		try {
			System.out.println(fleTemp.getAbsolutePath());
			BufferedWriter wrtGameObject = new BufferedWriter(new FileWriter(fleTemp));
			String codeToWrite = "";
			//codeToWrite += "	<GameObject name=\"" + gameObject.getName() + "\" tag=\"" + gameObject.getTag() + "\">" + "\r\n";
			codeToWrite += "<Transform pos=\"" + gameObject.getTransform().x + "," + gameObject.getTransform().y + "\" " +
					"rotation=\"" + gameObject.getTransform().rotation + "\" scale=\"" + gameObject.getTransform().scaleX + "," +
					gameObject.getTransform().scaleY + "\"/>" + "\r\n";
			codeToWrite += "<Depth use=\"" + gameObject.getUseDepthValue() + "\" " + "value=\"" + gameObject.getDepthValue() + "\"/>" + "\r\n";
			for(Component component : gameObject.getComponents())
			{
				codeToWrite += "<Component type=\"" + component.getTypeName().replaceAll(" ", "_") + "\" name=\"" +
						component.getIdentificationName().replaceAll(" ", "_") + "\">" + "\r\n";
				for(Object obj : component.getEditableFields(fleProject))
				{
					codeToWrite += "	<Field value=\"" + obj + "\"/>" + "\r\n";
				}
				codeToWrite += "</Component>" + "\r\n";
			}
			//codeToWrite += "	</GameObject>" + "\r\n";
			
			wrtGameObject.write(codeToWrite);
			wrtGameObject.close();
			
			File flePrefab = new File(fleProject.getAbsolutePath() + "/res/prefabs/" + gameObject.getPrefab() + ".jpo");
			if(flePrefab.exists())
				flePrefab.delete();
			fleTemp.renameTo(flePrefab);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to write to game object prefab file");
		}
	}
	
	/**Opens a jpo prefab file*/
	public static final void openGameObjectPrefab(GameObject gameObject, File fleProject, ResourceManager resourceManager) throws NumberFormatException, IOException
	{
		BufferedReader rdrGameObject = new BufferedReader(new FileReader(new File(fleProject.getAbsolutePath() + "/res/prefabs/" + gameObject.getPrefab() + ".jpo")));
		
		Component component = null;
		List<String> fields = new ArrayList<>();
		String line;
		while((line = rdrGameObject.readLine()) != null)
		{
			if((line = line.trim()).startsWith("<Transform "))
			{
				String[] args = line.split("<Transform")[1].split(" ");
				for(String arg : args)
				{
					if(arg.startsWith("pos=\""))
					{
						String[] split = arg.replace("pos=\"", "").split(",");
						gameObject.getTransform().x = Float.valueOf(split[0]);
						gameObject.getTransform().y = Float.valueOf(split[1].split("\"")[0]);
					}
					else if(arg.startsWith("rotation=\""))
					{
						gameObject.getTransform().rotation = Float.valueOf(arg.split("rotation=\"")[1].split("\"")[0]);
					}
					else if(arg.startsWith("scale=\""))
					{
						gameObject.getTransform().scaleX = Float.valueOf(arg.split("scale=\"")[1].split(",")[0]);
						gameObject.getTransform().scaleY = Float.valueOf(arg.split(",")[1].split("\"")[0]);
					}
				}
			} if((line = line.trim()).startsWith("<Depth "))
			{
				String[] args = line.split("<Depth")[1].split(" ");
				for(String arg : args)
				{
					if(arg.startsWith("use=\""))
					{
						gameObject.setUseDepthValue(Boolean.valueOf(arg.split("use=\"")[1].split("\"")[0]));
					}
					else if(arg.startsWith("value=\""))
					{
						gameObject.setDepthValue(Float.valueOf(arg.split("value=\"")[1].split("\"")[0]));
					}
				}
			} else if((line = line.trim()).startsWith("<Component"))
			{
				String[] args = (line.split("<Component ")[1]).split(" ");
				String type = "", name = "";
				for(String arg : args)
				{
					if(arg.startsWith("type=\""))
						type = arg.replace("type=\"", "").replace("\"", "").replace(">", "");;
					if(arg.startsWith("name=\""))
						name = arg.replace("name=\"", "").replace("\"", "").replace(">", "");
				}
				Component c = null;
				switch(type)
				{
				case "Image_Renderer":
					c = new ImageRenderer();
					break;
				case "Animation_Renderer":
					c = new AnimationRenderer();
					break;
				case "Animator":
					c = new Animator();
					break;
				case "Rect_Collider":
					c = new RectCollider();
					break;
				case "Grid_Collider":
					c = new GridCollider();
					break;
				case "Circle_Collider":
					c = new CircleCollider();
					break;
				case "Script":
					c = new Script();
					break;
				case "Particle_Emitter":
					c = new ParticleEmitter();
					break;
				case "Point_Light":
					c = new PointLight();
					break;
				case "Audio_Source":
					c = new AudioSource();
					break;
				case "Audio_Listener":
					c = new AudioListener();
					break;
				}
				if(c != null)
				{
					c.setIdentificationName(name);
					gameObject.addComponent(c);
					component = c;
				}
			} else if((line = line.trim()).startsWith("<Field"))
			{
				String arg = line.split("value=\"")[1].split("\"")[0];
				fields.add(arg);
			}
			else if((line = line.trim()).startsWith("</Component"))	//End of component tag
			{
				if(component != null)
				{
					component.setEditableFields(fields.toArray(new String[fields.size()]), resourceManager, fleProject);
					fields.clear();
					component = null;
				}
			}
		}
		
		//gameObject.setupComponents();
		
		rdrGameObject.close();
	}
	
	
	
	
	/**Returns a list of all files in the directory and sub directories*/
	public final static List<File> findImportedResourcesInDir(String path)
	{
		List<File> files = new ArrayList<>();
		File dir = new File(path);
		
		for(File file : dir.listFiles())
		{
			if(file.isDirectory())
				files.addAll(findImportedResourcesInDir(file.getAbsolutePath()));	//If the file is a dir then recurse over this method for the sub dir
			else if(file.isFile())
				files.add(file);	//Add the file if it is normal and not a directory
		}
		
		return files;
	}
}
