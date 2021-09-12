package net.marvk.fs.vatsim.map.version;

public interface VersionApi {
    VersionResponse checkVersion(final UpdateChannel channel) throws VersionApiException;

    void submitThemeChoice(final String themeName) throws VersionApiException;
}
