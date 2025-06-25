#version 330 core
in vec3 fragColor;
out vec4 FragColor;
in vec2 uv;

in vec2 vLocalWorld;
in vec2 vSize;

uniform float uBorderPx;

void main() {

    vec2 pixelSize = fwidth(vLocalWorld);
    float worldPerPx = max(pixelSize.x, pixelSize.y);

    // 2) on convertit l’épaisseur en monde
    float borderWorld = uBorderPx * worldPerPx;

    // 3) distance réelle (monde) jusqu’au plus proche bord
    float distWorld = min(
        min(vLocalWorld.x, vSize.x - vLocalWorld.x),
        min(vLocalWorld.y, vSize.y - vLocalWorld.y)
    );

    // 4) si on est à l’intérieur de cette zone de borderWorld
    if (distWorld < borderWorld) {
        FragColor = vec4(0,0,0,1);
    } else {
    
    	// Test pour les UV
    	float r,g,b;
    	r = uv.x;
    	g = uv.y;
    	b = 0;
        FragColor = vec4(vec3(r,g,b), 1.0);
        FragColor = vec4(fragColor, 1.0);
    }
   
}