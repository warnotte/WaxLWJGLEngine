package claude.scene;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dnt.fswf.manager.Manager;
import org.dnt.fswf.model.Flotteur;
import org.dnt.fswf.model.Ilot;
import org.dnt.fswf.model.PanneauSolaire;
import org.joml.Random;
import org.joml.Vector2f;

import claude.core.Window;
import claude.graphics.Camera2D;
import claude.graphics.InstancedMesh;
import claude.graphics.Renderer;
import claude.graphics.TextRendererImproved;
import claude.input.InputManager;
import claude.input.KeyboardHandler;
import claude.input.MouseHandler;

public class Scene {
    private Camera2D camera;
    private InstancedMesh mesh;
    private Manager manager;
    private int ilotId = 2;
    private InputManager inputManager;
    private Window window;
    //private SimpleTextRenderer textRenderer;  // Ajouter cette ligne
    //private TextRendererOptimized textRenderer;  // Ajouter cette ligne
    private TextRendererImproved textRenderer;  // Ajouter cette ligne
    
    
    
    public Scene(InputManager inputManager, Window window) {
        this.inputManager = inputManager;
        this.window = window;
        this.camera = new Camera2D();
        this.manager = new Manager();
        this.textRenderer = new TextRendererImproved();  // Créer l'instance
    }
    
    public void load() {
        try {
            manager.loadProject(new File("Test_ISL.xml"));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        createInstancedObjects();
        
        // Position initiale de la caméra
        camera.getPosition().set(0.0f, 0.0f);
        camera.setZoom(1.0f);
    }
    
    private void createInstancedObjects() {
        // Géométrie de base pour un rectangle
        float[] vertices = {
            -0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
             0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
             0.5f,  0.5f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
            -0.5f,  0.5f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f
        };
        
        int[] indices = { 0, 1, 2, 2, 3, 0 };
        
        // Créer les données d'instance
        List<InstanceData> instances = createInstanceData();
        float[] instanceData = packInstanceData(instances);
        
        mesh = new InstancedMesh(vertices, indices, instanceData, instances.size());
    }
    
    private List<InstanceData> createInstanceData() {
    	
    	   // Texte simple blanc
        textRenderer.addText("Centre (0,0)", 0, 0);
        
        // Texte coloré
        textRenderer.addText("Rouge", 50, 50, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f);
        textRenderer.addText("Vert", -50, 50, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f);
        textRenderer.addText("Bleu", 0, -50, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        
        // Texte avec transparence
        textRenderer.addText("Semi-transparent", 100, 0, 0.5f, 0.0f, 1.0f, 0.5f, 1.0f);
        
        // Texte plus grand
        textRenderer.addText("GRAND", -100, 100, 1.0f, 0.0f, 1.0f, 1.0f, 2.0f);
        
        // Texte plus normal (scale = 1)
        textRenderer.addText("Petit", -100, 75, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        
        // Texte plus petit
        textRenderer.addText("Petit", -100, 50, 1.0f, 0.0f, 1.0f, 1.0f, 0.5f);
       
        // Texte encore plus petit
        textRenderer.addText("Petit", -100, 25, 1.0f, 0.0f, 1.0f, 1.0f, 0.25f);
        
     // Texte avec fond
        textRenderer.addTextWithBackground("Important", -25, -25, 
            1.0f, 1.0f, 1.0f, 1.0f,  // Texte blanc
            0.0f, 0.0f, 0.0f, 0.7f); // Fond noir semi-transparent
        
        /*
        Random rnd = new Random();
        for (int i = 0 ; i < 10000; i ++)
        {
        	float x = rnd.nextFloat()*5000-2500;
        	float y = rnd.nextFloat()*5000-2500;
        	textRenderer.addText("DEMO_"+(int)(rnd.nextFloat()*655560), x,y, 0.0f, 0.0f, 0.0f, 1.0f, 1f);
        }
        */
        
        List<InstanceData> instances = new ArrayList<>();
        Ilot ilot = manager.getModel().getIlotById(ilotId);
        Random random = new Random();
        for (int y = 0; y < ilot.getNbrItemY(); y++) {
            for (int x = 0; x < ilot.getNbrItemX(); x++) {
                float posX = (float) manager.getOffsetX(ilot, x);
                float posY = (float) manager.getOffsetY(ilot, y);
                
                
                Integer id_panneau = ilot.getValuePanneaux(x, y);
				// posX = 1.0f* (float) manager.getOffsetX(ilot, x);
				// posY = 1.0f* (float) manager.getOffsetY(ilot, y);

				
				Integer id_flot = ilot.getValueFlotteurs(x, y);
				if (id_flot != null) {
					Flotteur flot = manager.getModel().getFlotteurById(id_flot);

					float L = (float) flot.getLongueur();
					float l = (float) flot.getLargeur();

					posX += L / 2;
					posY += l / 2;
					
					float r = 0.7f;
					float g = 0.7f;
					float b = 0.7f;
					InstanceData oir = new InstanceData(posX, posY, 0, 0f, L, l, 1, r, g, b);
					instances.add(oir);
				}
				
				
				if (id_panneau != null) {
					PanneauSolaire ps = (PanneauSolaire) manager.getModel().getObjetSurFlotteurById(id_panneau);

					float L = (float) ps.getLongueur() - random.nextFloat() * 0.5f;
					float l = (float) ps.getLargeur() - random.nextFloat() * 0.5f;

					float r = 0.7f + random.nextFloat() * 0.3f;
					float g = 0.7f + random.nextFloat() * 0.3f;
					float b = 0.5f + random.nextFloat() * 0.5f;
					
					InstanceData oir = new InstanceData(posX, posY, 1, 0f + (random.nextFloat() * 0.25f) - 0.125f, L, l, 1, r, g, b);
					instances.add(oir);

				}
                
                /*
                // Traiter les flotteurs
                Integer flotId = ilot.getValueFlotteurs(x, y);
                if (flotId != null) {
                    Flotteur flot = manager.getModel().getFlotteurById(flotId);
                    instances.add(new InstanceData(
                        posX + flot.getLongueur() / 2,
                        posY + flot.getLargeur() / 2,
                        0, 0, 
                        flot.getLongueur(), flot.getLargeur(), 1,
                        0.7f, 0.7f, 0.7f
                    ));
                }
                
                // Traiter les panneaux
                Integer panneauId = ilot.getValuePanneaux(x, y);
                if (panneauId != null) {
                    PanneauSolaire ps = (PanneauSolaire) manager.getModel().getObjetSurFlotteurById(panneauId);
                    instances.add(new InstanceData(
                        posX, posY, 1, 0,
                        ps.getLongueur(), ps.getLargeur(), 1,
                        0.8f, 0.8f, 0.6f
                    ));
                }*/
            }
        }

        
        return instances;
    }
    
    private float[] packInstanceData(List<InstanceData> instances) {
        float[] data = new float[instances.size() * 10];
        int idx = 0;
        
        for (InstanceData inst : instances) {
            data[idx++] = inst.x;
            data[idx++] = inst.y;
            data[idx++] = inst.z;
            data[idx++] = inst.rotation;
            data[idx++] = inst.scaleX;
            data[idx++] = inst.scaleY;
            data[idx++] = inst.scaleZ;
            data[idx++] = inst.r;
            data[idx++] = inst.g;
            data[idx++] = inst.b;
        }
        
        return data;
    }
    
    public void update(float deltaTime) {
        handleCameraInput(deltaTime);
        
        // Mise à jour des matrices de la caméra
        camera.updateProjectionMatrix(window.getWidth(), window.getHeight());
        camera.updateViewMatrix();
        
        debugMousePosition();
    }
    
    private void handleCameraInput(float deltaTime) {
        KeyboardHandler keyboard = inputManager.getKeyboard();
        MouseHandler mouse = inputManager.getMouse();
        
        float moveSpeed = 10.0f / camera.getZoom() * deltaTime * 60.0f;
        
        // Déplacement clavier (pas de changement ici)
        if (keyboard.isKeyDown(KeyboardHandler.Key.W) || keyboard.isKeyDown(KeyboardHandler.Key.UP)) {
            camera.move(0, moveSpeed);
        }
        if (keyboard.isKeyDown(KeyboardHandler.Key.S) || keyboard.isKeyDown(KeyboardHandler.Key.DOWN)) {
            camera.move(0, -moveSpeed);
        }
        if (keyboard.isKeyDown(KeyboardHandler.Key.A) || keyboard.isKeyDown(KeyboardHandler.Key.LEFT)) {
            camera.move(-moveSpeed, 0);
        }
        if (keyboard.isKeyDown(KeyboardHandler.Key.D) || keyboard.isKeyDown(KeyboardHandler.Key.RIGHT)) {
            camera.move(moveSpeed, 0);
        }
        
        // Zoom clavier
        if (keyboard.isKeyDown(KeyboardHandler.Key.PLUS) || keyboard.isKeyDown(KeyboardHandler.Key.KP_PLUS)) {
            camera.setZoom(camera.getZoom() * 1.02f);
        }
        if (keyboard.isKeyDown(KeyboardHandler.Key.MINUS) || keyboard.isKeyDown(KeyboardHandler.Key.KP_MINUS)) {
            camera.setZoom(camera.getZoom() / 1.02f);
        }
        
        // Reset camera
        if (keyboard.isKeyPressed(KeyboardHandler.Key.R)) {
            camera.getPosition().set(0.0f, 0.0f);
            camera.setZoom(1.0f);
            System.out.println("[SCENE] Camera reset");
        }
        /*
        // Drag souris
        if (mouse.isDragging()) {
            double deltaX = mouse.getDeltaX();
            double deltaY = mouse.getDeltaY();
            
            if (deltaX != 0 || deltaY != 0) {
                System.out.println("[SCENE] Mouse drag - Raw delta: " + deltaX + ", " + deltaY);
                float dx = (float)(deltaX * 0.1f / camera.getZoom());
                float dy = (float)(deltaY * 0.1f / camera.getZoom());
                camera.move(-dx, dy);
                System.out.println("[SCENE] Camera moved by: " + (-dx) + ", " + dy);
            }
        }*/
        
        
        // Drag souris
        if (mouse.isDragging()) {
            double deltaX = mouse.getDeltaX();
            double deltaY = mouse.getDeltaY();
            
            if (deltaX != 0 || deltaY != 0) {
                // Convertir le déplacement en pixels en déplacement dans le monde
                float aspect = (float)window.getWidth() / (float)window.getHeight();
                float scale = 100.0f / camera.getZoom();
                
                // Conversion du déplacement pixel vers unités monde
                float worldDeltaX = (float)deltaX / window.getWidth() * 2.0f * aspect * scale;
                float worldDeltaY = (float)deltaY / window.getHeight() * 2.0f * scale;
                
                // Déplacer la caméra (inversé pour que le monde suive la souris)
                camera.move(-worldDeltaX, worldDeltaY);
            }
        }
        
        // Zoom molette
        double scrollY = mouse.getScrollY();
        if (scrollY != 0) {
            float zoomFactor = 1.1f;
            float oldZoom = camera.getZoom();
            
            // Calculer le nouveau niveau de zoom
            if (scrollY > 0) {
                camera.setZoom(oldZoom * zoomFactor);
            } else {
                camera.setZoom(oldZoom / zoomFactor);
            }
            
            // Si le zoom a changé, ajuster la position de la caméra
            if (camera.getZoom() != oldZoom) {
                // Convertir la position de la souris en coordonnées monde (avant zoom)
                float normalizedX = ((float)mouse.getX() / window.getWidth()) * 2.0f - 1.0f;
                float normalizedY = 1.0f - ((float)mouse.getY() / window.getHeight()) * 2.0f;
                float aspect = (float)window.getWidth() / (float)window.getHeight();
                
                float oldScale = 100.0f / oldZoom;
                float worldX = normalizedX * aspect * oldScale + camera.getPosition().x;
                float worldY = normalizedY * oldScale + camera.getPosition().y;
                
                // Convertir la même position souris en coordonnées monde (après zoom)
                float newScale = 100.0f / camera.getZoom();
                float newWorldX = normalizedX * aspect * newScale + camera.getPosition().x;
                float newWorldY = normalizedY * newScale + camera.getPosition().y;
                
                // Ajuster la caméra pour que le point sous la souris reste fixe
                camera.getPosition().x += worldX - newWorldX;
                camera.getPosition().y += worldY - newWorldY;
            }
         // Réinitialiser le scroll après l'avoir utilisé
            mouse.clearScroll();
        }
    }
    
    public void render(Renderer renderer) {
        renderer.render(mesh, camera);
        textRenderer.render(camera, window);  // Ajouter cette ligne
        
    }
    
    public void cleanup() {
        if (mesh != null) {
            mesh.cleanup();
        }
        if (textRenderer != null) {
            textRenderer.cleanup();  // Ajouter cette ligne
        }
        
    }
    
 // Dans Scene.java, ajoutez cette méthode :
    private void debugMousePosition() {
        MouseHandler mouse = inputManager.getMouse();
        
        // Position souris en coordonnées monde
        Vector2f worldPos =camera.screenToWorld(
            (float)mouse.getX(), 
            (float)mouse.getY(), 
            window.getWidth(), window.getHeight()
           // camera.getZoom()
        );
        
        // Afficher dans le titre de la fenêtre ou en console
       // String info = String.format("Mouse: Screen(%.0f, %.0f) World(%.1f, %.1f) Zoom: %.2f", 
        //    mouse.getX(), mouse.getY(), worldPos.x, worldPos.y, camera.getZoom());
        //System.err.println(info);
        
        // Option 1 : Dans le titre de la fenêtre
        //window.setTitle("LWJGL - " + info);
        
        // Option 2 : Afficher comme texte dans la scène
        //textRenderer.clearTexts();
        //textRenderer.addText(info, worldPos.x + 10, worldPos.y + 10, 
        //                    1.0f, 1.0f, 0.0f, 1.0f, 0.8f);
        
        // Ajouter vos autres textes ici...
        //addTestLabels();
    }

    
}