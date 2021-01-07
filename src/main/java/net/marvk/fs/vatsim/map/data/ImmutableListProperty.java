package net.marvk.fs.vatsim.map.data;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.List;

public class ImmutableListProperty<E> extends ReadOnlyListProperty<E> {
    private final ObservableList<E> value;
    private ReadOnlyIntegerProperty size;
    private ImmutableBooleanProperty empty;

    public ImmutableListProperty(final List<E> value) {
        this.value = FXCollections.unmodifiableObservableList(FXCollections.observableList(value));
    }

    @Override
    public ReadOnlyIntegerProperty sizeProperty() {
        if (size == null) {
            size = new ImmutableIntegerProperty(value.size());
        }

        return size;
    }

    @Override
    public ReadOnlyBooleanProperty emptyProperty() {
        if (empty == null) {
            empty = new ImmutableBooleanProperty(value.isEmpty());
        }

        return empty;
    }

    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public ObservableList<E> get() {
        return value;
    }

    @Override
    public void addListener(final ChangeListener<? super ObservableList<E>> listener) {
    }

    @Override
    public void removeListener(final ChangeListener<? super ObservableList<E>> listener) {
    }

    @Override
    public void addListener(final ListChangeListener<? super E> listener) {
    }

    @Override
    public void removeListener(final ListChangeListener<? super E> listener) {
    }

    @Override
    public void addListener(final InvalidationListener listener) {
    }

    @Override
    public void removeListener(final InvalidationListener listener) {
    }
}
