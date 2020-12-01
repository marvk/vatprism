package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.Line;

import java.util.Collection;
import java.util.Collections;

public class InternationalDateLineRepository extends ProviderRepository<InternationalDateLine, Line> {
    @Inject
    public InternationalDateLineRepository(final VatsimApi vatsimApi, final Provider<InternationalDateLine> provider) {
        super(vatsimApi, provider);
    }

    @Override
    protected String keyFromModel(final Line points) {
        return "0";
    }

    @Override
    protected String keyFromViewModel(final InternationalDateLine internationalDateLine) {
        return "0";
    }

    @Override
    protected Collection<Line> extractModelList(final VatsimApi api) throws VatsimApiException {
        return Collections.singleton(api.vatSpy().getInternationDateLine());
    }
}
