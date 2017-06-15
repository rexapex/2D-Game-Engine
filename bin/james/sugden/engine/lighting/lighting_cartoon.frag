uniform sampler2D texture;
uniform int texBound;
uniform int lightCount;
uniform vec2 lightPos[64];
uniform vec3 lightColour[64];

void main()
{
	vec4 colour = vec4(0, 0, 0, 0);
	for(int i = 0; i < lightCount; i++)
	{
		float distance = length(lightPos[i] - gl_FragCoord.xy);
		float attenuation = 1.0 / distance;
		colour += (vec4(attenuation, attenuation, attenuation, pow(attenuation, 0.5)) * vec4(lightColour[i], 1));
	}
	
	colour *= 16;
	
	if(texBound == 1)
	{
		colour *= texture2D(texture, gl_TexCoord[0].st);
	}
	
	gl_FragColor = colour;
}