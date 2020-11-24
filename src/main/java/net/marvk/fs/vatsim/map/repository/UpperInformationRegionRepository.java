package net.marvk.fs.vatsim.map.repository;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimUpperInformationRegion;
import net.marvk.fs.vatsim.map.data.UpperInformationRegionViewModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UpperInformationRegionRepository extends ProviderRepository<VatsimUpperInformationRegion, UpperInformationRegionViewModel> {
    @Inject
    public UpperInformationRegionRepository(final VatsimApi vatsimApi, final Provider<UpperInformationRegionViewModel> upperInformationRegionViewModelProvider) {
        super(vatsimApi, upperInformationRegionViewModelProvider);
    }

    @Override
    protected String extractKey(final VatsimUpperInformationRegion vatsimUpperInformationRegion) {
        return vatsimUpperInformationRegion.getIcao();
    }

    @Override
    protected Collection<VatsimUpperInformationRegion> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api.vatSpy().getUpperInformationRegions();
    }

    public List<UpperInformationRegionViewModel> getByIcao(final String icao) {
        return Collections.singletonList(getByKey(icao));
    }
}
