package net.marvk.fs.vatsim.map.view.map;

import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.Pilot;

import java.util.List;

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

        throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + n);
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
