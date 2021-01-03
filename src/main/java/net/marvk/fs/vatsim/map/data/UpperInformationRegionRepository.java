package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimUpperInformationRegion;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UpperInformationRegionRepository extends ProviderRepository<UpperInformationRegion, VatsimUpperInformationRegion> {
    private final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository;

    @Inject
    public UpperInformationRegionRepository(
            final VatsimApi vatsimApi,
            final Provider<UpperInformationRegion> provider,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository
    ) {
        super(vatsimApi, provider);
        this.flightInformationRegionBoundaryRepository = flightInformationRegionBoundaryRepository;
    }

    @Override
    protected String keyFromModel(final VatsimUpperInformationRegion vatsimUpperInformationRegion) {
        return vatsimUpperInformationRegion.getIcao();
    }

    @Override
    protected String keyFromViewModel(final UpperInformationRegion upperInformationRegion) {
        return upperInformationRegion.getIcao();
    }

    @Override
    protected Collection<VatsimUpperInformationRegion> extractModels(final VatsimApi api) throws VatsimApiException {
        return api.vatSpy().getUpperInformationRegions();
    }

    @Override
    protected void onAdd(final UpperInformationRegion toAdd, final VatsimUpperInformationRegion vatsimUpperInformationRegion) {
        vatsimUpperInformationRegion
                .getSubordinateFlightInformationRegions()
                .stream()
                .map(flightInformationRegionBoundaryRepository::getByIcao)
                .flatMap(Collection::stream)
                .forEach(e -> toAdd.getFlightInformationRegionBoundariesWritable().add(e));
    }

    public List<UpperInformationRegion> getByIcao(final String identifier) {
        return Collections.singletonList(getByKey(identifier));
    }
}
