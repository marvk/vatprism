package net.marvk.fs.vatsim.map.view.map;

import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.map.data.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ContextMenuViewModel {
    private final ContextMenuItems<Pilot> pilots;
    private final ContextMenuItems<Airport> airports;
    private final ContextMenuItems<FlightInformationRegionBoundary> firbs;
    private final ContextMenuItems<UpperInformationRegion> uirs;

    private final List<ContextMenuItems<? extends Data>> contextMenuItems;

    public ContextMenuViewModel() {
        this(
                new ContextMenuItems<>("Pilots"),
                new ContextMenuItems<>("Airports"),
                new ContextMenuItems<>("FIRs"),
                new ContextMenuItems<>("UIRs")
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
        // TODO move filter to settings
        final Optional<Airport> airport = airports
                .getItems()
                .stream()
                .filter(Airport::hasControllers)
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

        return firbs
                .getItems()
                .stream()
                .min(firbComparator(worldPosition));
    }

    private static Comparator<FlightInformationRegionBoundary> firbComparator(final Point2D worldPosition) {
        return Comparator
                .<FlightInformationRegionBoundary>comparingDouble(e -> distanceToPolygon(e, worldPosition))
                .thenComparingDouble(e -> distanceToLabel(e, worldPosition))
                .thenComparing(Comparator.comparing(FlightInformationRegionBoundary::hasFirControllers).reversed())
                .thenComparing(Comparator.comparing(FlightInformationRegionBoundary::hasUirControllers).reversed());
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

    public Data getItem(final int i) {
        int remaining = i;

        if (remaining == 0) {
            return null;
        }

        remaining -= 1;

        if (remaining < pilots.getItems().size()) {
            return pilots.getItems().get(remaining);
        }

        remaining -= pilots.getItems().size();

        if (remaining == 0) {
            return null;
        }

        remaining -= 1;

        if (remaining < airports.getItems().size()) {
            return airports.getItems().get(remaining);
        }

        remaining -= airports.getItems().size();

        if (remaining == 0) {
            return null;
        }

        remaining -= 1;

        if (remaining < firbs.getItems().size()) {
            return firbs.getItems().get(remaining);
        }

        remaining -= firbs.getItems().size();

        if (remaining == 0) {
            return null;
        }

        remaining -= 1;

        if (remaining < uirs.getItems().size()) {
            return uirs.getItems().get(remaining);
        }

        remaining -= uirs.getItems().size();

        return null;
    }

    private int numberOfItems() {
        final var nItems = contextMenuItems
                .stream()
                .map(ContextMenuItems::getItems)
                .mapToInt(List::size)
                .sum();
        final int nContextMenus = contextMenuItems.size();

        return nItems + nContextMenus;
    }
}
