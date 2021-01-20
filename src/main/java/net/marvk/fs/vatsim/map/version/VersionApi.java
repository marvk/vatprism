package net.marvk.fs.vatsim.map.version;

public interface VersionApi {
    VersionResponse checkVersion() throws VersionApiException;
}
