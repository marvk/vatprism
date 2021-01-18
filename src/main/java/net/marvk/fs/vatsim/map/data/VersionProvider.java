package net.marvk.fs.vatsim.map.data;

import com.google.inject.Singleton;
import com.sun.tools.javac.Main;

@Singleton
public final class VersionProvider {
    private final String version;

    public VersionProvider() {
        final String version = Main.class.getPackage().getImplementationVersion();
        this.version = version == null ? "DEV" : version;
    }

    public String get() {
        return version;
    }
}
