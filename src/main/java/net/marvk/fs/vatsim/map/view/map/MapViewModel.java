package net.marvk.fs.vatsim.map.view.map;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.repository.AirportRepository;
import net.marvk.fs.vatsim.map.repository.ClientRepository;
import net.marvk.fs.vatsim.map.repository.FlightInformationRegionBoundaryRepository;
import net.marvk.fs.vatsim.map.repository.InternationalDateLineRepository;

import java.util.List;

public class MapViewModel implements ViewModel {
    private final DoubleProperty scale = new SimpleDoubleProperty(1);
    private final ReadOnlyObjectWrapper<Point2D> worldCenter = new ReadOnlyObjectWrapper<>(new Point2D(0, 0));

    private final ClientRepository clientRepository;
    private final AirportRepository airportRepository;
    private final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository;

    private final InternationalDateLineRepository internationalDateLineRepository;
    private final List<Polygon> world;

    @Inject
    public MapViewModel(
            final ClientRepository clientRepository,
            final AirportRepository airportRepository,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
            final InternationalDateLineRepository internationalDateLineRepository,
            @Named("world") final List<Polygon> world
    ) {
        this.clientRepository = clientRepository;
        this.airportRepository = airportRepository;
        this.flightInformationRegionBoundaryRepository = flightInformationRegionBoundaryRepository;
        this.internationalDateLineRepository = internationalDateLineRepository;

        this.world = world;
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public ReadOnlyObjectProperty<Point2D> worldCenterProperty() {
        return worldCenter.getReadOnlyProperty();
    }

    public void setWorldCenter(final Point2D worldCenter) {
        final double x = ((worldCenter.getX() + 540) % 360) - 180;
        this.worldCenter.set(new Point2D(x, worldCenter.getY()));
    }

    public Point2D getWorldCenter() {
        return worldCenter.get();
    }

    public ObservableList<ClientViewModel> clients() {
        return clientRepository.list().filtered(e -> e.rawClientTypeProperty().get() == RawClientType.PILOT);
    }

    public ObservableList<FlightInformationRegionBoundaryViewModel> flightInformationRegionBoundaries() {
        return flightInformationRegionBoundaryRepository.list();
    }

    public InternationalDateLineViewModel internationalDateLine() {
        return internationalDateLineRepository.list().get(0);
    }

    public ObservableList<AirportViewModel> airports() {
        return airportRepository.list();
    }

    public List<Polygon> world() {
        return world;
    }
}
