package net.marvk.fs.vatsim.map.data;

import com.google.gson.JsonParseException;
import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Log4j2
public abstract class FileRepository<E extends UniquelyIdentifiable> implements Repository<E> {
    protected final Path path;
    protected final Adapter<E> adapter;
    private final ObservableList<E> items = FXCollections.observableArrayList();
    private final SortedList<E> sortedList = new SortedList<>(items);
    private final HashMap<UUID, E> uuidMap = new HashMap<>();

    @Inject
    public FileRepository(final Path path, final Adapter<E> adapter) {
        this.adapter = adapter;

        this.path = tryCreateElementDirectory(path);
        if (canSaveToDisk()) {
            tryLoadingExistingElements();
        }

        final Comparator<E> comparator = comparator();
        if (comparator != null) {
            this.sortedList.setComparator(comparator);
        }
    }

    protected Comparator<E> comparator() {
        return null;
    }

    protected abstract String elementDescriptor(final E e);

    protected abstract String singular();

    protected abstract String plural();

    protected abstract String directoryName();

    protected void tryLoadingExistingElements() {
        try (Stream<Path> paths = Files.list(path)) {
            log.info("Loading %s from %s".formatted(singular(), path));
            paths.map(this::read)
                 .filter(Objects::nonNull)
                 .map(this::deserialize)
                 .filter(Objects::nonNull)
                 .forEach(this::createNoWrite);
        } catch (final IOException e) {
            log.error("Failed to read %s directory".formatted(singular()), e);
        }
    }

    private String read(final Path path) {
        try {
            log.info("Loading %s file %s".formatted(singular(), path));
            return Files.readString(path);
        } catch (final IOException e) {
            log.error("Failed to load %s file".formatted(singular()), e);
            return null;
        }
    }

    private E deserialize(final String s) {
        try {
            log.debug("Deserializing %s \n%s".formatted(singular(), s));
            return adapter.deserialize(s);
        } catch (final JsonParseException e) {
            log.error("Failed to deserialize %s".formatted(singular()), e);
            return null;
        }
    }

    public boolean canSaveToDisk() {
        return path != null;
    }

    protected Path tryCreateElementDirectory(final Path path) {
        try {
            log.info(("Creating %s directory %s").formatted(singular(), path));
            return Files.createDirectories(path.resolve(directoryName()));
        } catch (final IOException e) {
            log.error("Failed to create %s directory, %s will not be saved".formatted(singular(), plural()), e);
            return null;
        }
    }

    @Override
    public ObservableList<E> list() {
        return sortedList;
    }

    @Override
    public E getByKey(final String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void create(final E e) throws RepositoryException {
        createNoWrite(e);
        try {
            writeFile(e);
        } catch (final IOException ex) {
            throw new RepositoryException(ex);
        }
    }

    private void createNoWrite(final E e) {
        log.debug(("Creating %s %s").formatted(singular(), elementDescriptor(e)));
        items.add(e);
        uuidMap.put(e.getUuid(), e);
    }

    @Override
    public void update(final E e) throws RepositoryException {
        log.debug(("Updating %s %s").formatted(singular(), elementDescriptor(e)));
        delete(e);
        create(e);
    }

    @Override
    public void delete(final E e) throws RepositoryException {
        log.debug(("Deleting %s %s").formatted(singular(), elementDescriptor(e)));

        final E toRemove = uuidMap.get(e.getUuid());
        uuidMap.remove(e.getUuid());
        items.remove(toRemove);
        items.remove(e);

        try {
            deleteFile(e);
        } catch (final IOException ex) {
            throw new RepositoryException(ex);
        }
    }

    private void writeFile(final E e) throws IOException {
        if (canSaveToDisk()) {
            log.debug(("Writing %s file %s").formatted(singular(), elementDescriptor(e)));
            Files.writeString(path(e), adapter.serialize(e), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    private void deleteFile(final E e) throws IOException {
        if (canSaveToDisk()) {
            log.debug(("Deleting %s file %s").formatted(singular(), elementDescriptor(e)));
            Files.delete(path(e.getUuid()));
        }
    }

    private Path path(final E e) {
        return path(e.getUuid());
    }

    private Path path(final UUID uuid) {
        return path.resolve("%s.json".formatted(uuid));
    }
}
