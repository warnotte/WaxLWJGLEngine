package claude.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

public class InstancedMesh {
    private int vao, vbo, ebo, instanceVBO;
    private int vertexCount;
    private int instanceCount;
    
    public InstancedMesh(float[] vertices, int[] indices, float[] instanceData, int instanceCount) {
        this.vertexCount = indices.length;
        this.instanceCount = instanceCount;
        
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();
        instanceVBO = glGenBuffers();
        
        glBindVertexArray(vao);
        
        // Upload vertex data
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        
        // Upload indices
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        
        // Setup vertex attributes
        setupVertexAttributes();
        
        // Upload instance data
        glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
        glBufferData(GL_ARRAY_BUFFER, instanceData, GL_STATIC_DRAW);
        
        // Setup instance attributes
        setupInstanceAttributes();
        
        glBindVertexArray(0);
    }
    
    private void setupVertexAttributes() {
        // Position (location = 0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // Color (location = 1)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        // UV (location = 2)
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
    }
    
    private void setupInstanceAttributes() {
        int stride = 10 * Float.BYTES;
        
        // Instance position (location = 3)
        glVertexAttribPointer(3, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(3);
        glVertexAttribDivisor(3, 1);
        
        // Instance rotation (location = 4)
        glVertexAttribPointer(4, 1, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(4);
        glVertexAttribDivisor(4, 1);
        
        // Instance scale (location = 5)
        glVertexAttribPointer(5, 3, GL_FLOAT, false, stride, 4 * Float.BYTES);
        glEnableVertexAttribArray(5);
        glVertexAttribDivisor(5, 1);
        
        // Instance color (location = 6)
        glVertexAttribPointer(6, 3, GL_FLOAT, false, stride, 7 * Float.BYTES);
        glEnableVertexAttribArray(6);
        glVertexAttribDivisor(6, 1);
    }
    
    public void render() {
        glBindVertexArray(vao);
        glDrawElementsInstanced(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0, instanceCount);
        glBindVertexArray(0);
    }
    
    public void cleanup() {
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteBuffers(instanceVBO);
        glDeleteVertexArrays(vao);
    }
}