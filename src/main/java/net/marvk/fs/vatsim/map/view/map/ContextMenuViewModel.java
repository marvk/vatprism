package net.marvk.fs.vatsim.map.view.map;

import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.map.data.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ContextMenuViewModel {
    private final ContextMenuItems<Pilot> pilots;
    private final ContextMenuItems<Airport> airports;
    private final ContextMenuItems<FlightInformationRegionBoundary> firbs;
    private final ContextMenuItems<UpperInformationRegion> uirs;

    private final List<ContextMenuItems<? extends Data>> contextMenuItems;

    public ContextMenuViewModel(final ResourceBundle resourceBundle) {
        this(
                new ContextMenuItems<>(resourceBundle.getString("common.pilots")),
                new ContextMenuItems<>(resourceBundle.getString("common.airports")),
                new ContextMenuItems<>(resourceBundle.getString("common.firs")),
                new ContextMenuItems<>(resourceBundle.getString("common.uirs"))
        );
    }

    public ContextMenuViewModel(final ContextMenuViewModel contextMenuViewModel) {
        this(
                new ContextMenuItems<>(contextMenuViewModel.pilots),
                new ContextMenuItems<>(contextMenuViewModel.airports),
                new ContextMenuItems<>(contextMenuViewModel.firbs),
                new ContextMenuItems<>(contextMenuViewModel.uirs)
        );
    }

    private ContextMenuViewModel(
            final ContextMenuItems<Pilot> pilots,
            final ContextMenuItems<Airport> airports,
            final ContextMenuItems<FlightInformationRegionBoundary> firbs,
            final ContextMenuItems<UpperInformationRegion> uirs
    ) {
        this.pilots = pilots;
        this.airports = airports;
        this.firbs = firbs;
        this.uirs = uirs;
        this.contextMenuItems = List.of(
                pilots,
                airports,
                firbs,
                uirs
        );
    }

    public Optional<? extends Data> closest(final Point2D worldPosition) {
        final Optional<Airport> airport = airports
                .getItems()
                .stream()
                .findFirst();

        if (airport.isPresent()) {
            return airport;
        }

        final Optional<Pilot> pilot = pilots
                .getItems()
                .stream()
                .findFirst();

        if (pilot.isPresent()) {
            return pilot;
        }

        final Optional<FlightInformationRegionBoundary> firb = firbs
                .getItems()
                .stream()
                .min(firbComparator(worldPosition));

        if (firb.isPresent()) {
            return firb;
        }

        return uirs
                .getItems()
                .stream()
                .min(uirComparator(worldPosition));
    }

    private static Comparator<FlightInformationRegionBoundary> firbComparator(final Point2D worldPosition) {
        return Comparator
                .<FlightInformationRegionBoundary>comparingDouble(e -> distanceToPolygon(e, worldPosition))
                .thenComparing(Comparator.comparing(FlightInformationRegionBoundary::hasFirControllers).reversed())
                .thenComparing(Comparator.comparing(FlightInformationRegionBoundary::hasUirControllers).reversed())
                .thenComparingDouble(e -> distanceToLabel(e, worldPosition));
    }

    private static Comparator<UpperInformationRegion> uirComparator(final Point2D worldPosition) {
        final Comparator<FlightInformationRegionBoundary> firbComparator = firbComparator(worldPosition);
        return Comparator.comparing(e -> closestFirbOrNull(e, firbComparator), firbComparator);
    }

    private static FlightInformationRegionBoundary closestFirbOrNull(final UpperInformationRegion e, final Comparator<FlightInformationRegionBoundary> firbComparator) {
        return e.getFlightInformationRegionBoundaries().stream().min(firbComparator).orElse(null);
    }

    private static double distanceToLabel(final FlightInformationRegionBoundary flightInformationRegionBoundary, final Point2D point) {
        return dist(point, flightInformationRegionBoundary.getPolygon().getExteriorRing().getPolyLabel());
    }

    private static double dist(final Point2D p1, final Point2D p2) {
        return Math.min(
                Math.min(
                        p2.distance(p1.add(360, 0)),
                        p2.distance(p1.subtract(360, 0))
                ),
                p2.distance(p1)
        );
    }

    private static double distanceToPolygon(final FlightInformationRegionBoundary flightInformationRegionBoundary, final Point2D point) {
        return flightInformationRegionBoundary
                .getPolygon()
                .distance(point);
    }

    public ContextMenuItems<FlightInformationRegionBoundary> getFirbs() {
        return firbs;
    }

    public ContextMenuItems<Airport> getAirports() {
        return airports;
    }

    public ContextMenuItems<Pilot> getPilots() {
        return pilots;
    }

    public ContextMenuItems<UpperInformationRegion> getUirs() {
        return uirs;
    }

    public List<ContextMenuItems<? extends Data>> getContextMenuItems() {
        return contextMenuItems;
    }
}
