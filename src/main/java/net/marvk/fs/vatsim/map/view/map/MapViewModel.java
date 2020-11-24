package net.marvk.fs.vatsim.map.view.map;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.map.data.ClientViewModel;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundaryViewModel;
import net.marvk.fs.vatsim.map.data.RawClientType;
import net.marvk.fs.vatsim.map.repository.ClientRepository;
import net.marvk.fs.vatsim.map.repository.FlightInformationRegionBoundaryRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiLineString;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapViewModel implements ViewModel {
    private final DoubleProperty scale = new SimpleDoubleProperty(1);
    private final ObjectProperty<Point2D> centerLatitude = new SimpleObjectProperty<>(new Point2D(0, 0));

    private final ClientRepository clientRepository;
    private final FlightInformationRegionBoundaryRepository flightInformationRegionViewModel;

    private final List<double[]> worldX;
    private final List<double[]> worldY;

    @Inject
    public MapViewModel(
            final ClientRepository clientRepository,
            final FlightInformationRegionBoundaryRepository flightInformationRegionViewModel,
            @Named("world") final List<MultiLineString> world
    ) {
        this.clientRepository = clientRepository;
        this.flightInformationRegionViewModel = flightInformationRegionViewModel;
        this.worldX = extractCoords(world, Coordinate::getX);
        this.worldY = extractCoords(world, Coordinate::getY);
    }

    private static List<double[]> extractCoords(final List<MultiLineString> multiLineStrings, final Function<Coordinate, Double> mapper) {
        return multiLineStrings.stream().map(e -> asArray(e, mapper)).collect(Collectors.toList());
    }

    private static double[] asArray(final MultiLineString multiLineString, final Function<Coordinate, Double> mapper) {
        return Arrays.stream(multiLineString.getCoordinates()).map(mapper).mapToDouble(e -> e).toArray();
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public ObjectProperty<Point2D> worldCenterProperty() {
        return centerLatitude;
    }

    public ObservableList<ClientViewModel> clients() {
        return clientRepository.list().filtered(e -> e.rawClientTypeProperty().get() == RawClientType.PILOT);
    }

    public ObservableList<FlightInformationRegionBoundaryViewModel> flightInformationRegionBoundaries() {
        throw new UnsupportedOperationException();
    }

    public List<double[]> worldX() {
        return worldX;
    }

    public List<double[]> worldY() {
        return worldY;
    }
}
