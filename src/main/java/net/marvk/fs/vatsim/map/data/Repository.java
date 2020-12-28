package net.marvk.fs.vatsim.map.data;

import javafx.collections.ObservableList;

import java.util.stream.Stream;

public interface Repository<ViewModel> {
    void reload() throws RepositoryException;

    void reloadAsync(final Runnable onSucceed) throws RepositoryException;

    default void reloadAsync() throws RepositoryException {
        reloadAsync(null);
    }

    ObservableList<ViewModel> list();

    default Stream<ViewModel> stream() {
        return list().stream();
    }

    ViewModel getByKey(final String key);
}
