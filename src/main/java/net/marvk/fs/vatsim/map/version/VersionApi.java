package net.marvk.fs.vatsim.map.version;

public interface VersionApi {
    VersionResponse checkVersion(final UpdateChannel channel) throws VersionApiException;
}
