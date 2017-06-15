package james.sugden.utils;

import static org.lwjgl.opengl.GL11.*;

public class Texture
{
	private String texPath, texName;
	
	private int texID;
	
	private int width, height;
	
	private boolean empty;
	
	public Texture()
	{
		this.empty = true;
	}
	
	public Texture(int texID, String texPath, String texName, int width, int height)
	{
		this.texID = texID;
		this.texPath = texPath;
		this.texName = texName;
		this.width = width;
		this.height = height;
		this.empty = false;
	}
	
	public final void bind()
	{
		if(!empty)
		{
			glEnable(GL_TEXTURE_2D);
			glBindTexture(GL_TEXTURE_2D, texID);
		}
	}
	
	public static void unbind()
	{
		glDisable(GL_TEXTURE_2D);
	}
	
	public final int getID()
	{
		return texID;
	}
	
	public final String getPath()
	{
		return texPath;
	}
	
	/**Name is the relative path of the texture compared to the images directory*/
	public final String getName()
	{
		return texName;
	}
	
	public final void setTexture(Texture t)
	{
		this.texID = t.getID();
		this.texPath = t.getPath();
		this.width = t.getWidth();
		this.height = t.getHeight();
		this.empty = false;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	/**Deletes the opengl texture*/
	public final void delete()
	{
		texPath = null;
		empty = true;
		width = height = 0;
		glDeleteTextures(texID);
	}
}
