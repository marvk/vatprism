package net.marvk.fs.vatsim.map.data;

import com.github.zafarkhaja.semver.UnexpectedCharacterException;
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
        SEM_VERSION = tryParseRaw();
    }

    private static Version tryParseRaw() {
        try {
            return RAW_VERSION == null ? null : Version.valueOf(RAW_VERSION);
        } catch (final UnexpectedCharacterException e) {
            return null;
        }
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
