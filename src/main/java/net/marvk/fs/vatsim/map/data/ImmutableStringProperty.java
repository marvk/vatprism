package net.marvk.fs.vatsim.map.data;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;

public class ImmutableStringProperty extends ReadOnlyStringProperty {
    private final String value;

    public ImmutableStringProperty(final String value) {
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
    public String get() {
        return value;
    }

    @Override
    public void addListener(final ChangeListener<? super String> listener) {
    }

    @Override
    public void removeListener(final ChangeListener<? super String> listener) {
    }

    @Override
    public void addListener(final InvalidationListener listener) {
    }

    @Override
    public void removeListener(final InvalidationListener listener) {
    }
}
