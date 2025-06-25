package claude.core;

public class Time {
    private static long lastFrameTime;
    private static float deltaTime;
    
    static {
        lastFrameTime = System.nanoTime();
    }
    
    public static void update() {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = currentTime;
    }
    
    public static float getDeltaTime() {
        return deltaTime;
    }
    
    public static float getTime() {
        return System.nanoTime() / 1_000_000_000.0f;
    }
}