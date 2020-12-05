package net.marvk.fs.vatsim.map.data;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Point;
import com.github.davidmoten.rtree2.internal.EntryDefault;
import com.google.inject.Inject;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Point2D;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class ClientRepository extends SimpleRepository<Client, VatsimClient> {
    private final ReadOnlyListWrapper<Pilot> pilots;
    private final ReadOnlyListWrapper<Controller> controllers;
    private final AirportRepository airportRepository;
    private final CallsignParser callsignParser;
    private RTree<Pilot, Point> rTree = RTree.create();

    @Inject
    public ClientRepository(final VatsimApi vatsimApi, final AirportRepository airportRepository, final CallsignParser callsignParser) {
        super(vatsimApi);
        this.airportRepository = airportRepository;
        this.callsignParser = callsignParser;

        // yikes, but it works, sooo...
        pilots = new ReadOnlyListWrapper<Pilot>(new FilteredList(list(), e -> e instanceof Pilot));
        controllers = new ReadOnlyListWrapper<Controller>(new FilteredList(list(), e -> e instanceof Controller));
    }

    @Override
    protected Client newViewModelInstance(final VatsimClient vatsimClient) {
        return switch (vatsimClient.getClientType().toLowerCase(Locale.ROOT)) {
            case "atc" -> new Controller();
            case "pilot" -> new Pilot();
            default -> null;
        };
    }

    @Override
    protected String keyFromModel(final VatsimClient vatsimClient) {
        return vatsimClient.getCid() + "_" + vatsimClient.getCallsign();
    }

    @Override
    protected String keyFromViewModel(final Client client) {
        return client.getCid() + "_" + client.getCallsign();
    }

    @Override
    protected Collection<VatsimClient> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api.data().getClients();
    }

    @Override
    protected void onAdd(final Client toAdd, final VatsimClient vatsimClient) {
        if (toAdd instanceof Controller) {
            ((Controller) toAdd).setFromCallsignParserResult(callsignParser.parse(vatsimClient));
        } else if (toAdd instanceof Pilot) {
            final Pilot pilot = (Pilot) toAdd;
            pilot.getFlightPlan()
                 .departureAirportPropertyWritable()
                 .set(getAirport(vatsimClient.getPlannedDepartureAirport()));
            pilot.getFlightPlan()
                 .arrivalAirportPropertyWritable()
                 .set(getAirport(vatsimClient.getPlannedDestinationAirport()));
            pilot.getFlightPlan()
                 .alternativeAirportPropertyWritable()
                 .set(getAirport(vatsimClient.getPlannedAlternativeAirport()));
        }
    }

    @Override
    protected void onRemove(final Client toRemove) {
        if (toRemove instanceof Controller) {
            ((Controller) toRemove).setFromCallsignParserResult(CallsignParser.Result.EMPTY);
        } else if (toRemove instanceof Pilot) {
            final Pilot pilot = (Pilot) toRemove;
            pilot.getFlightPlan().departureAirportPropertyWritable().set(null);
            pilot.getFlightPlan().arrivalAirportPropertyWritable().set(null);
        }
    }

    @Override
    protected void onUpdate(final Client toUpdate, final VatsimClient vatsimClient) {
        onAdd(toUpdate, vatsimClient);
    }

    @Override
    public void reload() throws RepositoryException {
        super.reload();

        final List<Entry<Pilot, Point>> list = pilots
                .stream()
                .filter(e -> e.getPosition().getX() >= -180)
                .filter(e -> e.getPosition().getX() <= 180)
                .filter(e -> e.getPosition().getY() >= -90)
                .filter(e -> e.getPosition().getY() <= 90)
                .map(ClientRepository::entry)
                .collect(Collectors.toList());

        rTree = RTree.star().create(list);
    }

    public List<Pilot> searchByPosition(final Point2D p, final double maxDistance) {
        final var spliterator = rTree.nearest(Geometries.point(p.getX(), p.getY()), maxDistance, 3).spliterator();
        return StreamSupport.stream(spliterator, false)
                            .map(Entry::value)
                            .collect(Collectors.toCollection(ArrayList::new));
    }

    private static EntryDefault<Pilot, Point> entry(final Pilot e) {
        return new EntryDefault<>(e, Geometries.pointGeographic(e.getPosition().getX(), e.getPosition().getY()));
    }

    public ObservableList<Pilot> pilots() {
        return pilots.getReadOnlyProperty();
    }

    public ObservableList<Controller> controllers() {
        return controllers.getReadOnlyProperty();
    }

    private Airport getAirport(final String icao) {
        if (icao == null || icao.isBlank() || "none".equalsIgnoreCase(icao)) {
            return null;
        }

        final List<Airport> airports = airportRepository.getByIcao(icao);
        if (airports.isEmpty()) {
            log.warn("Unknown airport " + icao);
            return null;
        }

        if (airports.size() > 1) {
            log.warn("Multiple airports for " + icao + ": " + airports);
        }

        return airports.get(0);
    }
}