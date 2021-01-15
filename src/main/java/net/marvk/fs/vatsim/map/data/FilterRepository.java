package net.marvk.fs.vatsim.map.data;

import com.google.gson.JsonParseException;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Log4j2
public class FilterRepository implements Repository<Filter> {
    private final Path path;
    private final Serializer<Filter> serializer;
    private final ObservableList<Filter> items = FXCollections.observableArrayList();
    private final HashMap<UUID, Filter> uuidMap = new HashMap<>();

    @Inject
    public FilterRepository(@Named("userConfigDir") final Path path, @Named("filterSerializer") final Serializer<Filter> serializer) {
        this.path = tryCreateFilterDirectory(path);
        this.serializer = serializer;

        loadExistingFilters();
    }

    private void loadExistingFilters() {
        if (canSaveToDisk()) {
            try (Stream<Path> paths = Files.list(path)) {
                paths.map(this::read)
                     .filter(Objects::nonNull)
                     .map(this::deserialize)
                     .filter(Objects::nonNull)
                     .forEach(this::createNoWrite);
            } catch (final IOException e) {
                log.error("Failed to read directory", e);
            }
        }
    }

    private Filter deserialize(final String s) {
        try {
            return serializer.deserialize(s);
        } catch (final JsonParseException e) {
            log.error("Failed to parse filter", e);
            return null;
        }
    }

    private String read(final Path path) {
        try {
            return Files.readString(path);
        } catch (final IOException e) {
            log.error("Failed to load file", e);
            return null;
        }
    }

    public boolean canSaveToDisk() {
        return path != null;
    }

    private Path tryCreateFilterDirectory(final Path path) {
        try {
            return Files.createDirectories(path.resolve("filters"));
        } catch (final IOException e) {
            log.error("Unable to create filter directory, filters will not be saved", e);
        }
        return null;
    }

    @Override
    public ObservableList<Filter> list() {
        return items;
    }

    @Override
    public Filter getByKey(final String key) {
        throw new UnsupportedOperationException();
    }

    private void saveToDisk() {

    }

    @Override
    public void create(final Filter filter) throws RepositoryException {
        createNoWrite(filter);
        try {
            writeFile(filter);
        } catch (final IOException e) {
            throw new RepositoryException(e);
        }
    }

    private void createNoWrite(final Filter filter) {
        items.add(filter);
        uuidMap.put(filter.getUuid(), filter);
    }

    @Override
    public void update(final Filter filter) throws RepositoryException {
        delete(filter);
        create(filter);
    }

    @Override
    public void delete(final Filter filter) throws RepositoryException {
        final UUID uuid = filter.getUuid();

        final Filter toRemove = uuidMap.get(uuid);
        uuidMap.remove(uuid);
        items.remove(toRemove);
        items.remove(filter);

        try {
            deleteFile(uuid);
        } catch (final IOException e) {
            throw new RepositoryException(e);
        }
    }

    private void writeFile(final Filter filter) throws IOException {
        if (canSaveToDisk()) {
            Files.writeString(path(filter), serializer.serialize(filter), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private void deleteFile(final Filter filter) throws IOException {
        deleteFile(filter.getUuid());
    }

    private void deleteFile(final UUID uuid) throws IOException {
        if (canSaveToDisk()) {
            Files.delete(path(uuid));
        }
    }

    private Path path(final Filter filter) {
        return path(filter.getUuid());
    }

    private Path path(final UUID uuid) {
        return path.resolve("%s.json".formatted(uuid));
    }

}
