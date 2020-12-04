package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.marvk.fs.vatsim.api.data.Line;

public class InternationalDateLine implements Settable<Line>, Data {
    private final ObjectProperty<Polygon> polygon = new SimpleObjectProperty<>();

    @Override
    public void setFromModel(final Line points) {
        polygon.set(new Polygon(points.getPoints()));
    }

    public Polygon getPolygon() {
        return polygon.get();
    }

    public ReadOnlyObjectProperty<Polygon> polygonProperty() {
        return polygon;
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
