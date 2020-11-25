package net.marvk.fs.vatsim.map.repository;

import com.google.inject.Inject;
import com.google.inject.Provider;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimAirspace;
import net.marvk.fs.vatsim.api.data.VatsimAirspaceGeneral;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundaryViewModel;
import net.marvk.fs.vatsim.map.data.SimpleDataViewModel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class FlightInformationRegionBoundaryRepository extends ProviderRepository<VatsimAirspace, FlightInformationRegionBoundaryViewModel> {
    private final Lookup<FlightInformationRegionBoundaryViewModel> icao = new Lookup<>(e -> e.icaoProperty().get());

    @Inject
    public FlightInformationRegionBoundaryRepository(final VatsimApi vatsimApi, final Provider<FlightInformationRegionBoundaryViewModel> flightInformationRegionBoundaryViewModelProvider) {
        super(vatsimApi, flightInformationRegionBoundaryViewModelProvider);
    }

    @Override
    protected String extractKey(final VatsimAirspace vatsimAirspace) {
        final VatsimAirspaceGeneral general = vatsimAirspace.getGeneral();
        return String.join(general.getIcao(), String.valueOf(general.getExtension()), String.valueOf(general.getOceanic()));
    }

    @Override
    protected Collection<VatsimAirspace> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api.firBoundaries().getAirspaces();
    }

    @Override
    protected void onAdd(final VatsimAirspace airspace, final FlightInformationRegionBoundaryViewModel toAdd) {
        icao.put(toAdd);
    }

    public List<FlightInformationRegionBoundaryViewModel> getByIcao(final String icao) {
        return this.icao.get(icao);
    }

    @Override
    public void reload() throws RepositoryException {
        super.reload();

        final List<FlightInformationRegionBoundaryViewModel> extensions =
                list()
                        .stream()
                        .filter(e -> e.extensionProperty().get())
                        .collect(Collectors.toList());

        list.removeAll(extensions);

        for (final FlightInformationRegionBoundaryViewModel extension : extensions) {
            final Optional<FlightInformationRegionBoundaryViewModel> maybeParent =
                    list.stream()
                        .filter(e -> e.oceanicProperty().get() == extension.oceanicProperty().get())
                        .filter(e -> e.icaoProperty().get().equals(extension.icaoProperty().get()))
                        .findFirst();

            if (maybeParent.isPresent()) {
                final FlightInformationRegionBoundaryViewModel parent = maybeParent.get();

                parent.mergeInto(extension);
            } else {
                log.warn("No parent found for extension FIR " + extractKey(extension.getModel()));
            }
        }
    }

    public FlightInformationRegionBoundaryViewModel getByIcao(final String icao, final boolean oceanic, final boolean extension) {
        final var firs = this.getByIcao(icao);
        final var result = firs
                .stream()
                .filter(e -> icao.equals(e.icaoProperty().get()))
                .filter(e -> e.extensionProperty().get() == extension)
                .filter(e -> e.oceanicProperty().get() == oceanic)
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            return null;
        }

        if (result.size() > 1) {
            final List<VatsimAirspace> firsDebug = firs
                    .stream()
                    .map(SimpleDataViewModel::getModel)
                    .collect(Collectors.toList());
            log.warn("Duplicate FIRs: " + firsDebug);
        }

        return result.get(0);
    }
}
