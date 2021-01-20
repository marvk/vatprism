package net.marvk.fs.vatsim.map;

import lombok.extern.log4j.Log4j2;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import net.marvk.fs.vatsim.map.version.VersionProvider;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TODO Hacky way to start a JavaFX application without messing with modules
 */
@Log4j2
public final class Application {
    private Application() {
        throw new AssertionError("No instances of main class " + Application.class);
    }

    public static void main(final String[] args) throws IOException {
        reconfigureLogger();
        logSystemInformation();
        App.main(args);
    }

    private static void reconfigureLogger() throws IOException {
        final AppDirs instance = AppDirsFactory.getInstance();
        final String path = instance.getUserLogDir("VATprism", null, null);
        Files.createDirectories(Path.of(path));
        System.setProperty("log4j2.saveDirectory", path);

        // load a separate config to avoid error messages of a missing log4j2.saveDirectory property
        final InputStream inputStream = Application.class.getResourceAsStream("/log4j2-app.xml");
        final Configuration xmlConfiguration = new XmlConfiguration(LoggerContext.getContext(), new ConfigurationSource(inputStream));

        Configurator.reconfigure(xmlConfiguration);
    }

    private static void logSystemInformation() {
        log.info("VATprism version:   %s".formatted(new VersionProvider().getString()));
        log.info("Operating system:   %s".formatted(System.getProperty("os.name")));
        log.info("Available cores:    %s".formatted(Runtime.getRuntime().availableProcessors()));
        log.info("Total JVM memory:   %s".formatted(Runtime.getRuntime().freeMemory()));
        log.info("Maximum JVM memory: %s".formatted(Runtime.getRuntime().maxMemory()));
        log.info("Total JVM memory:   %s".formatted(Runtime.getRuntime().totalMemory()));
    }
}
