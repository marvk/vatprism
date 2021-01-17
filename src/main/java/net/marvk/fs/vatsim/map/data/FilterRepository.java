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
    private static final String FILTERS_DIRECTORY_NAME = "Filters";

    private final Path path;
    private final Adapter<Filter> adapter;
    private final ObservableList<Filter> items = FXCollections.observableArrayList();
    private final HashMap<UUID, Filter> uuidMap = new HashMap<>();

    @Inject
    public FilterRepository(@Named("userConfigDir") final Path path, @Named("filterSerializer") final Adapter<Filter> adapter) {
        this.adapter = adapter;

        this.path = tryCreateFilterDirectory(path);
        if (canSaveToDisk()) {
            tryLoadingExistingFilters();
        }
    }

    private void tryLoadingExistingFilters() {
        try (Stream<Path> paths = Files.list(path)) {
            log.info("Loading filters from %s".formatted(path));
            paths.map(this::read)
                 .filter(Objects::nonNull)
                 .map(this::deserialize)
                 .filter(Objects::nonNull)
                 .forEach(this::createNoWrite);
        } catch (final IOException e) {
            log.error("Failed to read directory", e);
        }
    }

    private String read(final Path path) {
        try {
            log.info("Loading filter file %s".formatted(path));
            return Files.readString(path);
        } catch (final IOException e) {
            log.error("Failed to load filter file", e);
            return null;
        }
    }

    private Filter deserialize(final String s) {
        try {
            log.debug("Deserializing filter \n%s".formatted(s));
            return adapter.deserialize(s);
        } catch (final JsonParseException e) {
            log.error("Failed to deserialize filter", e);
            return null;
        }
    }

    public boolean canSaveToDisk() {
        return path != null;
    }

    private Path tryCreateFilterDirectory(final Path path) {
        try {
            log.info(("Attempting to create filter directory %s").formatted(path));
            return Files.createDirectories(path.resolve(FILTERS_DIRECTORY_NAME));
        } catch (final IOException e) {
            log.error("Unable to create filter directory, filters will not be saved", e);
            return null;
        }
    }

    @Override
    public ObservableList<Filter> list() {
        return items;
    }

    @Override
    public Filter getByKey(final String key) {
        throw new UnsupportedOperationException();
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
        log.debug(("Creating filter %s").formatted(filterLogName(filter)));
        items.add(filter);
        uuidMap.put(filter.getUuid(), filter);
    }

    @Override
    public void update(final Filter filter) throws RepositoryException {
        log.debug(("Updating filter %s").formatted(filterLogName(filter)));
        delete(filter);
        create(filter);
    }

    @Override
    public void delete(final Filter filter) throws RepositoryException {
        log.debug(("Deleting filter filter %s").formatted(filterLogName(filter)));

        final Filter toRemove = uuidMap.get(filter.getUuid());
        uuidMap.remove(filter.getUuid());
        items.remove(toRemove);
        items.remove(filter);

        try {
            deleteFile(filter);
        } catch (final IOException e) {
            throw new RepositoryException(e);
        }
    }

    private void writeFile(final Filter filter) throws IOException {
        if (canSaveToDisk()) {
            log.debug(("Attempting to write filter file %s").formatted(filterLogName(filter)));
            Files.writeString(path(filter), adapter.serialize(filter), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private void deleteFile(final Filter filter) throws IOException {
        if (canSaveToDisk()) {
            log.debug(("Attempting to deleting filter file %s").formatted(filterLogName(filter)));
            Files.delete(path(filter.getUuid()));
        }
    }

    private Path path(final Filter filter) {
        return path(filter.getUuid());
    }

    private Path path(final UUID uuid) {
        return path.resolve("%s.json".formatted(uuid));
    }

    private static String filterLogName(final Filter filter) {
        return "%s (%s)".formatted(filter.getUuid(), filter.getName());
    }
}
