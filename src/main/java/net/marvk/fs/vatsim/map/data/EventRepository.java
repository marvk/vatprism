package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.Provider;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimEvent;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
public class EventRepository extends ProviderRepository<Event, VatsimEvent> {
    private final AirportRepository airportRepository;

    @Inject
    public EventRepository(final VatsimApi vatsimApi, final Provider<Event> provider, final AirportRepository airportRepository) {
        super(vatsimApi, provider);
        this.airportRepository = airportRepository;
    }

    @Override
    protected void onAdd(final Event toAdd, final VatsimEvent vatsimEvent) {
        final List<Airport> airports = vatsimEvent
                .getAirports()
                .stream()
                .map(VatsimEvent.Airport::getIcao)
                .map(this::getAirport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        toAdd.getAirportsWritable().addAll(airports);

        final List<EventRoute> eventRoutes = vatsimEvent
                .getRoutes()
                .stream()
                .map(this::mapToRoute)
                .collect(Collectors.toList());

        toAdd.getRoutesWritable().addAll(eventRoutes);
    }

    private EventRoute mapToRoute(final VatsimEvent.Route route) {
        final EventRoute eventRoute = new EventRoute();
        eventRoute.setFromModel(route);
        eventRoute.getArrivalWritable().set(getAirport(route.getArrival()));
        eventRoute.getDepartureWritable().set(getAirport(route.getDeparture()));
        return eventRoute;
    }

    private Airport getAirport(final String icao) {
        if (icao == null || icao.isBlank() || "none".equalsIgnoreCase(icao)) {
            return null;
        }

        final List<Airport> airports = airportRepository.getByIcao(icao);
        if (airports.isEmpty()) {
            log.info("Unknown airport \"%s\"".formatted(icao));
            return null;
        }

        if (airports.size() > 1) {
            log.warn("Multiple airports for " + icao + ": " + airports);
        }

        return airports.get(0);
    }

    @Override
    protected void onUpdate(final Event toUpdate, final VatsimEvent vatsimEvent) {
        onRemove(toUpdate);
        onAdd(toUpdate, vatsimEvent);
    }

    @Override
    protected void onRemove(final Event toRemove) {
        toRemove.getRoutes().forEach(e -> {
            e.getDepartureWritable().set(null);
            e.getArrivalWritable().set(null);
        });
        toRemove.getAirportsWritable().clear();
    }

    @Override
    protected String keyFromModel(final VatsimEvent vatsimEvent) {
        return Integer.toString(vatsimEvent.getId());
    }

    @Override
    protected String keyFromViewModel(final Event event) {
        return Integer.toString(event.getId());
    }

    @Override
    protected Collection<VatsimEvent> extractModels(final VatsimApi api) throws VatsimApiException {
        return api.events().getEvents();
    }
}
