package net.marvk.fs.vatsim.map.api;

public interface VatprismApi {
    VersionResponse checkVersion(final UpdateChannel channel) throws VatprismApiException;

    void submitThemeChoice(final String themeName) throws VatprismApiException;
}
