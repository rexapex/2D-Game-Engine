package james.sugden.file_handling;

import james.sugden.engine.audio.AudioSource;
import james.sugden.engine.game_object.Component;
import james.sugden.engine.game_object.GameObject;
import james.sugden.engine.rendering.ImageRenderer;
import james.sugden.engine.scripts.JPGEngineScript;
import james.sugden.engine.scripts.Script;
import james.sugden.utils.AudioBuffer;
import james.sugden.utils.AudioLoader;
import james.sugden.utils.Texture;
import james.sugden.utils.TextureLoader;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public final class ResourceManager
{
	/**Reference to the project*/
	private File fleProject;
	
	/**List of all the textures loaded into the current scene
	 * Repeated textures are only listed once*/
	private List<Texture> textures;
	
	/**List of all textures which need loading*/
	private Queue<TextureToLoad> texturesToLoad;
	
	/**Paths of the images imported
	 * If they are requested to be loaded in they are added to the queue*/
	private List<String> importedTexturePaths;
	
	/**List of all the textures loaded into the current scene
	 * Repeated textures are only listed once*/
	private List<AudioBuffer> audioBuffers;
	
	/**List of all textures which need loading*/
	private Queue<AudioBufferToLoad> audioBuffersToLoad;
	
	/**Paths of the images imported
	 * If they are requested to be loaded in they are added to the queue*/
	private List<String> importedAudioBufferPaths;
	
	public ResourceManager(File fleProject)
	{
		this.fleProject = fleProject;
		this.init();
	}
	
	private final void init()
	{
		textures = new ArrayList<>();
		texturesToLoad = new LinkedList<>();
		importedTexturePaths = new ArrayList<>();
		audioBuffers = new ArrayList<>();
		audioBuffersToLoad = new LinkedList<>();
		importedAudioBufferPaths = new ArrayList<>();
	}
	
	
	
	
	
	
	/**Loads in texture from a queue
	 * Only needs to be called from the scene editor's update method
	 * Is not called when running in game mode*/
	public final void update()
	{
		int texturesLoadedThisUpdate = 0;
		while(!texturesToLoad.isEmpty() && texturesLoadedThisUpdate < 5)
		{
			TextureToLoad t = texturesToLoad.remove();
			Texture tex = null;
			if(t.path != null)
			{
				tex = TextureLoader.loadTexture(fleProject.getAbsolutePath() + "/res/images/" + t.path, fleProject);
				textures.add(tex);
			} else if(t.img != null)
			{
				tex = TextureLoader.loadTexture(t.img);
			}
			for(ImageRenderer renderer : t.renderers)
			{
				if(renderer != null && tex != null)
				{
					renderer.setTexture(tex);
				}
			}
			texturesLoadedThisUpdate++;
		}
		
		int audioBuffersLoadedThisUpdate = 0;
		while(!audioBuffersToLoad.isEmpty() && audioBuffersLoadedThisUpdate < 5)
		{
			AudioBufferToLoad b = audioBuffersToLoad.remove();
			AudioBuffer buffer = null;
			System.out.println(b);
			if(b.path != null)
			{
				buffer = AudioLoader.loadAudio(fleProject.getAbsolutePath() + "/res/sounds/" + b.path, fleProject);
				audioBuffers.add(buffer);
		//		System.out.println(b.path);
			}
			for(AudioSource source : b.audioSources)
			{
				if(source != null && buffer != null)
				{
					source.setSourceAndBuffer(buffer.createSource(), buffer);
				}
			}
			audioBuffersLoadedThisUpdate++;
		}
	}
	
	
	
	
	
	
	
	
	
	/**Loads in every resource at once*/
	public final void loadAllResources()
	{
		while(!texturesToLoad.isEmpty())
		{
			TextureToLoad t = texturesToLoad.remove();
			Texture tex = null;
			if(t.path != null)
			{
				tex = TextureLoader.loadTexture(fleProject.getAbsolutePath() + "/res/images/" + t.path, fleProject);
				textures.add(tex);
			} else if(t.img != null)
			{
				tex = TextureLoader.loadTexture(t.img);
			}
					
			for(ImageRenderer renderer : t.renderers)
			{
				if(renderer != null && tex != null)
				{
					renderer.setTexture(tex);
				}
			}
		}
		
		while(!audioBuffersToLoad.isEmpty())
		{
			AudioBufferToLoad b = audioBuffersToLoad.remove();
			AudioBuffer buffer = null;
		//	System.out.println(b);
			if(b.path != null)
			{
				buffer = AudioLoader.loadAudio(fleProject.getAbsolutePath() + "/res/sounds/" + b.path, fleProject);
				audioBuffers.add(buffer);
				System.out.println(b.path);
			}
			for(AudioSource source : b.audioSources)
			{
				if(source != null && buffer != null)
				{
					source.setSourceAndBuffer(buffer.createSource(), buffer);
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**Convenience method for finding, loading & importing audio buffers
	 * @returns Texture object if the texture has already been loaded
	 * @param texExists[0] will store value true after execution iff tex path exists and texExists.length > 0*/
	public final Texture findLoadImportTexture(String path, ImageRenderer renderer, boolean[] texExists)
	{
		Texture tex = findTexture(path);
		if(tex == null)
		{
			if(findImportedTexturePath(path))
			{
				addTextureToLoad(path, renderer);
				if(texExists.length > 0)
					texExists[0] = true;
			}
			else
				if(texExists.length > 0)
					texExists[0] = false;
		}
		else
		{
			addTextureToLoad(path, renderer);
			if(texExists.length > 0)
				texExists[0] = true;
		}
		
		return tex;
	}
	
	/**Returns the texture specified by the given path
	 * Returns null if texture does not exist*/
	public final Texture findTexture(String path)
	{
		for(Texture t : textures)
		{
			if(t != null && new File(t.getPath()).getName().equals(path))
			{
				return t;
			}
		}
		return null;
	}
	
	/**Adds the given texture to the texture list if it is not already added
	 * Returns true if the texture was added and false if it already has been added*/
	public final boolean addTexture(Texture tex)
	{
		for(Texture t : textures)
		{
			if(!tex.equals(t))
			{
				textures.add(tex);
				return true;
			}
		}
		return false;
	}
	
	/**Adds a texture path to the queue of texture to load
	 * Returns false if the texture path has already been initialised and true if it has been added to the queue*/
	public final boolean addTextureToLoad(String texturePath, ImageRenderer renderer)
	{
		for(Texture t : textures)
		{
			if(t != null && texturePath.equals(t.getPath()))
			{
				renderer.setTexture(t);
				return false;
			}
		}
		for(TextureToLoad t : texturesToLoad)
		{
			if(t != null && texturePath.equals(t.path))
			{
				t.renderers.add(renderer);
				return false;
			}
		}
		texturesToLoad.add(new TextureToLoad(texturePath, renderer));
		return true;
	}
	
	/**Adds a texture path to the queue of texture to load
	 * Returns false if the texture path has already been initialised and true if it has been added to the queue*/
	public final boolean addTextureToLoad(BufferedImage img, ImageRenderer renderer)
	{
		texturesToLoad.add(new TextureToLoad(img, renderer));
		return true;
	}
	
	/**Adds a path to the imported image path list*/
	public final void addImportedTexturePath(String path)
	{
		importedTexturePaths.add(path);
	}
	
	/**Returns true if the texture path exists*/
	public final boolean findImportedTexturePath(String path)
	{
		for(String s : importedTexturePaths)
		{
			if(s.equals(path))
			{
				return true;
			}
		}
		return false;
	}
	
	public final List<String> getImportedTexturePaths()
	{
		return importedTexturePaths;
	}
	
	
	
	
	
	
	
	
	
	
	/**Convenience method for finding, loading & importing audio buffers*/
	public final AudioBuffer findLoadImportAudioBuffer(String path, AudioSource source)
	{
		AudioBuffer buffer = findAudioBuffer(path);
		if(buffer == null)
		{
			if(findImportedAudioBufferPath(path))
				addAudioBufferToLoad(path, source);
		}
		else
		{
			addAudioBufferToLoad(path, source);
		}
		
		return buffer;
	}
	
	/**Returns the audio buffer specified by the given path
	 * Returns null if audio buffer does not exist*/
	public final AudioBuffer findAudioBuffer(String path)
	{
		for(AudioBuffer t : audioBuffers)
		{
			if(t != null && new File(t.getPath()).getName().equals(path))
			{
				return t;
			}
		}
		return null;
	}
	
	/**Adds the given audio buffer to the audio buffer list if it is not already added
	 * Returns true if the audio buffer was added and false if it already has been added*/
	public final boolean addAudioBuffer(AudioBuffer buffer)
	{
		for(AudioBuffer t : audioBuffers)
		{
			if(!buffer.equals(t))
			{
				audioBuffers.add(buffer);
				return true;
			}
		}
		return false;
	}
	
	/**Adds an audio buffer path to the queue of audio buffers to load
	 * Returns false if the audio buffer path has already been initialised and true if it has been added to the queue*/
	public final boolean addAudioBufferToLoad(String path, AudioSource src)
	{
		for(AudioBuffer t : audioBuffers)
		{
			if(t != null && path.equals(t.getPath()))
			{
				src.setSourceAndBuffer(t.createSource(), t);
				return false;
			}
		}
		for(AudioBufferToLoad t : audioBuffersToLoad)
		{
			if(t != null && path.equals(t.path))
			{
				t.audioSources.add(src);
				return false;
			}
		}
		audioBuffersToLoad.add(new AudioBufferToLoad(path, src));
		return true;
	}
	
	/**Adds a path to the imported audio buffer path list*/
	public final void addImportedAudioBufferPath(String path)
	{
		System.out.println(path);
		importedAudioBufferPaths.add(path);
	}
	
	/**Returns true if the audio buffer path exists*/
	public final boolean findImportedAudioBufferPath(String path)
	{
		for(String s : importedAudioBufferPaths)
		{
			if(s.equals(path))
			{
				return true;
			}
		}
		return false;
	}
	
	public final List<String> getImportedAudioBufferPaths()
	{
		return importedAudioBufferPaths;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**Creates a file with the given name in the script directory if the file does not exist*/
	public final File createScriptFile(String name)
	{
		if(!name.endsWith(".java"))
			name += ".java";
		File fleScript = new File(fleProject.getAbsolutePath() + "/res/scripts/" + name);
		if(!fleScript.exists())
		{
			try {
				fleScript.createNewFile();
				BufferedWriter wrtScript = new BufferedWriter(new FileWriter(fleScript));
				
				String initialCode =   "import james.sugden.engine.scripts.JPGEngineScript;" + System.lineSeparator()
									 + "import james.sugden.engine.game_object.GameObject;" + System.lineSeparator()+System.lineSeparator()
								     + "public class " + name.substring(0, name.length()-5) + " extends JPGEngineScript" + System.lineSeparator()
									 + "{" + System.lineSeparator()
									 + "	public void init(GameObject thisGameObject)    //Initialise the script" + System.lineSeparator()
									 + "	{" + System.lineSeparator()
									 + "		" + System.lineSeparator()
									 + "	}" + System.lineSeparator()
									 + "	" + System.lineSeparator()
									 + "	public void update(GameObject thisGameObject)    //Called every update of the game" + System.lineSeparator()
									 + "	{" + System.lineSeparator()
									 + "		" + System.lineSeparator()
									 + "	}" + System.lineSeparator()
									 + "}";
				wrtScript.write(initialCode);
									 
				wrtScript.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fleScript;
	}
	
	/**Compile scripts*/
	public final static void compileScripts(List<GameObject> gameObjects)
	{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		ArrayList<String> loadedScripts = new ArrayList<>();	//Used so each script is only compiled once
		for(GameObject gameObject : gameObjects)	//Only compiles scripts for the current scene
		{
			for(Component component : gameObject.getComponents())
			{
				if(component instanceof Script)
				{
					File fleScript = ((Script)component).getScriptFile();
					if(fleScript != null)
					{
						if(!loadedScripts.contains(fleScript.getPath()))
						{
							loadedScripts.add(fleScript.getPath());
							int compilationResult = compiler.run(null, null, null, fleScript.getPath());	//Compile scripts
							if(compilationResult == 0)
								System.out.println("Successfully compiled " + fleScript.getName());
							else
								System.err.println("Failed to compile " + fleScript.getName());
						}
						
						//Load the class and create an instance
						File fleScriptRoot = new File((fleScript.getAbsolutePath().substring(0, fleScript.getAbsolutePath().length()-fleScript.getName().length()-1)));//.replaceAll(" ", File.));
						try {
							URL url = fleScriptRoot.toURI().toURL();
							URLClassLoader loader = new URLClassLoader(new URL[]{url});
							try {
								try {
									JPGEngineScript instance = (JPGEngineScript)loader.loadClass(fleScript.getName().replace(".java", "")).newInstance();
									((Script)component).setJPGEngineScript(instance);
								} catch (InstantiationException
										| IllegalAccessException e) {
									e.printStackTrace();
								}
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							} finally
							{
								try {
									loader.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		loadedScripts.clear();
	}
	
	/**Returns true if the script passed exists in the resources folder as a .java file*/
	public final File doesScriptExist(String path)
	{
		File fleScript = new File(fleProject + "/res/scripts/" + path);
		return fleScript.exists() ? fleScript : null;
	}
	
	
	
	
	
	
	
	
	
	/**Clean up resources*/
	public final void close()
	{
		for(Texture t : textures)
		{
			System.out.println("Deleted texture: " + t.getPath());
			t.delete();
		}
		for(AudioBuffer b : audioBuffers)
		{
			System.out.println("Deleted audio buffer: " + b.getPath());
			b.deleteBuffer();
		}
	}
	
	
	
	
	
	
	
	
	/**Returns the project directory*/
	public final File getProjectDir()
	{
		return fleProject;
	}
	
	
	
	
	
	
	
	
	
	
	private class TextureToLoad
	{
		private String path;
		private List<ImageRenderer> renderers;
		private BufferedImage img;
		
		/**Using this constructor means the texture will be added to the texture list*/
		private TextureToLoad(String path, ImageRenderer renderer)
		{
			this.renderers = new ArrayList<>();
			renderers.add(renderer);
			this.path = path;
			img = null;
		}
		
		/**Using this constructor means the texture won't be added to the texture list*/
		private TextureToLoad(BufferedImage img, ImageRenderer renderer)
		{
			renderers = new ArrayList<>();
			renderers.add(renderer);
			this.img = img;
			path = null;
		}
	}
	
	private class AudioBufferToLoad
	{
		private String path;
		private List<AudioSource> audioSources;
		
		/**Using this constructor means the sound will be added to the sound list*/
		private AudioBufferToLoad(String path, AudioSource source)
		{
			this.audioSources = new ArrayList<>();
			this.audioSources.add(source);
			this.path = path;
		}
	}
}
