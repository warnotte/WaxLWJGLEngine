package claude.graphics;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int shaderId;
    
    public Shader(int type, String source) {
        shaderId = glCreateShader(type);
        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader of type: " + type);
        }
        
        glShaderSource(shaderId, source);
        glCompileShader(shaderId);
        
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error compiling shader: " + 
                glGetShaderInfoLog(shaderId));
        }
    }
    
    public int getId() {
        return shaderId;
    }
    
    public void cleanup() {
        glDeleteShader(shaderId);
    }
}