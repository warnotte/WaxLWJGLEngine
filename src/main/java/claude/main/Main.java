package claude.main;

import claude.core.Application;

public class Main {
    public static void main(String[] args) {
        Application app = new Application("Moteur 2D OpenGL", 800, 600);
        app.run();
    }
}