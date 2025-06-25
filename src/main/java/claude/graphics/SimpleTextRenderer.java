package claude.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;

import claude.core.Window;

import java.util.ArrayList;
import java.util.List;

public class SimpleTextRenderer {
    private static class Label {
        String text;
        float worldX, worldY;
        float r, g, b, a;
        
        Label(String text, float x, float y, float r, float g, float b, float a) {
            this.text = text;
            this.worldX = x;
            this.worldY = y;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
    }
    
    private ShaderProgram labelShader;
    private int vao, vbo;
    private List<Label> labels = new ArrayList<>();
    
    private static final String LABEL_VERTEX_SHADER = """
        #version 330 core
        layout (location = 0) in vec2 aPos;
        
        uniform mat4 projection;
        uniform mat4 view;
        uniform vec2 worldPos;
        uniform vec2 pixelSize;
        uniform vec2 windowSize;
        
        void main() {
            // Position monde vers position écran
            vec4 pos = projection * view * vec4(worldPos, 0.0, 1.0);
            
            // Convertir la taille en pixels en NDC (Normalized Device Coordinates)
            vec2 ndcSize = pixelSize / windowSize * 2.0;
            
            // Appliquer la taille au vertex
            vec2 vertexOffset = aPos * ndcSize;
            
            gl_Position = vec4(pos.xy + vertexOffset, 0.0, 1.0);
        }
        """;
    
    private static final String LABEL_FRAGMENT_SHADER = """
        #version 330 core
        out vec4 FragColor;
        
        uniform vec4 color;
        
        void main() {
            FragColor = color;
        }
        """;
    
    public SimpleTextRenderer() {
        // Créer le shader
        labelShader = new ShaderProgram(LABEL_VERTEX_SHADER, LABEL_FRAGMENT_SHADER);
        labelShader.createUniform("projection");
        labelShader.createUniform("view");
        labelShader.createUniform("worldPos");
        labelShader.createUniform("pixelSize");
        labelShader.createUniform("windowSize");
        labelShader.createUniform("color");
        
        // Créer un quad simple
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        
        float[] quad = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
        };
        
        glBufferData(GL_ARRAY_BUFFER, quad, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glBindVertexArray(0);
    }
    
    public void addLabel(String text, float worldX, float worldY) {
        addLabel(text, worldX, worldY, 0.0f, 0.0f, 0.0f, 0.8f);
    }
    
    public void addLabel(String text, float worldX, float worldY, float r, float g, float b, float a) {
        labels.add(new Label(text, worldX, worldY, r, g, b, a));
    }
    
    public void clearLabels() {
        labels.clear();
    }
    
    public void render(Camera2D camera, Window window) {
        if (labels.isEmpty()) return;
        
        // Sauvegarder l'état du depth test
        boolean depthTestEnabled = glIsEnabled(GL_DEPTH_TEST);
        
        // Désactiver le depth test pour que les labels soient toujours visibles
        glDisable(GL_DEPTH_TEST);
        
        // Activer le blending pour la transparence
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        labelShader.bind();
        labelShader.setUniform("projection", camera.getProjectionMatrix());
        labelShader.setUniform("view", camera.getViewMatrix());
        labelShader.setUniform("windowSize", (float)window.getWidth(), (float)window.getHeight());
        
        glBindVertexArray(vao);
        
        for (Label label : labels) {
            // Position dans le monde
            labelShader.setUniform("worldPos", label.worldX, label.worldY);
            
            // Taille en pixels (8 pixels par caractère, 16 pixels de hauteur)
            float width = label.text.length() * 8.0f;
            float height = 16.0f;
            labelShader.setUniform("pixelSize", width, height);
            
            // Couleur avec transparence
            labelShader.setUniform("color", label.r, label.g, label.b, label.a);
            
            // Dessiner le rectangle
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }
        
        glBindVertexArray(0);
        labelShader.unbind();
        
        // Restaurer l'état du depth test
        if (depthTestEnabled) {
            glEnable(GL_DEPTH_TEST);
        }
    }
    
    public void cleanup() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        labelShader.cleanup();
    }
}