package net.marvk.fs.vatsim.map.view;

import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;

public class StatusbarScope implements Scope {
    private final ObjectProperty<Point2D> mouseWorldPosition = new SimpleObjectProperty<>();
    private final ObjectProperty<Point2D> mouseViewPosition = new SimpleObjectProperty<>();

    private final ObservableList<FlightInformationRegionBoundary> highlightedFirs = FXCollections.observableArrayList();

    public ObservableList<FlightInformationRegionBoundary> highlightedFirs() {
        return highlightedFirs;
    }

    public Point2D getMouseWorldPosition() {
        return mouseWorldPosition.get();
    }

    public ObjectProperty<Point2D> mouseWorldPositionProperty() {
        return mouseWorldPosition;
    }

    public void setMouseWorldPosition(final Point2D mouseWorldPosition) {
        this.mouseWorldPosition.set(mouseWorldPosition);
    }

    public Point2D getMouseViewPosition() {
        return mouseViewPosition.get();
    }

    public ObjectProperty<Point2D> mouseViewPositionProperty() {
        return mouseViewPosition;
    }

    public void setMouseViewPosition(final Point2D mouseViewPosition) {
        this.mouseViewPosition.set(mouseViewPosition);
    }
}
