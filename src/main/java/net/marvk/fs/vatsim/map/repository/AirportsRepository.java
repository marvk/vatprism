package net.marvk.fs.vatsim.map.repository;

import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimAirport;
import net.marvk.fs.vatsim.map.data.AirportViewModel;

import java.util.Collection;

public class AirportsRepository extends SimpleRepository<VatsimAirport, AirportViewModel> {
    public AirportsRepository(final VatsimApi vatsimApi) {
        super(vatsimApi);
    }

    @Override
    protected AirportViewModel map(final VatsimAirport vatsimAirport) {
        return new AirportViewModel(vatsimAirport);
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
