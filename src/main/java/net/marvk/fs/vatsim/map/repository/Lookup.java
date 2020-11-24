package net.marvk.fs.vatsim.map.repository;

import javafx.beans.property.StringProperty;

import java.util.*;
import java.util.function.Function;

class Lookup<ViewModel> {
    private final Map<String, List<ViewModel>> lookup = new HashMap<>();
    private final Function<ViewModel, String> keyExtractor;

    Lookup(final Function<ViewModel, String> keyExtractor) {
        this.keyExtractor = keyExtractor;
    }

    void put(final ViewModel viewModel) {
        final String key = keyExtractor.apply(viewModel);

        if (key == null) {
            return;
        }

        lookup.putIfAbsent(key, new ArrayList<>(1));
        lookup.get(key).add(viewModel);
    }

    List<ViewModel> get(final String key) {
        return lookup.getOrDefault(key, Collections.emptyList());
    }
}
