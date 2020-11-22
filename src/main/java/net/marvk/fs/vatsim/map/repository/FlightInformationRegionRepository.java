package net.marvk.fs.vatsim.map.repository;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimFlightInformationRegion;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionViewModel;

import java.util.Collection;

public class FlightInformationRegionRepository extends ProviderRepository<VatsimFlightInformationRegion, FlightInformationRegionViewModel> {
    @Inject
    public FlightInformationRegionRepository(final VatsimApi vatsimApi, final Provider<FlightInformationRegionViewModel> flightInformationRegionViewModelProvider) {
        super(vatsimApi, flightInformationRegionViewModelProvider);
    }

    @Override
    protected String extractKey(final VatsimFlightInformationRegion vatsimFlightInformationRegion) {
        return vatsimFlightInformationRegion.getIcao();
    }

    @Override
    protected Collection<VatsimFlightInformationRegion> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api.vatSpy().getFlightInformationRegions();
    }
}
