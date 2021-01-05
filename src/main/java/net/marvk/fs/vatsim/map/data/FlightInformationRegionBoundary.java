package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.data.VatsimAirspace;

import java.util.StringJoiner;

@Log4j2
public class FlightInformationRegionBoundary implements Settable<VatsimAirspace>, Data {
    private final StringProperty icao = new SimpleStringProperty();
    private final BooleanProperty extension = new SimpleBooleanProperty();
    private final BooleanProperty oceanic = new SimpleBooleanProperty();
    private final ObjectProperty<Polygon> polygon = new SimpleObjectProperty<>();

    private final ReadOnlyListWrapper<Airport> airports =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, Airport::flightInformationRegionBoundaryPropertyWritable);

    private final ReadOnlyObjectWrapper<Country> country =
            RelationshipReadOnlyObjectWrapper.withOtherList(this, Country::flightInformationRegionBoundariesWritable);

    private final ReadOnlyListWrapper<FlightInformationRegion> flightInformationRegions =
            RelationshipReadOnlyListWrapper.withOtherList(this, FlightInformationRegion::boundariesWritable);

    private final ReadOnlyListWrapper<UpperInformationRegion> upperInformationRegions =
            RelationshipReadOnlyListWrapper.withOtherList(this, UpperInformationRegion::getFlightInformationRegionBoundariesWritable);

    private final RelationshipReadOnlyListWrapper<Controller> controllers =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, Controller::workingFlightInformationRegionBoundaryPropertyWritable);

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
            c.getAddedSubList().forEach(controllers::regularAdd);
            c.getRemoved().forEach(controllers::regularRemove);
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

    SimpleListProperty<Airport> airportsWritable() {
        return airports;
    }

    public ReadOnlyListProperty<Airport> getAirports() {
        return airports.getReadOnlyProperty();
    }

    public SimpleListProperty<Controller> getControllersWritable() {
        return controllers;
    }

    public ReadOnlyListProperty<Controller> getControllers() {
        return controllers.getReadOnlyProperty();
    }

    public Country getCountry() {
        return country.get();
    }

    ObjectProperty<Country> countryPropertyWritable() {
        return country;
    }

    public ReadOnlyObjectProperty<Country> countryProperty() {
        return country.getReadOnlyProperty();
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
        return controllers.stream().anyMatch(e -> e.getWorkingUpperInformationRegion() == null);
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
