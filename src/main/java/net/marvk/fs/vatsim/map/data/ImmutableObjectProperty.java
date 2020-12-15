package net.marvk.fs.vatsim.map.data;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;

public class ImmutableObjectProperty<T> extends ReadOnlyObjectProperty<T> {
    private final T value;

    public ImmutableObjectProperty(final T value) {
        this.value = value;
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
    public T get() {
        return value;
    }

    @Override
    public void addListener(final ChangeListener<? super T> listener) {
    }

    @Override
    public void removeListener(final ChangeListener<? super T> listener) {
    }

    @Override
    public void addListener(final InvalidationListener listener) {
    }

    @Override
    public void removeListener(final InvalidationListener listener) {
    }
}
