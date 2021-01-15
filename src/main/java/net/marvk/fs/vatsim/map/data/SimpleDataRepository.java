package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;

import java.util.*;
import java.util.stream.Collectors;

public abstract class SimpleDataRepository<ViewModel extends Settable<Model>, Model> implements ReloadableRepository<ViewModel> {
    protected final VatsimApi vatsimApi;
    protected final Map<String, ViewModel> map = new HashMap<>();
    protected final ReadOnlyListWrapper<ViewModel> items = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    @Inject
    public SimpleDataRepository(final VatsimApi vatsimApi) {
        this.vatsimApi = vatsimApi;
    }

    protected abstract ViewModel newViewModelInstance(final Model model);

    protected abstract String keyFromModel(final Model model);

    protected abstract String keyFromViewModel(final ViewModel model);

    protected abstract Collection<Model> extractModels(final VatsimApi api) throws VatsimApiException;

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
            updateList(extractModels(vatsimApi));
        } catch (final VatsimApiException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void reloadAsync(final Runnable onSucceed) throws RepositoryException {
        try {
            final Collection<Model> models = extractModels(vatsimApi);
            Platform.runLater(() -> {
                updateList(models);
                if (onSucceed != null) {
                    onSucceed.run();
                }
            });
        } catch (final VatsimApiException e) {
            throw new RepositoryException(e);
        }
    }

    protected void updateList(final Collection<Model> updatedModels) {
        final Set<String> keysInUpdate = updatedModels
                .stream()
                .map(this::keyFromModel)
                .collect(Collectors.toSet());

        items.stream()
             .filter(e -> !keysInUpdate.contains(keyFromViewModel(e)))
             .forEach(this::onRemove);

        items.removeIf(e -> !keysInUpdate.contains(keyFromViewModel(e)));
        map.keySet().removeIf(o -> !keysInUpdate.contains(o));

        final List<ViewModel> toAdd = new ArrayList<>();

        for (final Model model : updatedModels) {
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
    }

    @Override
    public ObservableList<ViewModel> list() {
        return items.getReadOnlyProperty();
    }
}
