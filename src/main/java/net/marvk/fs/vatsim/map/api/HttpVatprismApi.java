package net.marvk.fs.vatsim.map.api;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.VersionProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Log4j2
public class HttpVatprismApi implements VatprismApi {
    private final VersionProvider versionProvider;
    private final String versionUrl;
    private final String themeUrl;
    private final Duration timeout;

    @Inject
    public HttpVatprismApi(
            final VersionProvider versionProvider,
            @Named("apiVersionUrl") final String versionUrl,
            @Named("apiThemeUrl") final String themeUrl,
            @Named("versionApiTimeout") final Duration timeout
    ) {
        this.versionProvider = versionProvider;
        this.versionUrl = versionUrl;
        this.themeUrl = themeUrl;
        this.timeout = timeout;
    }

    @Override
    public VersionResponse checkVersion(final UpdateChannel channel) throws VatprismApiException {
        final Map<String, Map<String, String>> response = tryRequestVersion(channel);
        log.info("Version response: %s".formatted(response));
        final Map<String, String> latestVersion = getVersionResponse(response, channel);

        final String latestVersionName = latestVersion.get("version");
        final String latestVersionUrl = latestVersion.get("downloadUrl");
        final String body = latestVersion.get("body");
        final boolean outdated = isOutdated(latestVersionName);

        return VersionResponse.of(outdated, latestVersionName, sanitize(latestVersionUrl), body);
    }

    private static String sanitize(final String latestVersionUrl) {
        return "https://github.com/" + latestVersionUrl.replaceFirst("https://github\\.com/", "");
    }

    @Override
    public void submitThemeChoice(final String themeName) throws VatprismApiException {
        tryPostTheme(themeName);
    }

    private boolean isOutdated(final String latestVersionName) {
        final Version currentVersion = versionProvider.getVersion();
        if (currentVersion == null) {
            return false;
        } else {
            return Version.valueOf(latestVersionName).greaterThan(currentVersion);
        }
    }

    private static Map<String, String> getVersionResponse(final Map<String, Map<String, String>> response, final UpdateChannel channel) throws VatprismApiException {
        for (final Map.Entry<String, Map<String, String>> e : response.entrySet()) {
            if (e.getKey().equalsIgnoreCase(channel.toString())) {
                return e.getValue();
            }
        }

        throw new VatprismApiException();
    }

    private void tryPostTheme(final String themeName) throws VatprismApiException {
        try {
            postTheme(themeName);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new VatprismApiException("Failed to submit theme to server", e);
        }
    }

    private void postTheme(final String themeName) throws URISyntaxException, IOException, InterruptedException {
        final String formatted = themeUrl.formatted(
                URLEncoder.encode(versionProvider.getString(), StandardCharsets.UTF_8),
                URLEncoder.encode(themeName, StandardCharsets.UTF_8)
        );
        final HttpRequest build = HttpRequest
                .newBuilder(new URI(formatted))
                .timeout(timeout)
                .GET()
                .build();

        HttpClient.newHttpClient().send(
                build,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private Map<String, Map<String, String>> tryRequestVersion(final UpdateChannel channel) throws VatprismApiException {
        try {
            return requestVersion(channel);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new VatprismApiException("Failed to fetch version from server", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> requestVersion(final UpdateChannel channel) throws URISyntaxException, IOException, InterruptedException {
        final String formatted = versionUrl.formatted(
                URLEncoder.encode(versionProvider.getString(), StandardCharsets.UTF_8),
                URLEncoder.encode(channel.toString(), StandardCharsets.UTF_8)
        );
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
