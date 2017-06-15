uniform sampler2D sampler;
uniform int lightCount;
uniform vec2 lightPos[64];
uniform vec3 lightColour[64];
uniform vec3 ambient;

void main()
{
	vec4 colour = vec4(0, 0, 0, 0);
	for(int i = 0; i < lightCount; i++)
	{
		float distance = length(lightPos[i] - gl_FragCoord.xy);
		float attenuation = 1.0 / distance;
		colour += (vec4(attenuation, attenuation, attenuation, pow(attenuation, 0.5)) * vec4(lightColour[i], 1));
	}
	
	colour += vec4(ambient, 0);
		
	if(textureSize(sampler, 0).x > 0)
		colour *= texture2D(sampler, gl_TexCoord[0].st);
	
	colour *= 16;
	
	gl_FragColor = colour;//mix(colour, texture2D(sampler, gl_TexCoord[0].st), 1);
}