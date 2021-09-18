package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.VatsimApiDataSource;
import net.marvk.fs.vatsim.api.VatsimApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Log4j2
public class ProxyDataSource implements VatsimApiDataSource {
    private final VatsimApiDataSource dataSource;
    private final Path configPath;

    @Inject
    public ProxyDataSource(@Named("httpDataSource") final VatsimApiDataSource dataSource, @Named("userConfigDir") final Path configPath) {
        this.dataSource = dataSource;
        this.configPath = configPath;
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
        return tryLoadProxy("FIRBoundaries.dat", dataSource::firBoundaries);
    }

    @Override
    public String vatSpy() throws VatsimApiException {
        return tryLoadProxy("VATSpy.dat", dataSource::vatSpy);
    }

    @Override
    public String mapData() throws VatsimApiException {
        return dataSource.mapData();
    }

    @Override
    public String events() throws VatsimApiException {
        return dataSource.events();
    }

    private String tryLoadProxy(final String fileName, final Callable<String> fallbackSupplier) throws VatsimApiException {
        final Path path = configPath.resolve(fileName);
        log.info("Trying to load proxy file for %s".formatted(fileName));
        if (Files.exists(path)) {
            try {
                final String result = Files.readString(path);
                log.info("Loaded proxy file for %s".formatted(fileName));
                return result;
            } catch (final IOException e) {
                log.error("Failed to load proxy file from " + path.toAbsolutePath(), e);
            }
        } else {
            log.info("Skipping proxy for %s, no file found".formatted(fileName));
        }

        try {
            return fallbackSupplier.call();
        } catch (final Exception e) {
            throw new VatsimApiException(e);
        }
    }
}
