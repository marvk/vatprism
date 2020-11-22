package net.marvk.fs.vatsim.map.repository;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimAirport;
import net.marvk.fs.vatsim.map.data.AirportViewModel;

import java.util.Collection;

public class AirportRepository extends ProviderRepository<VatsimAirport, AirportViewModel> {
    @Inject
    public AirportRepository(final VatsimApi vatsimApi, final Provider<AirportViewModel> airportViewModelProvider) {
        super(vatsimApi, airportViewModelProvider);
    }

    @Override
    protected String extractKey(final VatsimAirport vatsimAirport) {
        return vatsimAirport.getIcao();
    }

    @Override
    protected Collection<VatsimAirport> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api.vatSpy().getAirports();
    }
}
