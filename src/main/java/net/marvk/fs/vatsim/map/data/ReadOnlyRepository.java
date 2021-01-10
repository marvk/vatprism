package net.marvk.fs.vatsim.map.data;

import javafx.collections.ObservableList;

import java.util.stream.Stream;

public interface ReadOnlyRepository<ViewModel> {
    ObservableList<ViewModel> list();

    default Stream<ViewModel> stream() {
        return list().stream();
    }

    ViewModel getByKey(String key);
}
