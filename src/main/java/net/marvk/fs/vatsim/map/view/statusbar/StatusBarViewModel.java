package net.marvk.fs.vatsim.map.view.statusbar;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.StatusbarScope;

public class StatusBarViewModel implements ViewModel {
    private final ObjectProperty<Point2D> mouseWorldPosition = new SimpleObjectProperty<>();
    private final ObservableList<FlightInformationRegionBoundary> highlightedFirs = FXCollections.observableArrayList();

    @InjectScope
    private StatusbarScope statusbarScope;

    public void initialize() {
        mouseWorldPosition.bind(statusbarScope.mouseWorldPositionProperty());

        Bindings.bindContent(highlightedFirs, statusbarScope.highlightedFirs());
    }

    public ObjectProperty<Point2D> mouseWorldPositionProperty() {
        return mouseWorldPosition;
    }

    public ObservableList<FlightInformationRegionBoundary> getHighlightedFirs() {
        return highlightedFirs;
    }
}
