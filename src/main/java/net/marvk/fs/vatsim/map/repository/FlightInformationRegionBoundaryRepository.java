package net.marvk.fs.vatsim.map.repository;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimAirspace;
import net.marvk.fs.vatsim.api.data.VatsimAirspaceGeneral;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundaryViewModel;

import java.util.Collection;

public class FlightInformationRegionBoundaryRepository extends ProviderRepository<VatsimAirspace, FlightInformationRegionBoundaryViewModel> {
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
}
