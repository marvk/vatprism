package net.marvk.fs.vatsim.map.data;

import javafx.geometry.Point2D;
import lombok.Value;

@Value
public class DistanceMeasure {
    Point2D from;
    Point2D to;
    boolean released;
}
