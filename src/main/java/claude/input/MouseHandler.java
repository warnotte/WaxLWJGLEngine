package claude.input;

import static org.lwjgl.glfw.GLFW.*;
import org.joml.Vector2f;

public class MouseHandler {
    private static final int BUTTON_COUNT = GLFW_MOUSE_BUTTON_LAST;
    
    private double x, y;
    private double previousX, previousY;
    private double scrollX, scrollY;
    
    private boolean[] buttons;
    private boolean[] buttonsPressed;
    private boolean[] buttonsReleased;
    private boolean isDragging;
    private boolean firstMouse = true;
    
    // Variables pour le debug
    private int frameCount = 0;
    
    public MouseHandler() {
        buttons = new boolean[BUTTON_COUNT];
        buttonsPressed = new boolean[BUTTON_COUNT];
        buttonsReleased = new boolean[BUTTON_COUNT];
    }
    
    public void handleMouseMove(double xpos, double ypos) {
        if (firstMouse) {
            previousX = xpos;
            previousY = ypos;
            firstMouse = false;
       //     System.out.println("[MOUSE] First mouse position: " + xpos + ", " + ypos);
        }
        
        x = xpos;
        y = ypos;
        
        if (isDragging) {
            double dx = x - previousX;
            double dy = y - previousY;
            if (dx != 0 || dy != 0) {
       //         System.out.println("[MOUSE] Dragging - Delta: " + dx + ", " + dy);
            }
        }
    }
    
    public void handleMouseButton(int button, int action) {
        if (button < 0 || button >= BUTTON_COUNT) return;
        
        if (action == GLFW_PRESS) {
            buttons[button] = true;
            buttonsPressed[button] = true;
            
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                isDragging = true;
                previousX = x;
                previousY = y;
              // System.out.println("[MOUSE] Left button pressed at: " + x + ", " + y);
            }
        } else if (action == GLFW_RELEASE) {
            buttons[button] = false;
            buttonsReleased[button] = true;
            
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                isDragging = false;
             //   System.out.println("[MOUSE] Left button released");
            }
        }
    }
    
    public void handleScroll(double xoffset, double yoffset) {
        scrollX = xoffset;
        scrollY = yoffset;
        if (yoffset != 0) {
         //   System.out.println("[MOUSE] Scroll: " + yoffset);
        }
    }
    
    public void update() {
        frameCount++;
        
        // Debug toutes les 60 frames si on drag
        if (frameCount % 60 == 0 && isDragging) {
        //    System.out.println("[MOUSE] Frame " + frameCount + " - Still dragging, pos: " + x + ", " + y);
        }
        
        // Réinitialiser les états pressed/released
        for (int i = 0; i < BUTTON_COUNT; i++) {
            buttonsPressed[i] = false;
            buttonsReleased[i] = false;
        }
        
        // Mettre à jour les positions précédentes
        previousX = x;
        previousY = y;
        
        // Le scroll ne doit PAS être réinitialisé ici!
        // On le fait après l'avoir lu dans Scene
    }
    
    // Nouvelle méthode pour réinitialiser le scroll après usage
    public void clearScroll() {
        scrollX = 0;
        scrollY = 0;
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    
    public double getDeltaX() { 
        return x - previousX;
    }
    
    public double getDeltaY() { 
        return y - previousY;
    }
    
    public double getScrollX() { return scrollX; }
    public double getScrollY() { return scrollY; }
    
    public boolean isDragging() { return isDragging; }
    
    public boolean isLeftButtonDown() { return isButtonDown(GLFW_MOUSE_BUTTON_LEFT); }
    
    public boolean isButtonDown(int button) {
        return button >= 0 && button < BUTTON_COUNT && buttons[button];
    }
}