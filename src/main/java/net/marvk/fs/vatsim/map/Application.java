package net.marvk.fs.vatsim.map;

import lombok.extern.log4j.Log4j2;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import net.marvk.fs.vatsim.map.data.VersionProvider;

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
        SystemInformationLogger.logGeneralInformation();
        App.main(args);
    }

    private static void reconfigureLogger() throws IOException {
        final AppDirs instance = AppDirsFactory.getInstance();
        final String path = instance.getUserLogDir("VATprism", null, null);
        Files.createDirectories(Path.of(path));
        System.setProperty("log4j2.saveDirectory", path);
    }

    @Log4j2
    private static final class SystemInformationLogger {
        private SystemInformationLogger() {
            throw new AssertionError("No instances of utility class " + SystemInformationLogger.class);
        }

        public static void logGeneralInformation() {
            log.info("VATprism version:   %s".formatted(new VersionProvider().getString()));
            log.info("Operating system:   %s (%s)".formatted(System.getProperty("os.name"), System.getProperty("os.arch")));
            log.info("Available threads:  %s".formatted(Runtime.getRuntime().availableProcessors()));
            log.info("JVM Free memory:    %s".formatted(toMbString(Runtime.getRuntime().freeMemory())));
            log.info("JVM Maximum memory: %s".formatted(toMbString(Runtime.getRuntime().maxMemory())));
            log.info("JVM Total memory:   %s".formatted(toMbString(Runtime.getRuntime().totalMemory())));
            log.info("JRE Version         %s".formatted(System.getProperty("java.version")));
            log.info("JRE Vendor          %s (%s)".formatted(System.getProperty("java.vendor"), System.getProperty("java.vendor.url")));
            log.info("JRE Home            %s".formatted(System.getProperty("java.home")));
            log.info("JVM Version         %s".formatted(System.getProperty("java.vm.version")));
            log.info("JVM Vendor          %s".formatted(System.getProperty("java.vm.vendor")));
            log.info("JVM Name            %s".formatted(System.getProperty("java.vm.name")));
            log.info("Java Class Version  %s".formatted(System.getProperty("java.class.version")));
        }

        private static String toMbString(final long l) {
            return Long.toString(l / 1000000) + "MB";
        }
    }
}
