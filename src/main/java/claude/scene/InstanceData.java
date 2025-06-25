package claude.scene;

public class InstanceData {
    public float x, y, z;
    public float rotation;
    public float scaleX, scaleY, scaleZ;
    public float r, g, b;
    
    public InstanceData(float x, float y, float z, float rotation,
                       float scaleX, float scaleY, float scaleZ,
                       float r, float g, float b) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = rotation;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.r = r;
        this.g = g;
        this.b = b;
    }
}