uniform vec2 lightPos;
uniform vec3 lightColour;
uniform sampler2D texture;
uniform int texBound;

void main()
{
	float distance = length(lightPos - gl_FragCoord.xy) / 2;
	float attenuation = 1.0 / distance;
	vec4 colour = vec4(attenuation, attenuation, attenuation, pow(attenuation, 0.1)) * vec4(lightColour, 1);
	
	if(texBound == 1)
	{
		colour = colour * texture2D(texture, gl_TexCoord[0].st) * 25;
	}
	
	gl_FragColor = colour;
}