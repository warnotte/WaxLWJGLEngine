#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec2 uvPos;
layout (location = 3) in vec3 aInstancePos;
layout (location = 4) in float aInstanceRotation;
layout (location = 5) in vec3 aInstanceScale;
layout (location = 6) in vec3 aInstanceColor; 

uniform mat4 projection;
uniform mat4 view;
uniform mat4 modelmatrice;
uniform float time;

out vec3 fragColor;
out vec2 uv;
out vec2 vLocalWorld;  // coordonnée locale en “world‐units”
out vec2 vSize;        // taille du rectangle en world‐units


void main() {
    // Rotation matrix (toujours en 2D pour la rotation Z)
    float cos_r = cos(aInstanceRotation);
    float sin_r = sin(aInstanceRotation);
    mat2 rotation = mat2(cos_r, -sin_r, sin_r, cos_r);
    
    // Apply scale, rotation, then translation
    // On prend seulement X et Y pour la rotation 2D
    vec2 scaledPos = aPos.xy * aInstanceScale.xy;
    vec2 rotatedPos = rotation * scaledPos;
    
    // Position finale en 3D
    vec3 finalPos = vec3(
        rotatedPos.x + aInstancePos.x,
        rotatedPos.y + aInstancePos.y,
        aPos.z * aInstanceScale.z + aInstancePos.z  // Scale et translation Z
    );
    
    // Animation optionnelle (commentée)
    finalPos.x += 0.1*cos(time*finalPos.y/50);
    finalPos.y += 0.1*sin(time*finalPos.x/50);
    
    // Position finale avec zDepth pour le depth testing
    gl_Position = projection * view * modelmatrice * vec4(finalPos.x, finalPos.y, finalPos.z/* + zDepth*/, 1.0);
    
    // TODO : Je n'ai pas l'impression que aInstanceColor recoit bien les données...
    fragColor = aInstanceColor;
    // je vais tenter de forcer la couleur ici pour debug -> Ici on voit bien le magenta.
    // fragColor = vec3(1, 0, 1);
    
    // 1) on normalise aPos → uv ∈ [0,1] (utilise seulement X,Y)
    vec2 uvnorm = aPos.xy + vec2(0.5);

    // 2) on applique la scale pour obtenir la taille "monde" (utilise seulement X,Y)
    vec2 sized = aInstanceScale.xy;
    vSize = sized;
    vLocalWorld = uvnorm * sized;  // Correction : uvnorm au lieu de uv
    
    // 3) sortir uv
    uv = uvPos;
}

/*
void main() {
    // Rotation matrix
    float cos_r = cos(aInstanceRotation);
    float sin_r = sin(aInstanceRotation);
    mat2 rotation = mat2(cos_r, -sin_r, sin_r, cos_r);
    
    // Apply scale, rotation, then translation
    vec2 scaledPos = aPos * aInstanceScale;
    vec2 rotatedPos = rotation * scaledPos;
    vec2 finalPos = rotatedPos + aInstancePos;
    
    // J'ai ajouté une matrice pour transformé le modèle.
    
    //finalPos.x += 0.1*cos(time*finalPos.y/50);
    //finalPos.y += 0.1*sin(time*finalPos.x/50);
    
    gl_Position = projection * view * modelmatrice * vec4(finalPos, zDepth, 1.0);
    fragColor = aInstanceColor;
    
    //OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
    fragColor = vec4(1,0,1,1);
    
    
     // 1) on normalise aPos → uv ∈ [0,1]
    vec2 uvnorm = aPos + vec2(0.5);

    // 2) on applique la scale pour obtenir la taille “monde”
    vec2 sized = aInstanceScale;
    vSize       = sized;
    vLocalWorld = uv * uvnorm;
    
    // 3) sortir uv
    uv = uvPos;

    
}

*/
