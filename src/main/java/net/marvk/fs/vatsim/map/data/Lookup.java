package net.marvk.fs.vatsim.map.data;

import java.util.*;
import java.util.function.Function;

class Lookup<ViewModel> {
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

            lookup.putIfAbsent(key, new ArrayList<>(1));
            lookup.get(key).add(viewModel);
        }
    }

    List<ViewModel> get(final String key) {
        return lookup.getOrDefault(key, Collections.emptyList());
    }
}
