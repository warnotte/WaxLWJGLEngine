package claude.graphics;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import org.joml.Matrix4f;

import claude.utils.ResourceLoader;

public class Renderer {
    private ShaderProgram shaderProgram;
    private Matrix4f modelMatrix;
    
    public Renderer() {
        this.modelMatrix = new Matrix4f();
    }
    
    public void init() {
        // Charger les shaders
    	//String vertexShader = ResourceLoader.loadShader("vertex.vert");
        //String fragmentShader = ResourceLoader.loadShader("fragment.frag");
        String vertexShader = ResourceLoader.loadShader("Datas/shaders/vertex.vert");
        String fragmentShader = ResourceLoader.loadShader("Datas/shaders/fragment.frag");
        
        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        
        // Créer les uniforms
        shaderProgram.createUniform("projection");
        shaderProgram.createUniform("view");
        shaderProgram.createUniform("modelmatrice");
        //shaderProgram.createUniform("time");
        shaderProgram.createUniform("uBorderPx");
        
        // Configuration OpenGL
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
    
    public void render(InstancedMesh mesh, Camera2D camera) {
        shaderProgram.bind();
        
        // Mettre à jour les matrices
        shaderProgram.setUniform("projection", camera.getProjectionMatrix());
        shaderProgram.setUniform("view", camera.getViewMatrix());
        
        float time = (float)glfwGetTime();
        //modelMatrix.identity().rotate(time / 15, 0, 0, 1);
        shaderProgram.setUniform("modelmatrice", modelMatrix);
        
        // Autres uniforms
        //shaderProgram.setUniform("time", time);
        shaderProgram.setUniform("uBorderPx", 1.0f);
        
        // Rendu du mesh
        mesh.render();
        
        shaderProgram.unbind();
    }
    
    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
}