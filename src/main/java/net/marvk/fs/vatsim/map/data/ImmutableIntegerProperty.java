package net.marvk.fs.vatsim.map.data;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ChangeListener;

public class ImmutableIntegerProperty extends ReadOnlyIntegerProperty {
    private final int value;

    public ImmutableIntegerProperty(final int value) {
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
    public int get() {
        return value;
    }

    @Override
    public void addListener(final ChangeListener<? super Number> listener) {
    }

    @Override
    public void removeListener(final ChangeListener<? super Number> listener) {
    }

    @Override
    public void addListener(final InvalidationListener listener) {
    }

    @Override
    public void removeListener(final InvalidationListener listener) {
    }
}
