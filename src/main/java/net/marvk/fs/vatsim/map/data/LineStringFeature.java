package net.marvk.fs.vatsim.map.data;

import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.api.data.Point;

import java.util.List;

public class LineStringFeature extends Feature {
    private final ImmutableListProperty<Point> coordinates;

    public LineStringFeature(final String name, final List<Point> coordinates) {
        super(name);

        this.coordinates = new ImmutableListProperty<>(List.copyOf(coordinates));
    }

    public ObservableList<Point> getCoordinates() {
        return coordinates.get();
    }
}
