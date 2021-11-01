package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyStringProperty;

public abstract class Feature {
    private final ImmutableStringProperty name;

    public Feature(final String name) {
        this.name = new ImmutableStringProperty(name);
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }
}
