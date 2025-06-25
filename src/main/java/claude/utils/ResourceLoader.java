package claude.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class ResourceLoader {
    public static String loadShader(String filename) {
        try {
            return Files.readString(Path.of(filename));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shader: " + filename, e);
        }
    }
}