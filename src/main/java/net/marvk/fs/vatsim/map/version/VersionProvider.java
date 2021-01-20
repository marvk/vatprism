package net.marvk.fs.vatsim.map.version;

import com.github.zafarkhaja.semver.Version;
import com.google.inject.Singleton;

@Singleton
public final class VersionProvider {
    private static final String VERSION;
    private static final String RAW_VERSION;
    private static final Version SEM_VERSION;

    static {
        RAW_VERSION = VersionProvider.class.getPackage().getImplementationVersion();
        VERSION = RAW_VERSION == null ? "DEV" : RAW_VERSION;
        SEM_VERSION = RAW_VERSION == null ? null : Version.valueOf(RAW_VERSION);
    }

    public String getString() {
        return VERSION;
    }

    public String getStringNullable() {
        return RAW_VERSION;
    }

    public Version getVersion() {
        return SEM_VERSION;
    }
}
