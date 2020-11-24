package net.marvk.fs.vatsim.map.repository;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimAirport;
import net.marvk.fs.vatsim.map.data.AirportViewModel;

import java.util.Collection;
import java.util.List;

public class AirportRepository extends ProviderRepository<VatsimAirport, AirportViewModel> {
    private final Lookup<AirportViewModel> icao = new Lookup<>(e -> e.icaoProperty().get());
    private final Lookup<AirportViewModel> iataLid = new Lookup<>(e -> e.iataLidProperty().get());

    @Inject
    public AirportRepository(final VatsimApi vatsimApi, final Provider<AirportViewModel> airportViewModelProvider) {
        super(vatsimApi, airportViewModelProvider);
    }

    @Override
    protected String extractKey(final VatsimAirport vatsimAirport) {
        return vatsimAirport.getIcao() + "_" + vatsimAirport.getIataLid();
    }

    @Override
    protected Collection<VatsimAirport> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api.vatSpy().getAirports();
    }

    @Override
    protected void onAdd(final VatsimAirport vatsimAirport, final AirportViewModel toAdd) {
        icao.put(toAdd);
        iataLid.put(toAdd);
    }

    public List<AirportViewModel> getByIataLid(final String iataLid) {
        return this.iataLid.get(iataLid);
    }

    public List<AirportViewModel> getByIcao(final String icao) {
        return this.icao.get(icao);
    }
}
