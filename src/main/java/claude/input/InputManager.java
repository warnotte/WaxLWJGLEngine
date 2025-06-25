package claude.input;

import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import claude.core.Window;

public class InputManager {
    private KeyboardHandler keyboard;
    private MouseHandler mouse;
    
    public InputManager() {
        keyboard = new KeyboardHandler();
        mouse = new MouseHandler();
    }
    
    public void init(Window window) {
        long handle = window.getHandle();
        
        // Keyboard callbacks
        glfwSetKeyCallback(handle, (w, key, scancode, action, mods) -> {
            keyboard.handleKey(key, action);
        });
        
        // Mouse callbacks
        glfwSetCursorPosCallback(handle, (w, xpos, ypos) -> {
            mouse.handleMouseMove(xpos, ypos);
        });
        
        glfwSetMouseButtonCallback(handle, (w, button, action, mods) -> {
            mouse.handleMouseButton(button, action);
        });
        
        glfwSetScrollCallback(handle, (w, xoffset, yoffset) -> {
            mouse.handleScroll(xoffset, yoffset);
        });
    }
    
    public void update() {
        keyboard.update();
        mouse.update();
    }
    
    public void cleanup() {
        // Cleanup si nécessaire
    }
    
    public KeyboardHandler getKeyboard() { return keyboard; }
    public MouseHandler getMouse() { return mouse; }
}