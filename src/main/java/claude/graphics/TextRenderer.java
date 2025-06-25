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
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;

import claude.core.Window;

public class TextRenderer {
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
        float x, y, width, height; // Position et taille dans l'atlas
        float xOffset, yOffset;    // Offset pour le rendu
        float xAdvance;            // Avancement du curseur
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
    
    private static final String TEXT_VERTEX_SHADER = """
        #version 330 core
        layout (location = 0) in vec2 aPos;
        layout (location = 1) in vec2 aTexCoord;
        
        out vec2 TexCoord;
        
        uniform mat4 projection;
        uniform mat4 view;
        uniform vec2 worldPos;
        uniform vec2 scale;
        uniform vec2 windowSize;
        
        void main() {
            // Position monde vers position écran
            vec4 screenPos = projection * view * vec4(worldPos, 0.0, 1.0);
            
            // Appliquer l'échelle et convertir en NDC
            vec2 scaledPos = aPos * scale;
            vec2 ndcOffset = scaledPos / windowSize * 2.0;
            
            gl_Position = vec4(screenPos.xy + ndcOffset, 0.0, 1.0);
            TexCoord = aTexCoord;
        }
        """;
    
    private static final String TEXT_FRAGMENT_SHADER = """
        #version 330 core
        in vec2 TexCoord;
        out vec4 FragColor;
        
        uniform sampler2D fontTexture;
        uniform vec4 textColor;
        
        void main() {
            vec4 sampled = texture(fontTexture, TexCoord);
            FragColor = vec4(textColor.rgb, textColor.a * sampled.r);
        }
        """;
    
    public TextRenderer() {
        // Créer le shader
        textShader = new ShaderProgram(TEXT_VERTEX_SHADER, TEXT_FRAGMENT_SHADER);
        textShader.createUniform("projection");
        textShader.createUniform("view");
        textShader.createUniform("worldPos");
        textShader.createUniform("scale");
        textShader.createUniform("windowSize");
        textShader.createUniform("fontTexture");
        textShader.createUniform("textColor");
        
        // Créer les buffers
        createBuffers();
        
        // Générer l'atlas de texture
        generateFontAtlas();
    }
    
    private void createBuffers() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();
        
        glBindVertexArray(vao);
        
        // Buffer pour les vertices (sera rempli dynamiquement)
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        
        // Vertices pour un quad (position + UV)
        float[] vertices = new float[4 * 4]; // 4 vertices * 4 floats
        glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);
        
        // Indices pour deux triangles
        int[] indices = { 0, 1, 2, 2, 3, 0 };
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        
        // Attributs
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        glBindVertexArray(0);
    }
    
    private void generateFontAtlas() {
        // Créer une police Java AWT
        font = new Font("Arial", Font.PLAIN, fontSize);
        
        // Créer une image pour l'atlas
        BufferedImage atlas = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = atlas.createGraphics();
        
        // Configuration pour un meilleur rendu
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        
        FontMetrics metrics = g2d.getFontMetrics();
        fontHeight = metrics.getHeight();
        
        // Position actuelle dans l'atlas
        int x = 0;
        int y = 0;
        int rowHeight = 0;
        
        // Générer les caractères ASCII imprimables (32-126)
        for (char c = 32; c < 127; c++) {
            int charWidth = metrics.charWidth(c);
            int charHeight = metrics.getHeight();
            
            // Nouvelle ligne si nécessaire
            if (x + charWidth >= textureWidth) {
                x = 0;
                y += rowHeight + 2;
                rowHeight = 0;
            }
            
            // Dessiner le caractère
            g2d.drawString(String.valueOf(c), x, y + metrics.getAscent());
            
            // Stocker les informations du caractère
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
        
        // Convertir l'image en texture OpenGL
        createTextureFromImage(atlas);
    }
    
    private void createTextureFromImage(BufferedImage image) {
        // Convertir BufferedImage en ByteBuffer
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        
        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
                buffer.put((byte) (pixel & 0xFF));         // B
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
            }
        }
        
        buffer.flip();
        
        // Créer la texture OpenGL
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
        
        // Sauvegarder l'état
        boolean depthTestEnabled = glIsEnabled(GL_DEPTH_TEST);
        
        // Configuration pour le rendu du texte
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
        
        for (Text text : texts) {
            renderText(text);
        }
        
        glBindVertexArray(0);
        textShader.unbind();
        
        // Restaurer l'état
        if (depthTestEnabled) {
            glEnable(GL_DEPTH_TEST);
        }
    }
    
    private void renderText(Text text) {
        float x = 0;
        float y = 0;
        
        textShader.setUniform("worldPos", text.worldX, text.worldY);
        textShader.setUniform("textColor", text.r, text.g, text.b, text.a);
        
        for (char c : text.content.toCharArray()) {
            CharInfo info = charMap.get(c);
            if (info == null) continue;
            
            // Calculer les vertices pour ce caractère
            float x0 = x + info.xOffset;
            float y0 = y + info.yOffset;
            float x1 = x0 + info.xAdvance;
            float y1 = y0 + fontHeight;
            
            // Coordonnées UV
            float u0 = info.x;
            float v0 = info.y;
            float u1 = info.x + info.width;
            float v1 = info.y + info.height;
            
            // Vertices: position (2 floats) + UV (2 floats)
            float[] vertices = {
                x0, y0, u0, v0,  // Bottom-left
                x1, y0, u1, v0,  // Bottom-right
                x1, y1, u1, v1,  // Top-right
                x0, y1, u0, v1   // Top-left
            };
            
            // Appliquer l'échelle
            textShader.setUniform("scale", text.scale, text.scale);
            
            // Mettre à jour le VBO
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
            
            // Dessiner le caractère
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
            
            // Avancer le curseur
            x += info.xAdvance * text.scale;
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
    
    public float getTextHeight(float scale) {
        return fontHeight * scale;
    }
    
    public void cleanup() {
        glDeleteTextures(textureId);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);
        textShader.cleanup();
    }
}