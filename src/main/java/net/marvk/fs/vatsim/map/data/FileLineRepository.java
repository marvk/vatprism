package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class FileLineRepository<ViewModel> implements ReadOnlyRepository<ViewModel> {
    private final String fileName;
    protected ReadOnlyListProperty<ViewModel> viewModels;

    public FileLineRepository(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public ObservableList<ViewModel> list() {
        if (viewModels == null) {
            viewModels = load();
        }

        return viewModels;
    }

    protected ReadOnlyListProperty<ViewModel> load() {
        return preSave(
                Stream.concat(loadViewModelsFromFile(), additionalViewModels())
        ).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableListProperty::new));
    }

    protected Stream<ViewModel> preSave(final Stream<ViewModel> stream) {
        return stream;
    }

    public abstract String extractKey(final ViewModel viewModel);

    protected abstract Optional<ViewModel> parseLine(final String line);

    protected Stream<ViewModel> additionalViewModels() {
        return Stream.empty();
    }

    protected Stream<ViewModel> loadViewModelsFromFile() {
        final InputStream resource = getClass().getResourceAsStream(fileName);

        return new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                .lines()
                .map(this::parseLine)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    @Override
    public ViewModel getByKey(final String key) {
        return list()
                .filtered(e -> key.equals(extractKey(e)))
                .stream()
                .findFirst()
                .orElse(null);
    }
}
