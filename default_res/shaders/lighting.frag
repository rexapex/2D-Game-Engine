const int MAX_NUM_SPOT_LIGHTS = 64;
const int MAX_NUM_POINT_LIGHTS = 64;

uniform int numPointLights;
uniform int numSpotLights;

struct PointLight
{
	vec2 pos;
	float depth;
	
	vec3 ambientColour;
	vec3 diffuseColour;
	
	float intensity;
	
	//Attenuation
	float constant;
	float linear;
	float quadratic;
};

struct SpotLight
{
	vec2 pos;
	vec2 dir;				//Direction of the light source
	float depth;

	vec3 ambientColour;
	vec3 diffuseColour;

	float cosInnerCutoff;	//Cosine of the inner cutoff angle
	float cosOuterCutoff;	//Cosine of the outer cutoff angle
	
	float intensity;
	
	float constant;
	float linear;
	float quadratic;
};

uniform vec3 ambientColour;
uniform float ambientIntensity;

uniform PointLight pointLights[MAX_NUM_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_NUM_SPOT_LIGHTS];


uniform sampler2D sampler;





vec3 doPointLighting(vec2 fragPos, float fragDepth, vec3 texColour)
{
	vec3 colour = vec3(0.0, 0.0, 0.0);

	for(int i = 0; i < numPointLights && i < MAX_NUM_POINT_LIGHTS; i++)
	{
		if(pointLights[i].depth >= fragDepth)//fragDepth*2-1)
			continue;
		
		//Ambient
	    vec3 ambient = pointLights[i].intensity * pointLights[i].ambientColour;
	    
	    //Diffuse 
	    vec2 lightDir = normalize(pointLights[i].pos - fragPos);	//Direction from fragment to light source
		//float diff = max(dot(surfaceNormal, lightDir), 0.0);
    	vec3 diffuse = pointLights[i].intensity * pointLights[i].diffuseColour;// * diff;
		
	    //Attenuation
	    float distance = length(pointLights[i].pos - fragPos);
	    float attenuation = 1.0 / (pointLights[i].constant + pointLights[i].linear * distance + pointLights[i].quadratic * (distance * distance));
	    
	    colour += (attenuation * (ambient + diffuse)) * texColour;
	}

	return colour;
}

vec3 doSpotLighting(vec2 fragPos, float fragDepth, vec3 texColour)
{
	vec3 colour = vec3(0.0, 0.0, 0.0);

	for(int i = 0; i < numSpotLights && i < MAX_NUM_SPOT_LIGHTS; i++)
	{
		//Ambient
	    vec3 ambient = spotLights[i].intensity * spotLights[i].ambientColour;
	    
	    //Diffuse 
	    vec2 lightDir = normalize(spotLights[i].pos - fragPos);	//Direction from fragment to light source
		//float diff = max(dot(surfaceNormal, lightDir), 0.0);
    	vec3 diffuse = spotLights[i].intensity * spotLights[i].diffuseColour;// * diff;
		
	    //Spotlight (soft edges)
	    float theta = dot(lightDir, normalize(-spotLights[i].dir)); 
	    float epsilon = (spotLights[i].cosInnerCutoff - spotLights[i].cosOuterCutoff);
	    float intensity = clamp(spotLights[i].intensity * (theta - spotLights[i].cosOuterCutoff) / epsilon, 0.0, 1.0);
	    
	    ambient *= intensity;
	    diffuse *= intensity;
	    
	    //Attenuation
	    float distance = length(spotLights[i].pos - fragPos);
	    float attenuation = 1.0 / (spotLights[i].constant + spotLights[i].linear * distance + spotLights[i].quadratic * (distance * distance));
	    
	    colour += (attenuation * (ambient + diffuse)) * texColour;
	}

	return colour;
}









void main()
{ 
	vec2 fragPos	   = gl_FragCoord.xy;
	vec4 texColour	   = texture(sampler, gl_TexCoord[0].st);
	float fragDepth	   = gl_FragCoord.z;

	//outColour = texColour;

	//vec3 viewDir = normalize(-fragPos);	//(ViewDir = CamPos - CamSpacePos) but CamPos = (0,0,0) because in cam space

	vec3 colour = vec3(0.0, 0.0, 0.0);

	colour += doPointLighting(fragPos, fragDepth, texColour);
	colour += doSpotLighting(fragPos, fragDepth, texColour);
	
	colour += ambientIntensity * ambientColour * vec3(texColour.rgb);
	
	//float gamma = 2.2;
	//colour.rgb = pow(colour.rgb, vec3(1.0/gamma));	//Apply gamma correction
	
	gl_FragColor = vec4(colour, texColour.a);
}




















