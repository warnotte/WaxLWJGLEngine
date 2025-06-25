package claude.core;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import claude.graphics.Renderer;
import claude.input.InputManager;
import claude.scene.Scene;

public class Application {
    private Window window;
    private Renderer renderer;
    private InputManager inputManager;
    private Scene currentScene;
    private boolean running;
    
    public Application(String title, int width, int height) {
        this.window = new Window(title, width, height);
        this.renderer = new Renderer();
        this.inputManager = new InputManager();
        this.running = true;
    }
    
    public void run() {
        init();
        loop();
        cleanup();
    }
    
    private void init() {
        window.init();
        renderer.init();
        inputManager.init(window);
        
        // Créer et charger la scène
        currentScene = new Scene(inputManager, window);
        currentScene.load();
    }
    
    private void loop() {
        int frameCount = 0;
        
        while (running && !window.shouldClose()) {
            frameCount++;
            Time.update();
            
            // Debug toutes les 300 frames
            if (frameCount % 300 == 0) {
                System.out.println("[APP] Frame " + frameCount + " - FPS: " + (int)(1.0f / Time.getDeltaTime()));
            }
            
            // Gérer la fermeture avec Escape
            if (inputManager.getKeyboard().isKeyPressed(GLFW_KEY_ESCAPE)) {
                running = false;
            }
            
            // IMPORTANT: L'ordre est crucial ici
            // 1. D'abord update la scène (qui lit les inputs)
            currentScene.update(Time.getDeltaTime());
            
            // 2. Ensuite update l'inputManager (qui prépare pour la frame suivante)
            inputManager.update();
            
            // 3. Rendu
            renderer.clear();
            currentScene.render(renderer);
            
            // 4. Swap et poll events
            window.swapBuffers();
            window.pollEvents();
        }
    }
    
    private void cleanup() {
        currentScene.cleanup();
        renderer.cleanup();
        inputManager.cleanup();
        window.cleanup();
    }
}