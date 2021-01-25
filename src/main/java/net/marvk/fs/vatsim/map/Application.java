package net.marvk.fs.vatsim.map;

import lombok.extern.log4j.Log4j2;
import net.harawata.appdirs.AppDirs;
import net.marvk.fs.vatsim.map.version.VersionProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TODO Hacky way to start a JavaFX application without messing with modules
 */
public final class Application {
    private Application() {
        throw new AssertionError("No instances of main class " + Application.class);
    }

    public static void main(final String[] args) throws IOException {
        reconfigureLogger();
        new SystemInformationLogger().log();
        App.main(args);
    }

    private static void reconfigureLogger() throws IOException {
        final AppDirs instance = CustomAppDirsFactory.createInstance();
        final String path = instance.getUserLogDir("VATprism", null, null);
        Files.createDirectories(Path.of(path));
        System.setProperty("log4j2.saveDirectory", path);
    }

    @Log4j2
    private static class SystemInformationLogger {
        public void log() {
            log.info("VATprism version:   %s".formatted(new VersionProvider().getString()));
            log.info("Operating system:   %s".formatted(System.getProperty("os.name")));
            log.info("Available cores:    %s".formatted(Runtime.getRuntime().availableProcessors()));
            log.info("Total JVM memory:   %s".formatted(Runtime.getRuntime().freeMemory()));
            log.info("Maximum JVM memory: %s".formatted(Runtime.getRuntime().maxMemory()));
            log.info("Total JVM memory:   %s".formatted(Runtime.getRuntime().totalMemory()));
        }
    }
}
