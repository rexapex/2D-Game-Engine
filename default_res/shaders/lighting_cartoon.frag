uniform sampler2D sampler;
uniform int lightCount;
uniform vec2 lightPos[64];
uniform vec3 lightColour[64];

void main()
{
	vec4 colour = vec4(0, 0, 0, 1);
	//if(textureSize(sampler, 0).x > 0)
	//	colour = texture2D(sampler, gl_TexCoord[0].st);
	
	for(int i = 0; i < lightCount; i++)
	{
		float distance = length(lightPos[i] - gl_FragCoord.xy);
		float attenuation = 1.0 / distance;
		colour.rgb += (vec3(attenuation, attenuation, attenuation) * vec3(lightColour[i]));
		colour.a *= (1 - attenuation) * 1;
	}
	
	gl_FragColor = colour;//mix(colour, texture2D(sampler, gl_TexCoord[0].st), 1);
}