package net.marvk.fs.vatsim.map.data;

import com.google.inject.Singleton;

@Singleton
public final class VersionProvider {
    private static final String VERSION;

    static {
        final String implementationVersion = VersionProvider.class.getPackage().getImplementationVersion();
        VERSION = implementationVersion == null ? "DEV" : implementationVersion;
    }

    public String get() {
        return VERSION;
    }
}
