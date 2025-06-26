package claude.graphics;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glIsEnabled;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL20.GL_NO_ERROR;
import static org.lwjgl.opengl.GL20.glGetError;



import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

import claude.core.Window;

public class TextRendererImproved {
    private static class Text {
        String content;
        float worldX, worldY;
        float r, g, b, a;
        float scale;
        boolean hasBackground;
        float bgR, bgG, bgB, bgA;
        
        Text(String content, float x, float y, float r, float g, float b, float a, float scale) {
            this.content = content;
            this.worldX = x;
            this.worldY = y;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.scale = scale;
            this.hasBackground = false;
        }
    }
    
    private static class CharInfo {
        float x, y, width, height;
        float xOffset, yOffset;
        float xAdvance;
    }
    
    private ShaderProgram textShader;
    private ShaderProgram bgShader;
    private int vao, vbo, ebo;
    private int bgVao, bgVbo;
    private int textureId;
    private int textureWidth = 1024;  // Plus grande résolution
    private int textureHeight = 1024;
    
    private Map<Character, CharInfo> charMap = new HashMap<>();
    private List<Text> texts = new ArrayList<>();
    
    private Font font;
    private int fontSize = 32;  // Plus grand pour meilleur rendu
    private float fontHeight;
    
    // Buffers dynamiques
    private FloatBuffer vertexBuffer;
    private IntBuffer indexBuffer;
    private FloatBuffer bgVertexBuffer;
    private int currentBufferSize = 1000;  // Taille initiale
    
    private static final String TEXT_VERTEX_SHADER = """
        #version 330 core
        layout (location = 0) in vec2 aPos;
        layout (location = 1) in vec2 aTexCoord;
        layout (location = 2) in vec4 aColor;
        layout (location = 3) in vec2 aWorldPos;
        layout (location = 4) in float aScale;
        
        out vec2 TexCoord;
        out vec4 Color;
        
        uniform mat4 projection;
        uniform mat4 view;
        uniform vec2 windowSize;
        
        void main() {
            vec4 screenPos = projection * view * vec4(aWorldPos, 0.0, 1.0);
            vec2 scaledPos = aPos * aScale;
            vec2 ndcOffset = scaledPos / windowSize * 2.0;
            
            gl_Position = vec4(screenPos.xy + ndcOffset, 0.0, 1.0);
            TexCoord = aTexCoord;
            Color = aColor;
        }
        """;
    
    private static final String TEXT_FRAGMENT_SHADER = """
        #version 330 core
        in vec2 TexCoord;
        in vec4 Color;
        out vec4 FragColor;
        
        uniform sampler2D fontTexture;
        
        void main() {
            float alpha = texture(fontTexture, TexCoord).r;
            
            // Amélioration du rendu avec seuil
            if (alpha < 0.1) discard;
            
            // Smooth alpha pour les bords
            alpha = smoothstep(0.0, 0.7, alpha);
            
            FragColor = vec4(Color.rgb, Color.a * alpha);
        }
        """;
    
    private static final String BG_VERTEX_SHADER = """
    	    #version 330 core
    	    layout (location = 0) in vec2 aPos;
    	    layout (location = 1) in vec4 aColor;
    	    layout (location = 2) in vec2 aWorldPos;
    	    layout (location = 3) in vec2 aSize;
    	    
    	    out vec4 Color;
    	    
    	    uniform mat4 projection;
    	    uniform mat4 view;
    	    uniform vec2 windowSize;
    	    
    	    void main() {
    	        // Position monde vers écran
    	        vec4 screenPos = projection * view * vec4(aWorldPos, 0.0, 1.0);
    	        
    	        // aPos contient déjà l'offset en pixels, aSize n'est pas utilisé ici
    	        vec2 ndcOffset = aPos / windowSize * 2.0;
    	        
    	        gl_Position = vec4(screenPos.xy + ndcOffset, 0.0, 1.0);
    	        Color = aColor;
    	    }
    	    """;
    
    private static final String BG_FRAGMENT_SHADER = """
        #version 330 core
        in vec4 Color;
        out vec4 FragColor;
        
        void main() {
            FragColor = Color;
        }
        """;
    
    public TextRendererImproved() {
        allocateBuffers(currentBufferSize);
        
        // Shader pour le texte
        textShader = new ShaderProgram(TEXT_VERTEX_SHADER, TEXT_FRAGMENT_SHADER);
        textShader.createUniform("projection");
        textShader.createUniform("view");
        textShader.createUniform("windowSize");
        textShader.createUniform("fontTexture");
        
        // Shader pour les fonds
        bgShader = new ShaderProgram(BG_VERTEX_SHADER, BG_FRAGMENT_SHADER);
        bgShader.createUniform("projection");
        bgShader.createUniform("view");
        bgShader.createUniform("windowSize");
        
        createBuffers();
        generateFontAtlas();
    }
    
    private void allocateBuffers(int charCapacity) {
        // 11 floats par vertex (2 pos + 2 uv + 4 color + 2 worldPos + 1 scale)
        vertexBuffer = BufferUtils.createFloatBuffer(charCapacity * 4 * 11);
        indexBuffer = BufferUtils.createIntBuffer(charCapacity * 6);
        // Pour les fonds : 9 floats par vertex (2 pos + 4 color + 2 worldPos + 2 size)
        bgVertexBuffer = BufferUtils.createFloatBuffer(texts.size() * 4 * 10);
        currentBufferSize = charCapacity;
    }
    
    private void resizeBuffersIfNeeded(int requiredChars) {
        if (requiredChars > currentBufferSize) {
            int newSize = Math.max(requiredChars, currentBufferSize * 2);
            allocateBuffers(newSize);
        }
    }
    
    private void createBuffers() {
        // VAO pour le texte
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        
        int stride = 11 * Float.BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, stride, 4 * Float.BYTES);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(3, 2, GL_FLOAT, false, stride, 8 * Float.BYTES);
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(4, 1, GL_FLOAT, false, stride, 10 * Float.BYTES);
        glEnableVertexAttribArray(4);
        
        // VAO pour les fonds
        bgVao = glGenVertexArrays();
        bgVbo = glGenBuffers();
        
        glBindVertexArray(bgVao);
        glBindBuffer(GL_ARRAY_BUFFER, bgVbo);
        
        stride = 10 * Float.BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, stride, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(3, 2, GL_FLOAT, false, stride, 8 * Float.BYTES);
        glEnableVertexAttribArray(3);
        
        glBindVertexArray(0);
    }
    
    private void generateFontAtlas() {
        // Police avec meilleur rendu
    	font = new Font("Consolas", Font.PLAIN, fontSize);  // Police monospace
    	//font = new Font("Arial", Font.PLAIN, fontSize);  // Police monospace
        
        BufferedImage atlas = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = atlas.createGraphics();
        
        // Configuration pour le meilleur rendu possible
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        g2d.setBackground(new Color(0, 0, 0, 0));
        
        FontMetrics metrics = g2d.getFontMetrics();
        fontHeight = metrics.getHeight();
        
        int padding = 2;
        int x = padding;
        int y = padding;
        int rowHeight = 0;
        
        for (char c = 32; c < 127; c++) {
            int charWidth = metrics.charWidth(c);
            int charHeight = metrics.getHeight();
            
            if (x + charWidth + padding >= textureWidth) {
                x = padding;
                y += rowHeight + padding;
                rowHeight = 0;
            }
            
            g2d.drawString(String.valueOf(c), x, y + metrics.getAscent());
            
            CharInfo info = new CharInfo();
            info.x = (float)x / textureWidth;
            info.y = (float)y / textureHeight;
            info.width = (float)charWidth / textureWidth;
            info.height = (float)charHeight / textureHeight;
            info.xOffset = 0;
            info.yOffset = 0;
            info.xAdvance = charWidth;
            
            charMap.put(c, info);
            
            x += charWidth + padding;
            rowHeight = Math.max(rowHeight, charHeight);
        }
        
        g2d.dispose();
        
        try {
			ImageIO.write(atlas, "png", new java.io.File("font.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        createTextureFromImage(atlas);
    }
    
    private void createTextureFromImage(BufferedImage image) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight());
        
        // Pour une image en niveaux de gris
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int gray = image.getRGB(x, y) & 0xFF;
                buffer.put((byte)gray);
            }
        }
        
        buffer.flip();
        
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, image.getWidth(), image.getHeight(), 
                     0, GL_RED, GL_UNSIGNED_BYTE, buffer);
        
        glGenerateMipmap(GL_TEXTURE_2D);
    }
    
    public void addText(String text, float worldX, float worldY) {
        addText(text, worldX, worldY, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    public Text addTextWithBackground(String text, float worldX, float worldY, 
                                      float r, float g, float b, float a,
                                      float bgR, float bgG, float bgB, float bgA) {
        Text t = new Text(text, worldX, worldY, r, g, b, a, 1.0f);
        t.hasBackground = true;
        t.bgR = bgR;
        t.bgG = bgG;
        t.bgB = bgB;
        t.bgA = bgA;
        texts.add(t);
        return t;
    }
    
    public void addText(String text, float worldX, float worldY, float r, float g, float b, float a, float scale) {
        texts.add(new Text(text, worldX, worldY, r, g, b, a, scale));
    }
    
    public void clearTexts() {
        texts.clear();
    }
    
    public void render(Camera2D camera, Window window) {
        if (texts.isEmpty()) return;
        
        // Compter les caractères totaux
        int totalChars = 0;
        for (Text text : texts) {
            totalChars += text.content.length();
        }
        
        resizeBuffersIfNeeded(totalChars);
        
        // Rendu des fonds d'abord
        renderBackgrounds(camera, window);
        
        // Puis le texte
        renderTexts(camera, window, totalChars);
    }
    
    private void renderBackgrounds(Camera2D camera, Window window) {
        int bgCount = 0;
        for (Text text : texts) {
            if (text.hasBackground) bgCount++;
        }
        
        if (bgCount == 0) return;
        
        if (bgVertexBuffer == null || bgVertexBuffer.capacity() < bgCount * 4 * 10) {
            bgVertexBuffer = BufferUtils.createFloatBuffer(bgCount * 4 * 10);
        }
        
        bgVertexBuffer.clear();
        
        for (Text text : texts) {
            if (!text.hasBackground) continue;
            
            float width = getTextWidth(text.content, text.scale);
            float height = fontHeight * text.scale;
            float padding = 4.0f; // Padding en pixels
            
            // Les positions sont en pixels relatifs au point d'ancrage
            // Format: x, y, r, g, b, a, worldX, worldY, unused, unused
            
            // Bottom-left
            bgVertexBuffer.put(-padding).put(-padding)
                         .put(text.bgR).put(text.bgG).put(text.bgB).put(text.bgA)
                         .put(text.worldX).put(text.worldY)
                         .put(0.0f).put(0.0f); // unused
            
            // Bottom-right
            bgVertexBuffer.put(width + padding).put(-padding)
                         .put(text.bgR).put(text.bgG).put(text.bgB).put(text.bgA)
                         .put(text.worldX).put(text.worldY)
                         .put(0.0f).put(0.0f);
            
            // Top-right
            bgVertexBuffer.put(width + padding).put(height + padding)
                         .put(text.bgR).put(text.bgG).put(text.bgB).put(text.bgA)
                         .put(text.worldX).put(text.worldY)
                         .put(0.0f).put(0.0f);
            
            // Top-left
            bgVertexBuffer.put(-padding).put(height + padding)
                         .put(text.bgR).put(text.bgG).put(text.bgB).put(text.bgA)
                         .put(text.worldX).put(text.worldY)
                         .put(0.0f).put(0.0f);
        }
        
        bgVertexBuffer.flip();
        
        // Configuration du rendu
        boolean depthTestEnabled = glIsEnabled(GL_DEPTH_TEST);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        bgShader.bind();
        bgShader.setUniform("projection", camera.getProjectionMatrix());
        bgShader.setUniform("view", camera.getViewMatrix());
        bgShader.setUniform("windowSize", (float)window.getWidth(), (float)window.getHeight());
        
        glBindVertexArray(bgVao);
        
        // Upload des données
        glBindBuffer(GL_ARRAY_BUFFER, bgVbo);
        glBufferData(GL_ARRAY_BUFFER, bgVertexBuffer, GL_DYNAMIC_DRAW);
        
        // Indices pour les quads
        int[] indices = new int[bgCount * 6];
        int idx = 0;
        for (int i = 0; i < bgCount; i++) {
            int base = i * 4;
            indices[idx++] = base;
            indices[idx++] = base + 1;
            indices[idx++] = base + 2;
            indices[idx++] = base + 2;
            indices[idx++] = base + 3;
            indices[idx++] = base;
        }
        
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_DYNAMIC_DRAW);
        
        // Draw call
        glDrawElements(GL_TRIANGLES, bgCount * 6, GL_UNSIGNED_INT, 0);
        
        glBindVertexArray(0);
        bgShader.unbind();
        
        if (depthTestEnabled) {
            glEnable(GL_DEPTH_TEST);
        }
    }
    
    private void renderTexts(Camera2D camera, Window window, int totalChars) {
        vertexBuffer.clear();
        indexBuffer.clear();
        
        int vertexCount = 0;
        int indexCount = 0;
        /*
        System.out.println("=== DEBUG RENDER ===");
        System.out.println("Total texts: " + texts.size());
        System.out.println("Total chars: " + totalChars);
        */
        for (Text text : texts) {
            float x = 0;
           // System.out.println("Rendering text: '" + text.content + "' at " + text.worldX + ", " + text.worldY);
            
            for (char c : text.content.toCharArray()) {
                CharInfo info = charMap.get(c);
                if (info == null) {
                    System.out.println("  Char '" + c + "' not found in charMap!");
                    continue;
                }
                
                // Positions du quad
                float x0 = x;
                float y0 = 0;
                float x1 = x + info.xAdvance;
                float y1 = fontHeight;
                
                // UV
                float u0 = info.x;
                float v0 = info.y;
                float u1 = info.x + info.width;
                float v1 = info.y + info.height;
                
                float x2 = x0; 
                //float y2 = y0; 
                
                x0 = x1;
                // y0 = y1;
                x1 = x2;
                // y1 = y2;
                
                float u2 = u0;
                float v2 = v0;
                
                u0 = u1;
                v0 = v1;
                u1 = u2;
                v1 = v2;
                
                // Ajouter les 4 vertices
                // Bottom-left
                vertexBuffer.put(x0).put(y0).put(u0).put(v0)
                           .put(text.r).put(text.g).put(text.b).put(text.a)
                           .put(text.worldX).put(text.worldY).put(text.scale);
                
                // Bottom-right
                vertexBuffer.put(x1).put(y0).put(u1).put(v0)
                           .put(text.r).put(text.g).put(text.b).put(text.a)
                           .put(text.worldX).put(text.worldY).put(text.scale);
                
                // Top-right
                vertexBuffer.put(x1).put(y1).put(u1).put(v1)
                           .put(text.r).put(text.g).put(text.b).put(text.a)
                           .put(text.worldX).put(text.worldY).put(text.scale);
                
                // Top-left
                vertexBuffer.put(x0).put(y1).put(u0).put(v1)
                           .put(text.r).put(text.g).put(text.b).put(text.a)
                           .put(text.worldX).put(text.worldY).put(text.scale);
                
                // Indices
                int base = vertexCount;
                indexBuffer.put(base).put(base + 1).put(base + 2);
                indexBuffer.put(base + 2).put(base + 3).put(base);
                
                vertexCount += 4;
                indexCount += 6;
                x += info.xAdvance * text.scale;
            }
        }
        
        //System.out.println("Vertex count: " + vertexCount);
        //System.out.println("Index count: " + indexCount);
        
        if (vertexCount == 0) {
            System.out.println("WARNING: No vertices generated!");
            return;
        }
        
        vertexBuffer.flip();
        indexBuffer.flip();
        
        // Configuration du rendu
        boolean depthTestEnabled = glIsEnabled(GL_DEPTH_TEST);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        textShader.bind();
        textShader.setUniform("projection", camera.getProjectionMatrix());
        textShader.setUniform("view", camera.getViewMatrix());
        textShader.setUniform("windowSize", (float)window.getWidth(), (float)window.getHeight());
        textShader.setUniform("fontTexture", 0);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        glBindVertexArray(vao);
        
        // Upload des données
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_DYNAMIC_DRAW);
        /*
        // Check OpenGL errors
        int error = glGetError();
        if (error != GL_NO_ERROR) {
            System.out.println("OpenGL Error before draw: " + error);
        }
        */
        // Draw call
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        /*
        error = glGetError();
        if (error != GL_NO_ERROR) {
            System.out.println("OpenGL Error after draw: " + error);
        }
        */
        glBindVertexArray(0);
        textShader.unbind();
        
        if (depthTestEnabled) {
            glEnable(GL_DEPTH_TEST);
        }
    }
    
    public float getTextWidth(String text, float scale) {
        float width = 0;
        for (char c : text.toCharArray()) {
            CharInfo info = charMap.get(c);
            if (info != null) {
                width += info.xAdvance * scale;
            }
        }
        return width;
    }
    
    public void cleanup() {
        glDeleteTextures(textureId);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteBuffers(bgVbo);
        glDeleteVertexArrays(vao);
        glDeleteVertexArrays(bgVao);
        textShader.cleanup();
        bgShader.cleanup();
    }
}