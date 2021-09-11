package net.marvk.fs.vatsim.map.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class PackagedColorSchemeRepository implements ReadOnlyRepository<ColorScheme> {
    private static final String PATH = "/net/marvk/fs/vatsim/map/color_schemes";

    private final ObservableList<ColorScheme> colorSchemes;

    @Inject
    public PackagedColorSchemeRepository(final ColorSchemeAdapter adapter) {
        colorSchemes = fileNamesStream(PATH)
                .peek(e -> log.info("Loading built in color scheme %s".formatted(e)))
                .map(PackagedColorSchemeRepository::createResourceStream)
                .map(PackagedColorSchemeRepository::toBytes)
                .map(String::new)
                .map(adapter::deserialize)
                .collect(Collectors.collectingAndThen(
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                FXCollections::observableList
                        ),
                        FXCollections::unmodifiableObservableList)
                );
    }

    @SneakyThrows
    public static Stream<String> fileNamesStream(final String path) {
        final byte[] bytes = PackagedColorSchemeRepository.class
                .getResourceAsStream(path)
                .readAllBytes();

        return new String(bytes).lines().map(String::trim).filter(e -> !e.isBlank());
    }

    @SneakyThrows
    private static byte[] toBytes(final InputStream e) {
        return e.readAllBytes();
    }

    private static InputStream createResourceStream(final String fileName) {
        return PackagedColorSchemeRepository.class.getResourceAsStream(PATH + "/" + fileName);
    }

    @Override
    public ObservableList<ColorScheme> list() {
        return colorSchemes;
    }

    @Override
    public ColorScheme getByKey(final String key) {
        return colorSchemes.stream().filter(e -> e.getUuid().equals(UUID.fromString(key))).findFirst().orElse(null);
    }
}
