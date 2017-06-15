package james.sugden.utils;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alSourcei;
import james.sugden.engine.audio.AudioSource;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class AudioBuffer
{
	private IntBuffer buffer;
	
	private String path, name;
	
	private List<Integer> sources;
	
	public AudioBuffer(IntBuffer buffer, String path, String name)
	{
		this.buffer = buffer;
		this.path = path;
		this.name = name;
		this.sources = new ArrayList<>();
	}
	
	public int createSource()
	{
		int source = alGenSources();
		alSourcei(source, AL_BUFFER, buffer.get(0));
		sources.add(source);
		
		return source;
	}
	
	public void deleteSource(AudioSource source)
	{
		alDeleteSources(source.getSource());
	}
	
	/**Deletes the buffers and all audio sources using the buffer*/
	public void deleteBuffer()
	{
		System.out.println("Deleted buffer");
		for(int source : sources)
		{
			alDeleteSources(source);
		}
		alDeleteBuffers(buffer.get(0));
	}
	
	public String getPath()
	{
		return path;
	}
	
	public String getName()
	{
		return name;
	}
}
