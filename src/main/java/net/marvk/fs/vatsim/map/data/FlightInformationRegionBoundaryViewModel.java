package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.api.data.VatsimAirspace;
import net.marvk.fs.vatsim.map.repository.FlightInformationRegionRepository;

import java.util.List;
import java.util.stream.Collectors;

public class FlightInformationRegionBoundaryViewModel extends SimpleDataViewModel<VatsimAirspace, FlightInformationRegionBoundaryViewModel> implements ViewModel {
    private final ObservableList<Point2D> points = FXCollections.observableArrayList();
    private final FlightInformationRegionRepository flightInformationRegionRepository;

    @Inject
    public FlightInformationRegionBoundaryViewModel(final FlightInformationRegionRepository flightInformationRegionRepository) {
        super();
        this.flightInformationRegionRepository = flightInformationRegionRepository;
        setupBindings();
    }

    private void setupBindings() {
        modelProperty().addListener((observable, oldValue, newValue) -> update(newValue));
    }

    private void update(final VatsimAirspace airspace) {
        updatePoints(airspace);
        updateFirs(airspace);
    }

    private void updateFirs(final VatsimAirspace airspace) {

    }

    private void updatePoints(final VatsimAirspace airspace) {
        if (airspace == null) {
            points.clear();
        } else {
            final List<Point2D> newPoints = airspace
                    .getAirspacePoints()
                    .stream()
                    .map(e -> new Point2D(e.getX(), e.getY()))
                    .collect(Collectors.toList());

            points.setAll(newPoints);
        }
    }

    public StringProperty icaoProperty() {
        return stringProperty("icao", c -> c.getGeneral().getIcao());
    }

    public BooleanProperty oceanicProperty() {
        return booleanProperty("oceanic", c -> c.getGeneral().getOceanic());
    }

    public BooleanProperty extensionProperty() {
        return booleanProperty("extension", c -> c.getGeneral().getExtension());
    }

    public ObjectProperty<Point2D> minPositionProperty() {
        return pointProperty("minPosition", c -> c.getGeneral().getMinPosition());
    }

    public ObjectProperty<Point2D> maxPositionProperty() {
        return pointProperty("maxPosition", c -> c.getGeneral().getMaxPosition());
    }

    public ObjectProperty<Point2D> centerPositionProperty() {
        return pointProperty("centerPosition", c -> c.getGeneral().getCenterPosition());
    }

    public ObservableList<Point2D> points() {
        return points;
    }
}
