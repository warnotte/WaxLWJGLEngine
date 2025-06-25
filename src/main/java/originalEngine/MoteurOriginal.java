package originalEngine;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_EQUAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LESS;

import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glClearDepth;

import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dnt.fswf.manager.Manager;
import org.dnt.fswf.model.Flotteur;
import org.dnt.fswf.model.Ilot;
import org.dnt.fswf.model.PanneauSolaire;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Random;
import org.joml.Vector2f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

public class MoteurOriginal {
    
	// Window
    private long window;
    private int windowWidth = 800;
    private int windowHeight = 600;
    
    // Shader program
    private int shaderProgram;
    
    // Camera/View
    private Vector2f cameraPosition = new Vector2f(0.0f, 0.0f);
    private float zoomLevel = 1.0f;
    private Matrix4f projectionMatrix = new Matrix4f();
    private Matrix4f viewMatrix = new Matrix4f();
    
    // Instanced rendering
    private int rectangleVAO, rectangleVBO, rectangleInstanceVBO;
    private int gridSize = 1000;
    private int totalInstances = gridSize * gridSize;
    
    // Input
    private boolean[] keys = new boolean[GLFW_KEY_LAST];
    private double mouseX, mouseY;
    private boolean mouseDragging = false;
    private double lastMouseX, lastMouseY;
    
    // Vertex shader avec instanced rendering et animations
    private static String VERTEX_SHADER_SOURCE;
    
    // Fragment shader
    private static String FRAGMENT_SHADER_SOURCE;
    
    //"        FragColor = vec4(fragColor, 1.0);\n" +
    
    Manager manager = new Manager();
    int id_ilot = 2;
    
    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        
        try {
			manager.loadProject(new File("Test_ISL.xml"));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
        
        init();
        initOpenGL();
        createInstancedGeometry();
        loop();
        cleanup();
    }
    
    private void init() {
GLFWErrorCallback.createPrint(System.err).set();
        
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        
        // Configuration OpenGL moderne
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 8 );  // defined samples for  GLFW Window

        
        
        window = glfwCreateWindow(windowWidth, windowHeight, "Moteur 2D OpenGL - Instanced Rendering", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        
        setupCallbacks();
        centerWindow();
        
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }
    
    private void setupCallbacks() {
        // Callback clavier
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                keys[key] = true;
                if (key == GLFW_KEY_ESCAPE)
                    glfwSetWindowShouldClose(window, true);
            } else if (action == GLFW_RELEASE) {
                keys[key] = false;
            }
        });
        
        // Callback souris
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (mouseDragging) {
                double deltaX = xpos - lastMouseX;
                double deltaY = ypos - lastMouseY;
                
                cameraPosition.x -= (float)(deltaX * 0.1f / zoomLevel);
                cameraPosition.y += (float)(deltaY * 0.1f / zoomLevel);
            }
            
            lastMouseX = xpos;
            lastMouseY = ypos;
            mouseX = xpos;
            mouseY = ypos;
        });
        
        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    mouseDragging = true;
                    lastMouseX = mouseX;
                    lastMouseY = mouseY;
                } else if (action == GLFW_RELEASE) {
                    mouseDragging = false;
                }
            }
        });
        
        // Callback scroll pour le zoom
        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            
            float zoomFactor = 1.1f;
            float oldZoom = zoomLevel;
            
            // Calculer le nouveau niveau de zoom
            if (yoffset > 0) {
                zoomLevel *= zoomFactor;
            } else if (yoffset < 0) {
                zoomLevel /= zoomFactor;
            }
            zoomLevel = Math.max(0.001f, Math.min(100.0f, zoomLevel));
            
            // Si le zoom a changé, ajuster la position de la caméra
            if (zoomLevel != oldZoom) {
                // Convertir la position de la souris en coordonnées monde (avant zoom)
                Vector2f mouseWorldPos = screenToWorld((float)mouseX, (float)mouseY, oldZoom);
                
                // Convertir la même position souris en coordonnées monde (après zoom)
                Vector2f newMouseWorldPos = screenToWorld((float)mouseX, (float)mouseY, zoomLevel);
                
                // Ajuster la caméra pour que le point sous la souris reste fixe
                cameraPosition.x += mouseWorldPos.x - newMouseWorldPos.x;
                cameraPosition.y += mouseWorldPos.y - newMouseWorldPos.y;
            }
        });
        
        // Callback redimensionnement
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            windowWidth = width;
            windowHeight = height;
            glViewport(0, 0, width, height);
            updateProjectionMatrix();
        });
    }
    
    private void centerWindow() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            
            glfwSetWindowPos(
                window,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            );
        }
    }
    
    private void initOpenGL() {
        GL.createCapabilities();
        
        // Activer le depth testing
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glClearDepth(1.0); // Valeur de clear du depth buffer
        
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        createShaderProgram();
        updateProjectionMatrix();
    }
    
    private void createShaderProgram() {
        int vertexShader = createShader(GL_VERTEX_SHADER, VERTEX_SHADER_SOURCE);
        int fragmentShader = createShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER_SOURCE);
        
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader program linking failed: " + 
                glGetProgramInfoLog(shaderProgram));
        }
        
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }
    
    private int createShader(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader compilation failed: " + 
                glGetShaderInfoLog(shader));
        }
        
        return shader;
    }
    
    private void createInstancedGeometry() {
        // Géométrie de base pour un rectangle centré
        float[] vertices = {
            // Position     		// Couleur			// UV
            -0.5f, -0.5f, 0.0f,		1.0f, 1.0f, 1.0f,   0.0f, 0.0f,
             0.5f, -0.5f, 0.0f,		1.0f, 1.0f, 1.0f,	0.0f, 1.0f,
             0.5f,  0.5f, 0.0f,		1.0f, 1.0f, 1.0f,	1.0f, 1.0f,
            -0.5f,  0.5f, 0.0f,   	1.0f, 1.0f, 1.0f,	0.0f, 1.0f
        };
        
        int[] indices = {
                0, 1, 2,
                2, 3, 0
            };
        /* Ca c'est si dessine les instance avec GL_LINES
        int[] indices = {
                0, 1, 1, 2, 2, 3, 3, 0
        };*/
        
        
        // Créer VAO et VBO pour la géométrie de base
        rectangleVAO = glGenVertexArrays();
        rectangleVBO = glGenBuffers();
        int EBO = glGenBuffers();
        
        glBindVertexArray(rectangleVAO);
        
        glBindBuffer(GL_ARRAY_BUFFER, rectangleVBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        
        // Attributs de vertex (position et couleur de base)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
        
        // Créer les données d'instance
        createInstanceData();
        
        glBindVertexArray(0);
        
        System.out.println("Created " + totalInstances + " instances");
    }
    
    private class ObjetInstanceRender {
    	float x,y, z, rot, sX, sY, sZ, R,G,B;//, zDepth;

		public ObjetInstanceRender(float x, float y, float z, float rot, float LL, float ll, float sZ, float r, float g, float b/*, float zDepth*/) {
			super();
			this.x = x;
			this.y = y; 
			this.z = z;
			this.rot = rot;
			sX = LL;
			sY = ll;
			this.sZ = sZ;
			R = r;
			G = g;
			B = b;
			//this.zDepth = zDepth; 
		}
    	
    	
    }
    
    private void createInstanceData() {
        
    	Random random = new Random();
    	
        List<ObjetInstanceRender> objectsToRenders = new ArrayList<>();
        Ilot ilot = manager.getModel().getIlotById(id_ilot);
        
		for (int y = 0; y < ilot.getNbrItemY(); y++) {
			for (int x = 0; x < ilot.getNbrItemX(); x++) {
				
				float posX = 1.0f * (float) manager.getOffsetX(ilot, x);
				float posY = 1.0f * (float) manager.getOffsetY(ilot, y);
		
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
					ObjetInstanceRender oir = new ObjetInstanceRender(posX, posY, 0, 0f, L, l, 1, r, g, b);
					objectsToRenders.add(oir);
				}
				
				
				if (id_panneau != null) {
					PanneauSolaire ps = (PanneauSolaire) manager.getModel().getObjetSurFlotteurById(id_panneau);

					float L = (float) ps.getLongueur() - random.nextFloat() * 0.5f;
					float l = (float) ps.getLargeur() - random.nextFloat() * 0.5f;

					float r = 0.7f + random.nextFloat() * 0.3f;
					float g = 0.7f + random.nextFloat() * 0.3f;
					float b = 0.5f + random.nextFloat() * 0.5f;
					
					ObjetInstanceRender oir = new ObjetInstanceRender(posX, posY, 1, 0f + (random.nextFloat() * 0.25f) - 0.125f, L, l, 1, r, g, b);
					objectsToRenders.add(oir);

				}
			}
		}
		
		// TODO : Ca sert plus a rien normalement...
		//Collections.sort(objectsToRenders, (a, b) -> Float.compare(b.zDepth, a.zDepth));
        
        int dudul = 10;
        
		float[] instanceData = new float[objectsToRenders.size() * dudul]; 
		
		int idx = 0;
		for ( int i = 0; i < objectsToRenders.size(); i++)
		{
			ObjetInstanceRender oir = objectsToRenders.get(i);
			instanceData[idx++] = oir.x;
			instanceData[idx++] = oir.y;
			instanceData[idx++] = oir.z;
			instanceData[idx++] = oir.rot;
			instanceData[idx++] = oir.sX;
			instanceData[idx++] = oir.sY;
			instanceData[idx++] = oir.sZ;
			instanceData[idx++] = oir.R;
			instanceData[idx++] = oir.G;
			instanceData[idx++] = oir.B;
		}
		
        
        // Créer le VBO pour les données d'instance
        rectangleInstanceVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, rectangleInstanceVBO);
        glBufferData(GL_ARRAY_BUFFER, instanceData, GL_STATIC_DRAW);
        
        glBindVertexArray(rectangleVAO);
        
        // Position d'instance (attribut 2)
        glVertexAttribPointer(3, 3, GL_FLOAT, false, dudul * Float.BYTES, 0);
        glEnableVertexAttribArray(3);
        glVertexAttribDivisor(3, 1); // Un par instance
        
        // Rotation d'instance (attribut 3)
        glVertexAttribPointer(4, 1, GL_FLOAT, false, dudul * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(4);
        glVertexAttribDivisor(4, 1);
        
        // Scale d'instance (attribut 4)
        glVertexAttribPointer(5, 3, GL_FLOAT, false, dudul * Float.BYTES, 4 * Float.BYTES);
        glEnableVertexAttribArray(5);
        glVertexAttribDivisor(5, 1);
        
        // Couleur d'instance (attribut 5)
        glVertexAttribPointer(6, 3, GL_FLOAT, false, dudul * Float.BYTES, 7 * Float.BYTES);
        glEnableVertexAttribArray(6);
        glVertexAttribDivisor(6, 1);
        
        glBindVertexArray(0);
    }
    
    private void updateProjectionMatrix() {
        float aspect = (float)windowWidth / (float)windowHeight;
        float scale = 100.0f / zoomLevel; // Échelle adaptée pour voir la grille
        projectionMatrix.identity().ortho(-aspect * scale, aspect * scale, -scale, scale, -1.0f, 1.0f);
        //projectionMatrix.scale(1, -1, 1);
        
    }
    
    private void updateViewMatrix() {
        viewMatrix.identity().translate(-cameraPosition.x, -cameraPosition.y, 0.0f);
    }
    
    private void handleInput() {
        float moveSpeed = 10.0f / zoomLevel;
        
        if (keys[GLFW_KEY_W] || keys[GLFW_KEY_UP]) {
            cameraPosition.y += moveSpeed;
        }
        if (keys[GLFW_KEY_S] || keys[GLFW_KEY_DOWN]) {
            cameraPosition.y -= moveSpeed;
        }
        if (keys[GLFW_KEY_A] || keys[GLFW_KEY_LEFT]) {
            cameraPosition.x -= moveSpeed;
        }
        if (keys[GLFW_KEY_D] || keys[GLFW_KEY_RIGHT]) {
            cameraPosition.x += moveSpeed;
        }
        
        // Zoom avec + et -
        if (keys[GLFW_KEY_KP_ADD] || keys[GLFW_KEY_EQUAL]) {
            zoomLevel *= 1.02f;
            zoomLevel = Math.min(100.0f, zoomLevel);
        }
        if (keys[GLFW_KEY_KP_SUBTRACT] || keys[GLFW_KEY_MINUS]) {
            zoomLevel /= 1.02f;
            zoomLevel = Math.max(0.001f, zoomLevel);
        }
        
        // Reset camera
        if (keys[GLFW_KEY_R]) {
            cameraPosition.set(0.0f, 0.0f); // Centrer sur la grille
            zoomLevel = 1.00f; // Zoom arrière pour voir toute la grille
        }
        
        
        // Aller au coin (0,0)
        if (keys[GLFW_KEY_HOME]) {
            cameraPosition.set(0.0f, 0.0f);
            zoomLevel = 1.0f;
        }
        
        // Animation toggle
        if (keys[GLFW_KEY_SPACE]) {
            // Vous pouvez ajouter une variable pour activer/désactiver l'animation
            // ou modifier les paramètres d'animation
        }
    }
    
    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        glUseProgram(shaderProgram);
        
        updateProjectionMatrix();
        updateViewMatrix();
        
        // Calculer le temps écoulé en secondes
        float currentTime = (float)glfwGetTime();
        
        // Envoyer les uniformes aux shaders
        int projLoc = glGetUniformLocation(shaderProgram, "projection");
        int viewLoc = glGetUniformLocation(shaderProgram, "view");
        int modelMatrixLoc = glGetUniformLocation(shaderProgram, "modelmatrice");
        int timeLoc = glGetUniformLocation(shaderProgram, "time");
        int uBorderPxLoc = glGetUniformLocation(shaderProgram, "uBorderPx");
        
        Ilot ilot = manager.getModel().getIlotById(id_ilot);
        float L = (float) manager.getLongueur(ilot);
        float l = (float) manager.getLargeur(ilot);
        try (MemoryStack stack = stackPush()) {
            FloatBuffer projBuffer = stack.mallocFloat(16);
            FloatBuffer viewBuffer = stack.mallocFloat(16);
            FloatBuffer modelBuffer = stack.mallocFloat(16);
            
            projectionMatrix.get(projBuffer);
            viewMatrix.get(viewBuffer);
                
            Matrix4f modelMatrix = new Matrix4f().identity();
            modelMatrix.rotate(currentTime/15, 0, 0, 1);
            modelMatrix.translate(-L/2, -l/2, 0);
            modelMatrix.get(modelBuffer);
            
            glUniformMatrix4fv(projLoc, false, projBuffer);
            glUniformMatrix4fv(viewLoc, false, viewBuffer);
            glUniformMatrix4fv(modelMatrixLoc, false, modelBuffer);
            glUniform1f(timeLoc, currentTime); // Envoyer le temps
            glUniform1f(uBorderPxLoc, 0.5f); // Epaisseur bordure
           
        }
        
        // Rendre toutes les instances d'un coup !
        glBindVertexArray(rectangleVAO);
        glDrawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0, totalInstances);
        //glDrawElementsInstanced(GL_LINES, 8, GL_UNSIGNED_INT, 0, totalInstances); // Fait dans le fragmentShader.
       
        glBindVertexArray(0);
        
        glUseProgram(0);
    }
    
    private void loop() {
        // Commencer centré sur la grille
        cameraPosition.set(000.0f, 000.0f);
        zoomLevel = 1f;
        
        long lastTime = System.nanoTime();
        int frameCount = 0;
        
        while (!glfwWindowShouldClose(window)) {
            handleInput();
            render();
            
            // FPS counter
            frameCount++;
            long currentTime = System.nanoTime();
            if (currentTime - lastTime >= 1_000_000_000L) {
                System.out.println("FPS: " + frameCount);
                frameCount = 0;
                lastTime = currentTime;
            }
            
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
    
    private void cleanup() {
        glDeleteVertexArrays(rectangleVAO);
        glDeleteBuffers(rectangleVBO);
        glDeleteBuffers(rectangleInstanceVBO);
        glDeleteProgram(shaderProgram);
        
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
    
    // Nouvelle méthode à ajouter dans la classe HelloWorld_GOOD :
    private Vector2f screenToWorld(float screenX, float screenY, float zoom) {
        // Convertir les coordonnées écran en coordonnées normalisées [-1, 1]
        float normalizedX = (screenX / windowWidth) * 2.0f - 1.0f;
        float normalizedY = 1.0f - (screenY / windowHeight) * 2.0f; // Y inversé
        
        // Calculer l'aspect ratio et l'échelle
        float aspect = (float)windowWidth / (float)windowHeight;
        float scale = 100.0f / zoom;
        
        // Convertir en coordonnées monde
        float worldX = normalizedX * aspect * scale + cameraPosition.x;
        float worldY = normalizedY * scale + cameraPosition.y;
        
        return new Vector2f(worldX, worldY);
    }
    
    public static void main(String[] args) throws IOException {
    	VERTEX_SHADER_SOURCE= Files.readString(Path.of("vertex.vert"));
    	FRAGMENT_SHADER_SOURCE= Files.readString(Path.of("fragment.frag"));
    	System.err.println(VERTEX_SHADER_SOURCE);
    	System.err.println(FRAGMENT_SHADER_SOURCE);
        new MoteurOriginal().run();
    }
}