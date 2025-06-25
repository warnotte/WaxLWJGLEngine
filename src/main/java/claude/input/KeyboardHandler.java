package claude.input;

import static org.lwjgl.glfw.GLFW.*;
import java.util.Arrays;

public class KeyboardHandler {
    private static final int KEY_COUNT = GLFW_KEY_LAST;
    private boolean[] keys;
    private boolean[] keysPressed;
    private boolean[] keysReleased;
    
    public KeyboardHandler() {
        keys = new boolean[KEY_COUNT];
        keysPressed = new boolean[KEY_COUNT];
        keysReleased = new boolean[KEY_COUNT];
    }
    
    public void handleKey(int key, int action) {
        if (key < 0 || key >= KEY_COUNT) return;
        
        if (action == GLFW_PRESS) {
            keys[key] = true;
            keysPressed[key] = true;
        } else if (action == GLFW_RELEASE) {
            keys[key] = false;
            keysReleased[key] = true;
        }
    }
    
    public void update() {
        // Réinitialiser les états pressed/released après chaque frame
        Arrays.fill(keysPressed, false);
        Arrays.fill(keysReleased, false);
    }
    
    // Méthodes d'accès
    public boolean isKeyDown(int key) {
        return key >= 0 && key < KEY_COUNT && keys[key];
    }
    
    public boolean isKeyPressed(int key) {
        return key >= 0 && key < KEY_COUNT && keysPressed[key];
    }
    
    public boolean isKeyReleased(int key) {
        return key >= 0 && key < KEY_COUNT && keysReleased[key];
    }
    
    // Raccourcis pour les touches communes
    public boolean isKeyDown(Key key) {
        return isKeyDown(key.getCode());
    }
    
    public boolean isKeyPressed(Key key) {
        return isKeyPressed(key.getCode());
    }
    
    public boolean isKeyReleased(Key key) {
        return isKeyReleased(key.getCode());
    }
    
    // Enum pour les touches communes
    public enum Key {
        W(GLFW_KEY_W),
        A(GLFW_KEY_A),
        S(GLFW_KEY_S),
        D(GLFW_KEY_D),
        UP(GLFW_KEY_UP),
        LEFT(GLFW_KEY_LEFT),
        DOWN(GLFW_KEY_DOWN),
        RIGHT(GLFW_KEY_RIGHT),
        SPACE(GLFW_KEY_SPACE),
        ESCAPE(GLFW_KEY_ESCAPE),
        ENTER(GLFW_KEY_ENTER),
        SHIFT(GLFW_KEY_LEFT_SHIFT),
        CTRL(GLFW_KEY_LEFT_CONTROL),
        R(GLFW_KEY_R),
        HOME(GLFW_KEY_HOME),
        PLUS(GLFW_KEY_EQUAL),
        MINUS(GLFW_KEY_MINUS),
        KP_PLUS(GLFW_KEY_KP_ADD),
        KP_MINUS(GLFW_KEY_KP_SUBTRACT);
        
        private final int code;
        
        Key(int code) {
            this.code = code;
        }
        
        public int getCode() {
            return code;
        }
    }
}