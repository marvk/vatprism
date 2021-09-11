package net.marvk.fs.vatsim.map.version;

import lombok.Value;

@Value
public class VersionResponse {
    Result result;
    String latestVersion;
    String url;
    String changelog;

    public static VersionResponse of(final boolean outdated, final String name, final String url, final String changelog) {
        return new VersionResponse(outdated ? Result.OUTDATED : Result.CURRENT, name, url, changelog);
    }

    public enum Result {
        CURRENT, OUTDATED;
    }
}
