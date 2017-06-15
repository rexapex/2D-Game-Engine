package james.sugden.utils;

import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alGenBuffers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.WaveData;

public class AudioLoader
{
	public static AudioBuffer loadAudio(String path, File fleProject)
	{
		if(path.endsWith(".wav"))
		{
			File file = new File(path);
			try {
				return new AudioBuffer(loadWAV(path), path,
						file.getAbsolutePath().substring((fleProject.getAbsolutePath() + "/res/sounds").length() + 1).replaceAll("\\\\", "/"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		} else
		{
			return null;
		}
	}
	
	private static IntBuffer loadWAV(String path) throws FileNotFoundException
	{
		WaveData data = WaveData.create(new BufferedInputStream(new FileInputStream(path)));
		IntBuffer buffer = BufferUtils.createIntBuffer(1);
		alGenBuffers(buffer);
		alBufferData(buffer.get(0), data.format, data.data, data.samplerate);
		data.dispose();
		
		return buffer;
	}
}
