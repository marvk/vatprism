package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.data.VatsimAirspace;

import java.util.ArrayList;
import java.util.StringJoiner;

@Log4j2
public class FlightInformationRegionBoundary implements Settable<VatsimAirspace>, Data {
    private final StringProperty icao = new SimpleStringProperty();
    private final BooleanProperty extension = new SimpleBooleanProperty();
    private final BooleanProperty oceanic = new SimpleBooleanProperty();
    private final ObjectProperty<Polygon> polygon = new SimpleObjectProperty<>();

    private final ReadOnlyListWrapper<Airport> airports =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, Airport::flightInformationRegionBoundaryPropertyWritable);

    private final ReadOnlyListWrapper<FlightInformationRegion> flightInformationRegions =
            RelationshipReadOnlyListWrapper.withOtherList(this, FlightInformationRegion::boundariesWritable);

    private final ReadOnlyListWrapper<UpperInformationRegion> upperInformationRegions =
            RelationshipReadOnlyListWrapper.withOtherList(this, UpperInformationRegion::getFlightInformationRegionBoundariesWritable);

    private final ReadOnlyListWrapper<Controller> controllers = new ReadOnlyListWrapper<>(FXCollections.observableList(new ArrayList<>(0)));

    @Override
    public void setFromModel(final VatsimAirspace airspace) {
        icao.set(airspace.getGeneral().getIcao());
        extension.set(airspace.getGeneral().getExtension());
        oceanic.set(airspace.getGeneral().getOceanic());
        polygon.set(new Polygon(airspace.getAirspacePoints(), polygonName()));

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

    private String polygonName() {
        final StringJoiner sj = new StringJoiner("_");
        sj.add(icao.get());
        if (oceanic.get()) {
            sj.add("Oceanic");
        }
        if (extension.get()) {
            sj.add("Extension");
        }
        return sj.toString();
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
        log.debug("Merging %s with it's extension".formatted(getIcao()));
        polygon.set(Polygon.merge(getPolygon(), extension.getPolygon()));
    }

    SimpleListProperty<FlightInformationRegion> getFlightInformationRegionsWritable() {
        return flightInformationRegions;
    }

    public ReadOnlyListProperty<FlightInformationRegion> getFlightInformationRegions() {
        return flightInformationRegions.getReadOnlyProperty();
    }

    SimpleListProperty<UpperInformationRegion> getUpperInformationRegionsWritable() {
        return upperInformationRegions;
    }

    public ReadOnlyListProperty<UpperInformationRegion> getUpperInformationRegions() {
        return upperInformationRegions.getReadOnlyProperty();
    }

    SimpleListProperty<Airport> getAirportsWritable() {
        return airports;
    }

    public ReadOnlyListProperty<Airport> getAirports() {
        return airports.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<Controller> getControllers() {
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
                "icao=" + icao +
                ", extension=" + extension +
                ", oceanic=" + oceanic +
                ", polygon=" + polygon +
                '}';
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
