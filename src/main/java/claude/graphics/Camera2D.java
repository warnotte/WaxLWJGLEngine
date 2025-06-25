package claude.graphics;

import org.joml.Matrix4f;
import org.joml.Vector2f;

public class Camera2D {
    private Vector2f position;
    private float zoom;
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    
    public Camera2D() {
        this.position = new Vector2f(0, 0);
        this.zoom = 1.0f;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
    }
    
    public void updateProjectionMatrix(int width, int height) {
        float aspect = (float)width / (float)height;
        float scale = 100.0f / zoom;
        projectionMatrix.identity().ortho(
            -aspect * scale, aspect * scale,
            -scale, scale,
            -1.0f, 1.0f
        );
    }
    
    public void updateViewMatrix() {
        viewMatrix.identity().translate(-position.x, -position.y, 0.0f);
    }
    
    public void move(float dx, float dy) {
        position.x += dx;
        position.y += dy;
    }
    
    public void setZoom(float zoom) {
        this.zoom = Math.max(0.001f, Math.min(100.0f, zoom));
    }
    
    public Vector2f screenToWorld(float screenX, float screenY, int windowWidth, int windowHeight) {
        float normalizedX = (screenX / windowWidth) * 2.0f - 1.0f;
        float normalizedY = 1.0f - (screenY / windowHeight) * 2.0f;
        
        float aspect = (float)windowWidth / (float)windowHeight;
        float scale = 100.0f / zoom;
        
        float worldX = normalizedX * aspect * scale + position.x;
        float worldY = normalizedY * scale + position.y;
        
        return new Vector2f(worldX, worldY);
    }
    
    // Getters
    public Matrix4f getProjectionMatrix() { return projectionMatrix; }
    public Matrix4f getViewMatrix() { return viewMatrix; }
    public Vector2f getPosition() { return position; }
    public float getZoom() { return zoom; }
}