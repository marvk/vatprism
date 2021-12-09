package net.marvk.fs.vatsim.map.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.api.*;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.preferences.PreferencesView;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(VatsimApiUrlProvider.class).to(UrlProviderV3.class).in(Singleton.class);
        bind(VatsimApiDataSource.class).to(ProxyDataSource.class).in(Singleton.class);
        bind(AirportRepository.class).in(Singleton.class);
        bind(ClientRepository.class).in(Singleton.class);
        bind(FlightInformationRegionRepository.class).in(Singleton.class);
        bind(FlightInformationRegionBoundaryRepository.class).in(Singleton.class);
        bind(UpperInformationRegionRepository.class).in(Singleton.class);
        bind(InternationalDateLineRepository.class).in(Singleton.class);
        bind(CountryRepository.class).in(Singleton.class);
        bind(DependencyRepository.class).in(Singleton.class);
        bind(AirlineRepository.class).in(Singleton.class);
        bind(FilterRepository.class).in(Singleton.class);
        bind(CustomColorSchemeRepository.class).in(Singleton.class);
        bind(PackagedColorSchemeRepository.class).in(Singleton.class);
        bind(Preferences.class).to(ConfigFilePreferences.class).asEagerSingleton();
        bind(PreferencesView.class).asEagerSingleton();
        bind(TimeProvider.class).toInstance(new UpdatingTimeProvider(Duration.ofMinutes(1), true));
    }

    @Provides
    @Singleton
    private CloseableHttpClient closableHttpClient(@Named("userAgent") final String userAgent) {
        return HttpClientBuilder
                .create()
                .setUserAgent(userAgent)
                .build();
    }

    @Provides
    @Singleton
    @Named("userAgent")
    private String userAgent(final VersionProvider versionProvider) {
        final String userAgent = "VATprism-Client/%s".formatted(versionProvider.getString());
        log.info("User Agent: %s".formatted(userAgent));
        return userAgent;
    }

    @Provides
    @Singleton
    @Named("httpDataSource")
    private VatsimApiDataSource vatsimApiDataSource(final CloseableHttpClient closeableHttpClient) {
        return new HttpDataSource(closeableHttpClient);
    }

    @Provides
    @Named("licenseFileName")
    public String licenseFileName() {
        return "/THIRD-PARTY.txt";
    }

    @Provides
    @Named("airlineFileName")
    public String airlineFileName() {
        return "airlines.csv";
    }

    @Provides
    @Singleton
    public VatsimApi vatsimApi(final VatsimApiDataSource dataSource) {
        final SimpleVatsimApi api = new SimpleVatsimApi(dataSource);
        return new CachedVatsimApi(api, Duration.ofSeconds(5));
    }

    @Provides
    @Named("worldShapefileUrl")
    public List<String> worldShapefileUrls() {
        return Collections.singletonList("ne_50m_land");
    }

    @Provides
    @Named("lakesShapefileUrl")
    public List<String> lakesShapefileUrl() {
        return List.of(
//                "ne_10m_lakes_north_america",
//                "ne_10m_lakes_europe",
                "ne_50m_lakes"
        );
    }

    @Provides
    @Singleton
    @Named("world")
    public PolygonRepository world(@Named("worldShapefileUrl") final List<String> shapefileUrls) throws IOException {
        return new PolygonRepository(shapefileUrls, shpUrls(shapefileUrls));
    }

    @Provides
    @Singleton
    @Named("lakes")
    public PolygonRepository lakes(@Named("lakesShapefileUrl") final List<String> shapefileUrls) throws IOException {
        return new PolygonRepository(shapefileUrls, shpUrls(shapefileUrls));
    }

    @Provides
    @Singleton
    @Named("vatsimApiRefreshRate")
    public Duration vatsimApiRefreshRate() {
        return Duration.ofSeconds(15);
    }

    private static List<URL> shpUrls(final List<String> shapefileUrls) {
        return shapefileUrls.stream().map(AppModule::shpUrl).collect(Collectors.toList());
    }

    private static URL shpUrl(final String name) {
        return url(name, "shp");
    }

    private static URL url(final String name, final String extension) {
        return AppModule.class.getResource("/net/marvk/fs/vatsim/map/world/%s/%s.%s".formatted(name, name, extension));
    }
}
