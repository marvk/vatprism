package net.marvk.fs.vatsim.map.version;

import lombok.Value;

@Value
public class VersionResponse {
    Result result;
    String latestVersion;
    String url;

    public static VersionResponse of(final boolean outdated, final String name, final String url) {
        return new VersionResponse(outdated ? Result.OUTDATED : Result.CURRENT, name, url);
    }

    public enum Result {
        CURRENT, OUTDATED;
    }
}
