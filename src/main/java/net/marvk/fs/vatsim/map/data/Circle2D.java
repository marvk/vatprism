package net.marvk.fs.vatsim.map.data;

import javafx.geometry.Point2D;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Circle2D {
    private final Point2D center;
    private final double radius;

    public Circle2D(final double xCenter, final double yCenter, final double radius) {
        this.center = new Point2D(xCenter, yCenter);
        this.radius = radius;
    }

    public double getCenterX() {
        return center.getX();
    }

    public double getCenterY() {
        return center.getY();
    }
}
