package net.marvk.fs.vatsim.map;

/**
 * TODO Hacky way to start a JavaFX application without messing with modules
 */
public final class Application {
    private Application() {
        throw new AssertionError("No instances of main class " + Application.class);
    }

    public static void main(final String[] args) {
        App.main(args);
    }
}
