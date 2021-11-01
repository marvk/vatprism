package net.marvk.fs.vatsim.map.data;

import net.marvk.fs.vatsim.api.data.Point;

public class PointFeature extends Feature {
    private final ImmutableObjectProperty<Point> coordinate;

    public PointFeature(final String name, final Point point) {
        super(name);

        this.coordinate = new ImmutableObjectProperty<>(point);
    }

    public Point getCoordinate() {
        return coordinate.get();
    }
}
