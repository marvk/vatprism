package net.marvk.fs.vatsim.map.data;

import java.util.*;
import java.util.function.Function;

final class Lookup<ViewModel> {
    private final Map<String, List<ViewModel>> lookup = new HashMap<>();
    private final Function<ViewModel, Collection<String>> keyExtractor;

    private Lookup(final Function<ViewModel, Collection<String>> keyExtractor) {
        this.keyExtractor = keyExtractor;
    }

    static <ViewModel> Lookup<ViewModel> fromProperty(final Function<ViewModel, String> keyExtractor) {
        return new Lookup<>(viewModel -> Collections.singletonList(keyExtractor.apply(viewModel)));
    }

    static <ViewModel> Lookup<ViewModel> fromCollection(final Function<ViewModel, Collection<String>> keyExtractor) {
        return new Lookup<>(keyExtractor);
    }

    void put(final ViewModel viewModel) {
        for (final String key : keyExtractor.apply(viewModel)) {
            if (key == null) {
                return;
            }

            viewModels(key).add(viewModel);
        }
    }

    private List<ViewModel> viewModels(final String key) {
        lookup.putIfAbsent(key, new ArrayList<>(1));
        final List<ViewModel> viewModels = lookup.get(key);
        return viewModels;
    }

    List<ViewModel> get(final String key) {
        return lookup.getOrDefault(key, Collections.emptyList());
    }

    void remove(final ViewModel viewModel) {
        for (final String key : keyExtractor.apply(viewModel)) {
            if (key == null) {
                return;
            }

            final List<ViewModel> viewModels = viewModels(key);

            if (viewModel == null) {
                continue;
            }

            viewModels.remove(viewModel);

            if (viewModels.isEmpty()) {
                lookup.remove(key);
            }
        }
    }

    void removeAll(final Iterable<ViewModel> viewModels) {
        viewModels.forEach(this::remove);
    }
}
