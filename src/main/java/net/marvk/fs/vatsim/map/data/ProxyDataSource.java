package net.marvk.fs.vatsim.map.data;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.beans.property.StringProperty;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.VatsimApiDataSource;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimMapData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.StringJoiner;

@Log4j2
public class ProxyDataSource implements VatsimApiDataSource {
    private static final String FIRBOUNDARIES_FILENAME = "FIRBoundaries.dat";
    private static final String VATSPY_FILENAME = "VATSpy.dat";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final VatsimApiDataSource dataSource;
    private final Path configPath;
    private final Path cachePath;
    private final Preferences preferences;

    @Inject
    public ProxyDataSource(
            @Named("httpDataSource") final VatsimApiDataSource dataSource,
            @Named("userConfigDir") final Path configPath,
            @Named("userCacheDir") final Path cachePath,
            final Preferences preferences
    ) {
        this.dataSource = dataSource;
        this.configPath = configPath;
        this.cachePath = cachePath;
        this.preferences = preferences;
    }

    @Override
    public String data() throws VatsimApiException {
        return dataSource.data();
    }

    @Override
    public String metar(final String airportIcao) throws VatsimApiException {
        return dataSource.metar(airportIcao);
    }

    @Override
    public String firBoundaries() throws VatsimApiException {
        return tryLoadProxy(FIRBOUNDARIES_FILENAME, () -> tryLoadCached(FIRBOUNDARIES_FILENAME, dataSource::firBoundaries));
    }

    @Override
    public String vatSpy() throws VatsimApiException {
        return tryLoadProxy(VATSPY_FILENAME, () -> tryLoadCached(VATSPY_FILENAME, dataSource::vatSpy));
    }

    @Override
    public String mapData() throws VatsimApiException {
        return dataSource.mapData();
    }

    @Override
    public String events() throws VatsimApiException {
        throw new UnsupportedOperationException();
    }

    private String tryLoadCached(final String fileName, final FallbackSupplier fallbackSupplier) throws VatsimApiException {
        if (!preferences.booleanProperty("general.use_map_data_cache").get()) {
            log.info("Skipping map data cache for %s, cache was disabled".formatted(fileName));
            return fallbackSupplier.supply();
        }

        log.info("Trying to load cached file for %s".formatted(fileName));

        try {
            createCachePath();
        } catch (final IOException e) {
            log.info("Failed to create cache directory %s".formatted(cachePath.toAbsolutePath()), e);
            return fallbackSupplier.supply();
        }

        final Path path = cachePath.resolve(fileName);

        final String commitHash = new Gson()
                .fromJson(dataSource.mapData(), VatsimMapData.class)
                .getCurrentCommitHash();

        if (Files.exists(path)) {
            final String cacheCommitHash = commitHashProperty(fileName).get();
            final LocalDateTime cacheChangedAt = cacheChangedAt(fileName);
            final int cacheTtlInSeconds = cacheTtlInSeconds();
            final LocalDateTime cacheDeadLine = cacheChangedAt.plusSeconds(cacheTtlInSeconds);

            final boolean fileOutdated = cacheCommitHash == null || !cacheCommitHash.equals(commitHash);
            final LocalDateTime now = LocalDateTime.now();
            final boolean timeToLiveExceeded = now.isAfter(cacheDeadLine);

            if (fileOutdated || timeToLiveExceeded) {
                final StringJoiner reason = new StringJoiner(" and ");
                if (fileOutdated) {
                    reason.add("last and current commit hash differ (%s != %s)".formatted(cacheCommitHash, commitHash));
                }
                if (timeToLiveExceeded) {
                    reason.add("cache exceeded time to live (out of date for %s)".formatted(Duration.between(cacheDeadLine, now)));
                }

                try {
                    log.info("Updating %s in cache, %s)".formatted(fileName, reason));
                    final String result = supplyAndWrite(path, fallbackSupplier);
                    setChanged(fileName, commitHash);
                    return result;
                } catch (final IOException e) {
                    log.error("Failed to write %s to cache".formatted(fileName), e);
                }
            } else {
                try {
                    log.info("Reading file %s from cache with commit hash %s".formatted(fileName, commitHash));
                    return Files.readString(path);
                } catch (final IOException e) {
                    log.error("Failed to read cached file from %s".formatted(path.toAbsolutePath()), e);
                }
            }
        } else {
            try {
                log.info("Writing %s to cache, no existing files found".formatted(fileName));
                final String result = supplyAndWrite(path, fallbackSupplier);
                setChanged(fileName, commitHash);
                return result;
            } catch (final IOException e) {
                log.error("Failed to write %s to cache".formatted(fileName), e);
            }
        }

        log.info("Failed to read from or write to cache %s, using fallback...".formatted(fileName));
        return fallbackSupplier.supply();
    }

    private LocalDateTime cacheChangedAt(final String fileName) {
        return LocalDateTime
                .parse(lastChangedTimestampProperty(fileName).get(), FORMATTER);
    }

    private int cacheTtlInSeconds() {
        return preferences.integerProperty("general.map_data_cache_ttl").get();
    }

    private void setChanged(final String fileName, final String currentCommitHash) {
        commitHashProperty(fileName).set(currentCommitHash);
        lastChangedTimestampProperty(fileName).set(LocalDateTime.now().format(FORMATTER));
    }

    private StringProperty commitHashProperty(final String fileName) {
        return preferences.stringProperty(propertyKey(fileName, "commit_hash"), "");
    }

    private StringProperty lastChangedTimestampProperty(final String fileName) {
        return preferences.stringProperty(
                propertyKey(fileName, "updated_at"),
                LocalDateTime.now().minusDays(1000).format(FORMATTER)
        );
    }

    private static String propertyKey(final String fileName, final String key) {
        return "meta.map_data.%s.%s".formatted(fileName.replaceAll("\\..*$", "").toLowerCase(Locale.ROOT), key);
    }

    private void createCachePath() throws IOException {
        if (!Files.exists(cachePath)) {
            log.info("Creating cache directory %s".formatted(cachePath));
            Files.createDirectories(cachePath);
        }
    }

    private String supplyAndWrite(final Path path, final FallbackSupplier fallbackSupplier) throws VatsimApiException, IOException {
        if (Files.exists(path)) {
            Files.delete(path);
        }
        final String result = fallbackSupplier.supply();
        Files.writeString(path, result, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return result;
    }

    private String tryLoadProxy(final String fileName, final FallbackSupplier fallbackSupplier) throws VatsimApiException {
        final Path path = configPath.resolve(fileName);
        if (Files.exists(path)) {
            log.info("Trying to load proxy file for %s".formatted(fileName));
            try {
                final String result = Files.readString(path);
                log.info("Loaded proxy file for %s".formatted(fileName));
                return result;
            } catch (final IOException e) {
                log.error("Failed to load proxy file from %s".formatted(path.toAbsolutePath()), e);
            }
        } else {
            log.info("Skipping proxy for %s, no file found".formatted(fileName));
        }

        return fallbackSupplier.supply();
    }

    @FunctionalInterface
    private interface FallbackSupplier {
        String supply() throws VatsimApiException;
    }
}
