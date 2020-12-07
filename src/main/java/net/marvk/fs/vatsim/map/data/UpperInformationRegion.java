package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import net.marvk.fs.vatsim.api.data.VatsimUpperInformationRegion;
import net.marvk.fs.vatsim.map.GeomUtil;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UpperInformationRegion implements Settable<VatsimUpperInformationRegion>, Data {
    private final StringProperty icao =
            new SimpleStringProperty();

    private final StringProperty name =
            new SimpleStringProperty();

    private final ObjectProperty<Rectangle2D> bounds = new SimpleObjectProperty<>();

    private final ReadOnlyListWrapper<Controller> controllers =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, Controller::workingUpperInformationRegionPropertyWritable);

    private final ReadOnlyListWrapper<FlightInformationRegionBoundary> flightInformationRegionBoundaries =
            RelationshipReadOnlyListWrapper.withOtherList(this, FlightInformationRegionBoundary::getUpperInformationRegionsWritable);

    @Override
    public void setFromModel(final VatsimUpperInformationRegion model) {
        Objects.requireNonNull(model);

        icao.set(model.getIcao());
        name.set(model.getName());

        flightInformationRegionBoundaries.addListener((observable, oldValue, newValue) -> {
            final List<Rectangle2D> rectangles = flightInformationRegionBoundaries
                    .stream()
                    .map(FlightInformationRegionBoundary::getPolygon)
                    .map(Polygon::boundary)
                    .collect(Collectors.toList());

            bounds.set(GeomUtil.boundingRect(rectangles));
        });
    }

    public String getIcao() {
        return icao.get();
    }

    public ReadOnlyStringProperty icaoProperty() {
        return icao;
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public Rectangle2D getBounds() {
        return bounds.get();
    }

    public ReadOnlyObjectProperty<Rectangle2D> boundsProperty() {
        return bounds;
    }

    ObservableList<Controller> getControllersWritable() {
        return controllers;
    }

    public ObservableList<Controller> getControllers() {
        return controllers.getReadOnlyProperty();
    }

    ObservableList<FlightInformationRegionBoundary> getFlightInformationRegionBoundariesWritable() {
        return flightInformationRegionBoundaries;
    }

    public ObservableList<FlightInformationRegionBoundary> getFlightInformationRegionBoundaries() {
        return flightInformationRegionBoundaries.getReadOnlyProperty();
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
