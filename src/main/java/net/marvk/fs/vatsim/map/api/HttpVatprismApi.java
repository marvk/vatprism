package net.marvk.fs.vatsim.map.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.commons.motd.MessageOfTheDay;
import net.marvk.fs.vatsim.map.commons.motd.MessageOfTheDayObjectMapper;
import net.marvk.fs.vatsim.map.data.VersionProvider;

import java.io.IOException;
import java.io.Serial;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Log4j2
public class HttpVatprismApi implements VatprismApi {
    private static final TypeReference<List<MessageOfTheDay>> LIST_OF_MOTDS_TYPE_REFERENCE = new TypeReference<>() {
    };
    private static final MessageOfTheDayObjectMapper MOTDS_MAPPER = new MessageOfTheDayObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final VersionProvider versionProvider;
    private final String versionUrl;
    private final String themeUrl;
    private final String motdsUrl;
    private final Duration timeout;

    @Inject
    public HttpVatprismApi(
            final VersionProvider versionProvider,
            @Named("apiVersionUrl") final String versionUrl,
            @Named("apiThemeUrl") final String themeUrl,
            @Named("apiMotdsUrl") final String motdsUrl,
            @Named("apiTimeout") final Duration timeout
    ) {
        this.versionProvider = versionProvider;
        this.versionUrl = versionUrl;
        this.themeUrl = themeUrl;
        this.motdsUrl = motdsUrl;
        this.timeout = timeout;
    }

    @Override
    public VersionResponse checkVersion(final UpdateChannel channel) throws VatprismApiException {
        final Map<String, Map<String, String>> response = tryRequestVersion(channel);
        log.info("Version response: %s".formatted(response));
        final Map<String, String> latestVersion = getVersionResponse(response, channel);

        final String latestVersionName = latestVersion.get("version");

        final String os = System.getProperty("os.name").toLowerCase();
        final String latestVersionUrl;
        if (os.startsWith("mac os x")) {
            latestVersionUrl = latestVersion.get("macosUrl");
        } else if (os.startsWith("windows")) {
            latestVersionUrl = latestVersion.get("windowsUrl");
        } else {
            latestVersionUrl = "https://github.com/marvk/vatprism";
        }

        final String body = latestVersion.get("body");
        final boolean outdated = isOutdated(latestVersionName);

        return VersionResponse.of(outdated, latestVersionName, sanitize(latestVersionUrl), body);
    }

    @Override
    public void submitThemeChoice(final String themeName) throws VatprismApiException {
        tryPostTheme(themeName);
    }

    @Override
    public List<MessageOfTheDay> messagesOfTheDay(final Version version, final Double focusedHours, final Double totalHours, final boolean unfiltered) throws VatprismApiException {
        try {
            return fetchMessagesOfTheDays(focusedHours, totalHours, unfiltered);
        } catch (URISyntaxException | IOException | InterruptedException | ServerResponseException e) {
            throw new VatprismApiException(e);
        }
    }

    private List<MessageOfTheDay> fetchMessagesOfTheDays(final Double focusedHours, final Double totalHours, final boolean unfiltered) throws URISyntaxException, IOException, InterruptedException, ServerResponseException {
        final String formatted = motdsUrl.formatted(
                URLEncoder.encode(versionProvider.getString(), StandardCharsets.UTF_8),
                URLEncoder.encode(Double.toString(focusedHours), StandardCharsets.UTF_8),
                URLEncoder.encode(Double.toString(totalHours), StandardCharsets.UTF_8),
                URLEncoder.encode(Boolean.toString(unfiltered), StandardCharsets.UTF_8)
        );

        final HttpRequest request = HttpRequest
                .newBuilder(new URI(formatted))
                .timeout(timeout)
                .GET()
                .build();

        final HttpResponse<String> response = makeRequest(request);

        return parseMotds(response.body());
    }

    private static List<MessageOfTheDay> parseMotds(final String jsonString) throws JsonProcessingException {
        return MOTDS_MAPPER.readValue(jsonString, LIST_OF_MOTDS_TYPE_REFERENCE);
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
        } catch (VatprismApiException | URISyntaxException | IOException | InterruptedException | ServerResponseException e) {
            throw new VatprismApiException("Failed to submit theme to server", e);
        }
    }

    private void postTheme(final String themeName) throws URISyntaxException, IOException, InterruptedException, VatprismApiException, ServerResponseException {
        final String formatted = themeUrl.formatted(
                URLEncoder.encode(versionProvider.getString(), StandardCharsets.UTF_8),
                URLEncoder.encode(themeName, StandardCharsets.UTF_8)
        );
        final HttpRequest request = HttpRequest
                .newBuilder(new URI(formatted))
                .timeout(timeout)
                .GET()
                .build();

        makeRequest(request);
    }

    private Map<String, Map<String, String>> tryRequestVersion(final UpdateChannel channel) throws VatprismApiException {
        try {
            return requestVersion(channel);
        } catch (URISyntaxException | IOException | InterruptedException | ServerResponseException e) {
            throw new VatprismApiException("Failed to fetch version from server", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> requestVersion(final UpdateChannel channel) throws URISyntaxException, IOException, InterruptedException, VatprismApiException, ServerResponseException {
        final String formatted = versionUrl.formatted(
                URLEncoder.encode(versionProvider.getString(), StandardCharsets.UTF_8),
                URLEncoder.encode(channel.toString(), StandardCharsets.UTF_8)
        );

        final HttpRequest request = HttpRequest
                .newBuilder(new URI(formatted))
                .timeout(timeout)
                .GET()
                .build();

        final HttpResponse<String> response = makeRequest(request);

        return new Gson().fromJson(response.body(), Map.class);
    }

    private HttpResponse<String> makeRequest(final HttpRequest request) throws IOException, InterruptedException, ServerResponseException {
        final HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        verifySuccessfulResponse(response);

        return response;
    }

    private static void verifySuccessfulResponse(final HttpResponse<String> response) throws ServerResponseException {

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new ServerResponseException(response.statusCode(), response.uri());
        }
    }

    private static class ServerResponseException extends Exception {

        @Serial
        private static final long serialVersionUID = -6889295245769514633L;

        public ServerResponseException(final int statusCode, final URI uri) {
            super("Invalid response from " + uri + ", status " + statusCode);
        }
    }

    private static String sanitize(final String latestVersionUrl) {
        return "https://github.com/" + latestVersionUrl.replaceFirst("https://github\\.com/", "");
    }
}
