package net.marvk.fs.vatsim.map.repository;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.Line;
import net.marvk.fs.vatsim.map.data.InternationalDateLineViewModel;

import java.util.Collection;
import java.util.Collections;

public class InternationalDateLineRepository extends ProviderRepository<Line, InternationalDateLineViewModel> {
    @Inject
    public InternationalDateLineRepository(final VatsimApi vatsimApi, final Provider<InternationalDateLineViewModel> internationalDateLineViewModelProvider) {
        super(vatsimApi, internationalDateLineViewModelProvider);
    }

    @Override
    protected String extractKey(final Line points) {
        return "_";
    }

    @Override
    protected Collection<Line> extractModelList(final VatsimApi api) throws VatsimApiException {
        return Collections.singletonList(api.vatSpy().getInternationDateLine());
    }
}
