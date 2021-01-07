package net.marvk.fs.vatsim.map.data;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;

public class ImmutableBooleanProperty extends ReadOnlyBooleanProperty {
    private final boolean value;

    public ImmutableBooleanProperty(final boolean value) {
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
    public boolean get() {
        return value;
    }

    @Override
    public void addListener(final ChangeListener<? super Boolean> listener) {
    }

    @Override
    public void removeListener(final ChangeListener<? super Boolean> listener) {
    }

    @Override
    public void addListener(final InvalidationListener listener) {
    }

    @Override
    public void removeListener(final InvalidationListener listener) {
    }
}
