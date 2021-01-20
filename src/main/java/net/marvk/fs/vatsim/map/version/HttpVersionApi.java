package net.marvk.fs.vatsim.map.version;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Log4j2
public class HttpVersionApi implements VersionApi {
    private final VersionProvider versionProvider;
    private final UpdateChannel channel;
    private final String url;
    private final Duration timeout;

    @Inject
    public HttpVersionApi(final VersionProvider versionProvider, final UpdateChannel channel, @Named("versionApiUrl") final String url, @Named("versionApiTimeout") final Duration timeout) {
        this.versionProvider = versionProvider;
        this.channel = channel;
        this.url = url;
        this.timeout = timeout;
    }

    @Override
    public VersionResponse checkVersion() throws VersionApiException {
        final Map<String, Map<String, String>> response = tryRequestVersion();
        log.info("Version response: %s".formatted(response));
        final Map<String, String> latestVersion = getVersionResponse(response);

        final String latestVersionName = latestVersion.get("version");
        final String latestVersionUrl = latestVersion.get("downloadUrl");
        final boolean outdated = isOutdated(latestVersionName);

        return VersionResponse.of(outdated, latestVersionName, latestVersionUrl);
    }

    private boolean isOutdated(final String latestVersionName) {
        final Version currentVersion = versionProvider.getVersion();
        if (currentVersion == null) {
            return false;
        } else {
            return Version.valueOf(latestVersionName).greaterThan(currentVersion);
        }
    }

    private Map<String, String> getVersionResponse(final Map<String, Map<String, String>> response) throws VersionApiException {
        for (final Map.Entry<String, Map<String, String>> e : response.entrySet()) {
            if (e.getKey().equalsIgnoreCase(channel.toString())) {
                return e.getValue();
            }
        }

        throw new VersionApiException();
    }

    private Map<String, Map<String, String>> tryRequestVersion() throws VersionApiException {
        try {
            return requestVersion();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new VersionApiException("Failed to fetch version from server", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> requestVersion() throws URISyntaxException, IOException, InterruptedException {
        final String formatted = this.url.formatted(versionProvider.getString(), channel);

        final HttpRequest build = HttpRequest
                .newBuilder(new URI(formatted))
                .timeout(timeout)
                .GET()
                .build();

        final HttpResponse<String> send = HttpClient.newHttpClient().send(
                build,
                HttpResponse.BodyHandlers.ofString()
        );

        return new Gson().fromJson(send.body(), Map.class);
    }
}
