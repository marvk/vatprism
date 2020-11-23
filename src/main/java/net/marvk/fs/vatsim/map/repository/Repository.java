package net.marvk.fs.vatsim.map.repository;

import javafx.collections.ObservableList;

public interface Repository<ViewModel> {
    ObservableList<ViewModel> list();

    void reload() throws RepositoryException;

    ViewModel getByKey(final String key);
}
