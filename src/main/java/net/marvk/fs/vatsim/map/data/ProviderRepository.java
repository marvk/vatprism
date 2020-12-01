package net.marvk.fs.vatsim.map.data;

import com.google.inject.Provider;
import net.marvk.fs.vatsim.api.VatsimApi;

public abstract class ProviderRepository<ViewModel extends Settable<Model>, Model> extends SimpleRepository<ViewModel, Model> {
    private final Provider<ViewModel> provider;

    public ProviderRepository(final VatsimApi vatsimApi, final Provider<ViewModel> provider) {
        super(vatsimApi);
        this.provider = provider;
    }

    @Override
    protected ViewModel newViewModelInstance(final Model model) {
        return provider.get();
    }
}
