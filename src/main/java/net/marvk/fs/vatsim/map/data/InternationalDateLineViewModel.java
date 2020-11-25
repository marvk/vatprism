package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.api.data.Line;
import net.marvk.fs.vatsim.map.GeomUtil;

import java.util.List;
import java.util.stream.Collectors;

public class InternationalDateLineViewModel extends SimpleDataViewModel<Line, InternationalDateLineViewModel> {
    private final ObservableList<Point2D> points = FXCollections.observableArrayList();

    @Inject
    public InternationalDateLineViewModel() {
        setupBindings();
    }

    private void setupBindings() {
        modelProperty().addListener((observable, oldValue, newValue) -> {
            final List<Point2D> collect = newValue
                    .getPoints()
                    .stream()
                    .map(e -> GeomUtil.parsePoint(e.getX(), e.getY()))
                    .collect(Collectors.toList());

            points.setAll(collect);
        });
    }

    public ObservableList<Point2D> points() {
        return points;
    }
}
