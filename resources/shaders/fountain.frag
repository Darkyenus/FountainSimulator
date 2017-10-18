
varying vec4 v_color;
/* Texture coordinates, 0-1, zero is in top left corner */
varying vec2 v_texCoords;

/* Texture that is being drawn */
uniform sampler2D u_texture;
/* Time it takes for u_texture to start appearing after t=0 */
uniform float textureOffset;
/* Time it takes for u_texture to stop falling after it starts */
uniform float textureHeight;
/* Total loop length */
uniform float totalTextureHeight;

/* Texture with drop color */
uniform sampler2D colorTexture;
/* U, V top, V bottom */
uniform vec3 colorTextureCoords;

/* Current time */
uniform float time;
/* Some acceleration factor */
uniform float acceleration;


void main() {
	float tOffset = sqrt(max(2.0 * v_texCoords.y / acceleration, 0.0));
	float t = time - tOffset;
	t -= textureOffset;

	// Sanitize
	while (t < 0.0) {
		t += totalTextureHeight;
	}
	while (t >= totalTextureHeight) {
		t -= totalTextureHeight;
	}

	// Check if in range
	if (t < 0.0 || t > textureHeight) {
		discard;
	}

	// Draw the texture!
	float y = 1.0 - (t / textureHeight);

	vec4 color = texture2D(u_texture, vec2(v_texCoords.x, y));

	if (color.r < 0.5) {
		vec4 color = texture2D(colorTexture, vec2(colorTextureCoords.x, mix(colorTextureCoords.y, colorTextureCoords.z, v_texCoords.y)));
		//color = vec4(0.6, 0.9, 0.95, 0.7);

		gl_FragColor = color * v_color;
	} else {
		//gl_FragColor = texture2D(colorTexture, v_texCoords.xy);
		discard;
	}
}