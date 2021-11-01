package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;

import java.util.List;

public class FeatureSet {
    private final ImmutableStringProperty name;
    private final ImmutableListProperty<PointFeature> points;
    private final ImmutableListProperty<LineStringFeature> lineStrings;

    public FeatureSet(final String name, final List<PointFeature> points, final List<LineStringFeature> lineStrings) {
        this.name = new ImmutableStringProperty(name);
        this.points = new ImmutableListProperty<>(List.copyOf(points));
        this.lineStrings = new ImmutableListProperty<>(List.copyOf(lineStrings));
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public ObservableList<PointFeature> getPoints() {
        return points.get();
    }

    public ObservableList<LineStringFeature> getLineStrings() {
        return lineStrings.get();
    }
}
