package claude.graphics;

import static org.lwjgl.opengl.GL20.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class ShaderProgram {
    private final int programId;
    private final Map<String, Integer> uniforms;
    private Shader vertexShader;
    private Shader fragmentShader;
    
    public ShaderProgram(String vertexSource, String fragmentSource) {
        programId = glCreateProgram();
        uniforms = new HashMap<>();
        
        // Utiliser la classe Shader
        vertexShader = new Shader(GL_VERTEX_SHADER, vertexSource);
        fragmentShader = new Shader(GL_FRAGMENT_SHADER, fragmentSource);
        
        // Attacher les shaders
        glAttachShader(programId, vertexShader.getId());
        glAttachShader(programId, fragmentShader.getId());
        
        // Lier le programme
        glLinkProgram(programId);
        
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader linking failed: " + 
                glGetProgramInfoLog(programId));
        }
        
        // Détacher les shaders après le linking
        glDetachShader(programId, vertexShader.getId());
        glDetachShader(programId, fragmentShader.getId());
        
        // Nettoyer les shaders (ils ne sont plus nécessaires après le linking)
        vertexShader.cleanup();
        fragmentShader.cleanup();
    }
    
    public void createUniform(String uniformName) {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new RuntimeException("Uniform not found: " + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }
    
    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);
            glUniformMatrix4fv(uniforms.get(uniformName), false, buffer);
        }
    }
    
    public void setUniform(String uniformName, float value) {
        glUniform1f(uniforms.get(uniformName), value);
    }
    
    public void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }
    
    public void setUniform(String uniformName, float x, float y) {
        glUniform2f(uniforms.get(uniformName), x, y);
    }
    
    public void setUniform(String uniformName, float x, float y, float z) {
        glUniform3f(uniforms.get(uniformName), x, y, z);
    }
    
    public void setUniform(String uniformName, float x, float y, float z, float w) {
        glUniform4f(uniforms.get(uniformName), x, y, z, w);
    }
    
    public void setUniform(String uniformName, Vector2f value) {
        glUniform2f(uniforms.get(uniformName), value.x, value.y);
    }
    
    public void setUniform(String uniformName, Vector3f value) {
        glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
    }
    
    public void setUniform(String uniformName, Vector4f value) {
        glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }
    
    public void bind() {
        glUseProgram(programId);
    }
    
    public void unbind() {
        glUseProgram(0);
    }
    
    public void cleanup() {
        glDeleteProgram(programId);
    }
}