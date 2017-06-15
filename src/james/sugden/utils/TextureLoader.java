package james.sugden.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;

import static org.lwjgl.opengl.GL11.*;

public class TextureLoader
{
	public final static Texture loadTexture(File file, File fleProject)
	{
		Texture tex;
		if(file.exists())
		{
			try {
				BufferedImage img = ImageIO.read(file);
				tex = new Texture(decodePNG(img), file.getPath(),
						file.getPath().substring((fleProject.getAbsolutePath() + "/res/images").length() + 1).replaceAll("\\\\", "/"),
						img.getWidth(), img.getHeight());
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed to load texture");
				return null;
			}
		} else
		{
			System.out.println("Texture doesn't exist");
			return null;
		}
		
		return tex;
	}
	
	public final static Texture loadTexture(String path, File fleProject)
	{
		File file = new File(path);
		return loadTexture(file, fleProject);
	}
	
	public final static Texture loadTexture(BufferedImage img)			//Only used for cuts of sprite sheet as they do not need to be directly referenced by name
	{
		return new Texture(decodePNG(img), "null", "null", img.getWidth(), img.getHeight());
	}
	
	private static final int decodePNG(BufferedImage img)
	{
		ByteBuffer buffer = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 4);
		
		int[] pixels = new int[img.getWidth() * img.getHeight()];
		img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
		
		for(int y = 0; y < img.getHeight(); y++)
		{
			for(int x = 0; x < img.getWidth(); x++)
			{
				int pixel = pixels[y * img.getWidth() + x];
	            buffer.put((byte)((pixel >> 16) & 0xFF));
	            buffer.put((byte)((pixel >> 8) & 0xFF));
	            buffer.put((byte)(pixel & 0xFF)); 
	            buffer.put((byte)((pixel >> 24) & 0xFF));
			}
		}
		
		//VERY IMPORTANT
		buffer.flip();
		
		int id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		
		//Setup wrap modeglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        
        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, img.getWidth(), img.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		
		return id;
	}
	
	/**Generates an empty opengl texture id and returns the id in a texture object*/
	public static final Texture genTexture(int width, int height)
	{
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
	            buffer.put((byte)1);
	            buffer.put((byte)1);
	            buffer.put((byte)1); 
	            buffer.put((byte)0);
			}
		}
		
		buffer.flip();
		
		int id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		
		//Setup wrap modeglTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        
        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		
		return new Texture(id, null, null, width, height);
	}
}
