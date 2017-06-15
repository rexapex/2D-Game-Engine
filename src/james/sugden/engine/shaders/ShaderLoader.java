package james.sugden.engine.shaders;

import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.lwjgl.opengl.GL11;


public class ShaderLoader
{
	/**Creates and returns a shader component with the loaded vertex and fragment shaders
	 * To not include a type of shader, pass null*/
	public static Shader loadShader(String fragShaderPath, String vertShaderPath)
	{
		int shaderProgram = glCreateProgram();
		
		
		
		if(vertShaderPath != null)	//If a vertex shader path is supplied then load the vertex shader
		{
			int vertexShader = glCreateShader(GL_VERTEX_SHADER);
			StringBuilder vertexShaderSrc = new StringBuilder();
			
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(vertShaderPath));
				String line;
				while((line = reader.readLine()) != null)
				{
					vertexShaderSrc.append(line).append("\n");
				}
				reader.close();
			} catch(IOException e)
			{
				System.err.println("Failed to Load Vertex Shader");
				System.exit(1);
			}
			
			glShaderSource(vertexShader, vertexShaderSrc);
			glCompileShader(vertexShader);
			if(glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL11.GL_FALSE)
			{
				System.err.println("Failed to Compile Vertex Shader");
				System.err.println(glGetShaderInfoLog(vertexShader, 1024));
			}
			
			glAttachShader(shaderProgram, vertexShader);
			glDeleteShader(vertexShader);
		}
		
		
		
		if(fragShaderPath != null)	//If a fragment shader path is supplied then load the fragment shader
		{
			int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
			StringBuilder fragmentShaderSrc = new StringBuilder();
			
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(fragShaderPath));
				String line;
				while((line = reader.readLine()) != null)
				{
					fragmentShaderSrc.append(line).append("\n");
				}
				reader.close();
			} catch(IOException e)
			{
				System.err.println("Failed to Load Fragment Shader");
				System.exit(1);
			}
			
			glShaderSource(fragmentShader, fragmentShaderSrc);
			glCompileShader(fragmentShader);
			if(glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL11.GL_FALSE)
			{
				System.err.println("Failed to Compile Fragment Shader");
				System.err.println(glGetShaderInfoLog(fragmentShader, 1024));
			}
			
			glAttachShader(shaderProgram, fragmentShader);
			glDeleteShader(fragmentShader);
		}
		
		
		
		glLinkProgram(shaderProgram);
		glValidateProgram(shaderProgram);
		
		Shader shader = new Shader(shaderProgram);
		
		return shader;
	}
}
