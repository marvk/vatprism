package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SimpleRepository<ViewModel extends Settable<Model>, Model> implements Repository<ViewModel> {
    protected final VatsimApi vatsimApi;
    protected final Map<String, ViewModel> map = new HashMap<>();
    protected final ReadOnlyListWrapper<ViewModel> items = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    @Inject
    public SimpleRepository(final VatsimApi vatsimApi) {
        this.vatsimApi = vatsimApi;
    }

    protected abstract ViewModel newViewModelInstance(final Model model);

    protected abstract String keyFromModel(final Model model);

    protected abstract String keyFromViewModel(final ViewModel model);

    protected abstract Collection<Model> extractModelList(final VatsimApi api) throws VatsimApiException;

    protected void onAdd(final ViewModel toAdd, final Model model) {
    }

    protected void onRemove(final ViewModel toRemove) {
    }

    protected void onUpdate(final ViewModel toUpdate, final Model model) {
    }

    @Override
    public ViewModel getByKey(final String key) {
        return map.get(key);
    }

    @Override
    public void reload() throws RepositoryException {
        try {
            final Collection<Model> models = extractModelList(vatsimApi);

            final Set<String> keysInUpdate = models
                    .stream()
                    .map(this::keyFromModel)
                    .collect(Collectors.toSet());

            items.stream()
                 .filter(e -> !keysInUpdate.contains(keyFromViewModel(e)))
                 .forEach(this::onRemove);

            items.removeIf(e -> !keysInUpdate.contains(keyFromViewModel(e)));
            map.keySet().removeIf(o -> !keysInUpdate.contains(o));

            final List<ViewModel> toAdd = new ArrayList<>();

            for (final Model model : models) {
                final String key = keyFromModel(model);
                final ViewModel viewModel = map.get(key);
                if (viewModel == null) {
                    final ViewModel newViewModel = newViewModelInstance(model);
                    if (newViewModel == null) {
                        continue;
                    }
                    newViewModel.setFromModel(model);
                    map.put(key, newViewModel);
                    onAdd(newViewModel, model);
                    toAdd.add(newViewModel);
                } else {
                    viewModel.setFromModel(model);
                    onUpdate(viewModel, model);
                }
            }

            items.addAll(toAdd);
        } catch (final VatsimApiException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public ObservableList<ViewModel> list() {
        return items.getReadOnlyProperty();
    }
}
