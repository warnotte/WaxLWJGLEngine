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
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;

import claude.core.Window;

public class TextRendererOptimized {
    private static class Text {
        String content;
        float worldX, worldY;
        float r, g, b, a;
        float scale;
        
        Text(String content, float x, float y, float r, float g, float b, float a, float scale) {
            this.content = content;
            this.worldX = x;
            this.worldY = y;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.scale = scale;
        }
    }
    
    private static class CharInfo {
        float x, y, width, height;
        float xOffset, yOffset;
        float xAdvance;
    }
    
    private ShaderProgram textShader;
    private int vao, vbo, ebo;
    private int textureId;
    private int textureWidth = 512;
    private int textureHeight = 512;
    
    private Map<Character, CharInfo> charMap = new HashMap<>();
    private List<Text> texts = new ArrayList<>();
    
    private Font font;
    private int fontSize = 24;
    private float fontHeight;
    
    // Buffers pour le batching
    //private static final int MAX_CHARACTERS = 10000; // Support jusqu'à 10k caractères
    private static final int MAX_CHARACTERS = 1000000; // Support jusqu'à 100000k caractères
    private FloatBuffer vertexBuffer;
    private IntBuffer indexBuffer;
    
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
            // Position monde vers position écran
            vec4 screenPos = projection * view * vec4(aWorldPos, 0.0, 1.0);
            
            // Appliquer l'échelle et convertir en NDC
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
            vec4 sampled = texture(fontTexture, TexCoord);
            FragColor = vec4(Color.rgb, Color.a * sampled.r);
        }
        """;
    
    
    int maxCharSize = 15;
    
    public TextRendererOptimized() {
        // Créer les buffers CPU
    	vertexBuffer = BufferUtils.createFloatBuffer(MAX_CHARACTERS * 4 * maxCharSize); // 4 vertices * 9 floats
        indexBuffer = BufferUtils.createIntBuffer(MAX_CHARACTERS * 6); // 6 indices par quad
        
        // Créer le shader
        textShader = new ShaderProgram(TEXT_VERTEX_SHADER, TEXT_FRAGMENT_SHADER);
        textShader.createUniform("projection");
        textShader.createUniform("view");
        textShader.createUniform("windowSize");
        textShader.createUniform("fontTexture");
        
        createBuffers();
        generateFontAtlas();
    }
    
    private void createBuffers() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();
        
        glBindVertexArray(vao);
        
        // VBO avec taille maximale
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, MAX_CHARACTERS * 4 * maxCharSize * Float.BYTES, GL_DYNAMIC_DRAW);
        
        // EBO avec taille maximale
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, MAX_CHARACTERS * 6 * Integer.BYTES, GL_DYNAMIC_DRAW);
        
        // Layout des attributs :
        // 0: position (2 floats)
        // 1: texCoord (2 floats) 
        // 2: color (4 floats)
        // 3: worldPos (2 floats)
        // 4: scale (1 float)
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
        
        glBindVertexArray(0);
    }
    
    private void generateFontAtlas() {
        font = new Font("Arial", Font.PLAIN, fontSize);
        
        BufferedImage atlas = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = atlas.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        
        FontMetrics metrics = g2d.getFontMetrics();
        fontHeight = metrics.getHeight();
        
        int x = 0;
        int y = 0;
        int rowHeight = 0;
        
        for (char c = 32; c < 127; c++) {
            int charWidth = metrics.charWidth(c);
            int charHeight = metrics.getHeight();
            
            if (x + charWidth >= textureWidth) {
                x = 0;
                y += rowHeight + 2;
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
            
            x += charWidth + 2;
            rowHeight = Math.max(rowHeight, charHeight);
        }
        
        g2d.dispose();
        createTextureFromImage(atlas);
    }
    
    private void createTextureFromImage(BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        
        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        
        buffer.flip();
        
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 
                     0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    }
    
    public void addText(String text, float worldX, float worldY) {
        addText(text, worldX, worldY, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    public void addText(String text, float worldX, float worldY, float r, float g, float b) {
        addText(text, worldX, worldY, r, g, b, 1.0f, 1.0f);
    }
    
    public void addText(String text, float worldX, float worldY, float r, float g, float b, float a, float scale) {
        texts.add(new Text(text, worldX, worldY, r, g, b, a, scale));
    }
    
    public void clearTexts() {
        texts.clear();
    }
    
    public void render(Camera2D camera, Window window) {
        if (texts.isEmpty()) return;
        
        // Préparer le batch
        vertexBuffer.clear();
        indexBuffer.clear();
        
        int vertexCount = 0;
        int indexCount = 0;
        int charCount = 0;
        
        // Construire le batch pour tous les textes
        for (Text text : texts) {
            float x = 0;
            
            for (char c : text.content.toCharArray()) {
                if (charCount >= MAX_CHARACTERS) break;
                
                CharInfo info = charMap.get(c);
                if (info == null) continue;
                
                // Positions locales du quad
                float x0 = x + info.xOffset;
                float y0 = info.yOffset;
                float x1 = x0 + info.xAdvance;
                float y1 = y0 + fontHeight;
                
                // UV
                float u0 = info.x;
                float v0 = info.y;
                float u1 = info.x + info.width;
                float v1 = info.y + info.height;
                
                // Ajouter les 4 vertices
                // Format: x, y, u, v, r, g, b, a, worldX, worldY, scale
                
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
                int baseIndex = vertexCount;
                indexBuffer.put(baseIndex).put(baseIndex + 1).put(baseIndex + 2);
                indexBuffer.put(baseIndex + 2).put(baseIndex + 3).put(baseIndex);
                
                vertexCount += 4;
                indexCount += 6;
                x += info.xAdvance * text.scale;
                charCount++;
            }
        }
        
        if (charCount == 0) return;
        
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
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indexBuffer);
        
        // UN SEUL draw call pour tout !
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        
        glBindVertexArray(0);
        textShader.unbind();
        
        if (depthTestEnabled) {
            glEnable(GL_DEPTH_TEST);
        }
    }
    
    public void cleanup() {
        glDeleteTextures(textureId);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);
        textShader.cleanup();
    }
}