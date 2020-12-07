package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.api.data.VatsimAirspace;

import java.util.ArrayList;

public class FlightInformationRegionBoundary implements Settable<VatsimAirspace>, Data {
    private final StringProperty icao = new SimpleStringProperty();
    private final BooleanProperty extension = new SimpleBooleanProperty();
    private final BooleanProperty oceanic = new SimpleBooleanProperty();
    private final ObjectProperty<Polygon> polygon = new SimpleObjectProperty<>();

    private final ReadOnlyListWrapper<Airport> airports =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, Airport::flightInformationRegionBoundaryPropertyWritable);

    private final ReadOnlyListWrapper<FlightInformationRegion> flightInformationRegions =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, FlightInformationRegion::boundaryPropertyWritable);

    private final ReadOnlyListWrapper<UpperInformationRegion> upperInformationRegions =
            RelationshipReadOnlyListWrapper.withOtherList(this, UpperInformationRegion::getFlightInformationRegionBoundariesWritable);

    private final ReadOnlyListWrapper<Controller> controllers = new ReadOnlyListWrapper<>(FXCollections.observableList(new ArrayList<>(0)));

    @Override
    public void setFromModel(final VatsimAirspace airspace) {
        icao.set(airspace.getGeneral().getIcao());
        extension.set(airspace.getGeneral().getExtension());
        oceanic.set(airspace.getGeneral().getOceanic());
        polygon.set(new Polygon(airspace.getAirspacePoints()));

        upperInformationRegions.addListener((ListChangeListener<UpperInformationRegion>) c -> {
            while (c.next()) {
                for (final UpperInformationRegion upperInformationRegion : c.getAddedSubList()) {
                    addListener(upperInformationRegion.getControllers());
                }
            }
        });

        flightInformationRegions.addListener((ListChangeListener<FlightInformationRegion>) c -> {
            while (c.next()) {
                for (final FlightInformationRegion flightInformationRegion : c.getAddedSubList()) {
                    addListener(flightInformationRegion.getControllers());
                }
            }
        });
    }

    private void addListener(final ObservableList<Controller> controllers) {
        controllers.addListener(this::controllersChanged);
    }

    private void controllersChanged(final ListChangeListener.Change<? extends Controller> c) {
        while (c.next()) {
            controllers.addAll(c.getAddedSubList());

            for (final Controller controller : c.getRemoved()) {
                controllers.remove(controller);
            }
        }
    }

    public String getIcao() {
        return icao.get();
    }

    public ReadOnlyStringProperty icaoProperty() {
        return icao;
    }

    public boolean isExtension() {
        return extension.get();
    }

    public ReadOnlyBooleanProperty extensionProperty() {
        return extension;
    }

    public boolean isOceanic() {
        return oceanic.get();
    }

    public ReadOnlyBooleanProperty oceanicProperty() {
        return oceanic;
    }

    public Polygon getPolygon() {
        return polygon.get();
    }

    public ReadOnlyObjectProperty<Polygon> polygonProperty() {
        return polygon;
    }

    public void mergeInto(final FlightInformationRegionBoundary extension) {
        polygon.set(Polygon.merge(getPolygon(), extension.getPolygon()));
    }

    ObservableList<FlightInformationRegion> getFlightInformationRegionsWritable() {
        return flightInformationRegions.get();
    }

    public ObservableList<FlightInformationRegion> getFlightInformationRegions() {
        return flightInformationRegions.getReadOnlyProperty();
    }

    ObservableList<UpperInformationRegion> getUpperInformationRegionsWritable() {
        return upperInformationRegions;
    }

    public ObservableList<UpperInformationRegion> getUpperInformationRegions() {
        return upperInformationRegions.getReadOnlyProperty();
    }

    ObservableList<Airport> getAirportsWritable() {
        return airports;
    }

    public ObservableList<Airport> getAirports() {
        return airports.getReadOnlyProperty();
    }

    public ObservableList<Controller> getControllers() {
        return controllers.getReadOnlyProperty();
    }

    public boolean hasUirControllers() {
        for (final UpperInformationRegion uir : getUpperInformationRegions()) {
            if (!uir.getControllers().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasFirControllers() {
        for (final FlightInformationRegion fir : getFlightInformationRegions()) {
            if (!fir.getControllers().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "FlightInformationRegionBoundary{" +
                "icao=" + icao.get() +
                ", extension=" + extension.get() +
                ", oceanic=" + oceanic.get() +
                ", polygon=" + polygon.get().boundary() +
                ", flightInformationRegions=" + flightInformationRegions.get() +
                ", upperInformationRegions=" + upperInformationRegions.get() +
                '}';
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
