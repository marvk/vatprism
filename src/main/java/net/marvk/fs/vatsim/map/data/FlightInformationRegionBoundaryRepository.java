package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.Provider;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimAirspace;
import net.marvk.fs.vatsim.api.data.VatsimAirspaceGeneral;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class FlightInformationRegionBoundaryRepository extends ProviderRepository<FlightInformationRegionBoundary, VatsimAirspace> {
    private final Lookup<FlightInformationRegionBoundary> icao = Lookup.fromProperty(FlightInformationRegionBoundary::getIcao);

    @Inject
    public FlightInformationRegionBoundaryRepository(final VatsimApi vatsimApi, final Provider<FlightInformationRegionBoundary> provider) {
        super(vatsimApi, provider);
    }

    @Override
    protected String keyFromModel(final VatsimAirspace airspace) {
        final VatsimAirspaceGeneral general = airspace.getGeneral();
        return String.join(
                "_",
                general.getIcao(),
                String.valueOf(general.getExtension()),
                String.valueOf(general.getOceanic())
        );
    }

    @Override
    protected String keyFromViewModel(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
        return String.join(
                "_",
                flightInformationRegionBoundary.getIcao(),
                String.valueOf(flightInformationRegionBoundary.isExtension()),
                String.valueOf(flightInformationRegionBoundary.isOceanic())
        );
    }

    @Override
    protected Collection<VatsimAirspace> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api.firBoundaries().getAirspaces();
    }

    @Override
    protected void onAdd(final FlightInformationRegionBoundary toAdd, final VatsimAirspace airspace) {
        icao.put(toAdd);
    }

    @Override
    public void reload() throws RepositoryException {
        super.reload();

        final List<FlightInformationRegionBoundary> extensions =
                list()
                        .stream()
                        .filter(FlightInformationRegionBoundary::isExtension)
                        .collect(Collectors.toList());

        items.removeAll(extensions);

        for (final FlightInformationRegionBoundary extension : extensions) {
            final Optional<FlightInformationRegionBoundary> maybeParent =
                    items.stream()
                         .filter(e -> e.isOceanic() == extension.isOceanic())
                         .filter(e -> e.getIcao().equals(extension.getIcao()))
                         .findFirst();

            if (maybeParent.isPresent()) {
                final FlightInformationRegionBoundary parent = maybeParent.get();

                parent.mergeInto(extension);
            } else {
                log.warn("No parent found for extension FIR " + keyFromViewModel(extension));
            }
        }
    }

    public List<FlightInformationRegionBoundary> getByIcao(final String icao) {
        return this.icao.get(icao);
    }

    public FlightInformationRegionBoundary getByIcao(final String icao, final boolean oceanic, final boolean extension) {
        final List<FlightInformationRegionBoundary> firs = getByIcao(icao);
        final var result = firs
                .stream()
                .filter(e -> icao.equals(e.getIcao()))
                .filter(e -> e.isExtension() == extension)
                .filter(e -> e.isOceanic() == oceanic)
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            return null;
        }

        if (result.size() > 1) {
            log.warn("Duplicate FIRs: " + firs);
        }

        return result.get(0);
    }

}
