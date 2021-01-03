package net.marvk.fs.vatsim.map.view.map;

import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.Pilot;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContextMenuViewModel {
    private final ContextMenuItems<Pilot> pilots;
    private final ContextMenuItems<Airport> airports;
    private final ContextMenuItems<FlightInformationRegionBoundary> boundaries;

    private final List<ContextMenuItems<? extends Data>> contextMenuItems;

    public ContextMenuViewModel() {
        this(
                new ContextMenuItems<>("Pilots"),
                new ContextMenuItems<>("Airports"),
                new ContextMenuItems<>("FIRs")
        );
    }

    public ContextMenuViewModel(final ContextMenuViewModel contextMenuViewModel) {
        this(
                new ContextMenuItems<>(contextMenuViewModel.pilots),
                new ContextMenuItems<>(contextMenuViewModel.airports),
                new ContextMenuItems<>(contextMenuViewModel.boundaries)
        );
    }

    private ContextMenuViewModel(
            final ContextMenuItems<Pilot> pilots,
            final ContextMenuItems<Airport> airports,
            final ContextMenuItems<FlightInformationRegionBoundary> boundaries
    ) {
        this.pilots = pilots;
        this.airports = airports;
        this.boundaries = boundaries;
        this.contextMenuItems = List.of(
                pilots,
                airports,
                boundaries
        );
    }

    public Optional<? extends Data> closest(final Point2D worldPosition) {
        // TODO move filter to settings
        final Optional<Airport> airport = airports.getItems().stream().filter(Airport::hasControllers).findFirst();

        if (airport.isPresent()) {
            return airport;
        }

        final Optional<Pilot> pilot = pilots.getItems().stream().findFirst();

        if (pilot.isPresent()) {
            return pilot;
        }

        return extracted(this.boundaries.getItems())
                .stream()
                .min(Comparator.comparing(e -> distance(e, worldPosition)));
    }

    private static double distance(final FlightInformationRegionBoundary flightInformationRegionBoundary, final Point2D point) {
        return flightInformationRegionBoundary
                .getPolygon()
                .distance(point);
    }

    private List<FlightInformationRegionBoundary> extracted(final List<FlightInformationRegionBoundary> boundaries) {
        final List<FlightInformationRegionBoundary> boundariesWithFirControllers =
                boundaries.stream()
                          .filter(FlightInformationRegionBoundary::hasFirControllers)
                          .collect(Collectors.toUnmodifiableList());

        if (!boundariesWithFirControllers.isEmpty()) {
            return boundariesWithFirControllers;
        }

        final List<FlightInformationRegionBoundary> boundariesWithUirControllers =
                boundaries.stream()
                          .filter(FlightInformationRegionBoundary::hasUirControllers)
                          .collect(Collectors.toUnmodifiableList());

        if (!boundariesWithUirControllers.isEmpty()) {
            return boundariesWithUirControllers;
        }

        return boundaries;
    }

    public ContextMenuItems<FlightInformationRegionBoundary> getBoundaries() {
        return boundaries;
    }

    public ContextMenuItems<Airport> getAirports() {
        return airports;
    }

    public ContextMenuItems<Pilot> getPilots() {
        return pilots;
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

        if (remaining < boundaries.getItems().size()) {
            return boundaries.getItems().get(remaining);
        }

        remaining -= boundaries.getItems().size();

        final var n = numberOfItems();

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
