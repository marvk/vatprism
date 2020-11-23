package net.marvk.fs.vatsim.map.repository;

import com.google.inject.Provider;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.map.data.DataViewModel;

public abstract class ProviderRepository<Model, ViewModel extends DataViewModel<Model, ViewModel>> extends SimpleRepository<Model, ViewModel> {
    private final Provider<ViewModel> viewModelProvider;

    public ProviderRepository(final VatsimApi vatsimApi, final Provider<ViewModel> viewModelProvider) {
        super(vatsimApi);
        this.viewModelProvider = viewModelProvider;
    }

    @Override
    protected ViewModel create(final Model model) {
        final ViewModel viewModel = viewModelProvider.get();
        viewModel.setModel(model);
        return viewModel;
    }
}
