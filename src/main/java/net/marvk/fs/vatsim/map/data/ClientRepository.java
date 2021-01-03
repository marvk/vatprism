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
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.api.data.VatsimController;
import net.marvk.fs.vatsim.api.data.VatsimFlightPlan;
import net.marvk.fs.vatsim.api.data.VatsimPilot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Log4j2
public class ClientRepository extends SimpleRepository<Client, VatsimClient> {
    private final ReadOnlyListWrapper<Pilot> pilots;
    private final ReadOnlyListWrapper<Controller> controllers;
    private final AirportRepository airportRepository;
    private final CallsignParser callsignParser;
    private final ClientTypeMapper clientTypeMapper;
    private RTree<Pilot, Point> rTree = RTree.create();

    @Inject
    public ClientRepository(final VatsimApi vatsimApi, final AirportRepository airportRepository, final CallsignParser callsignParser, final ClientTypeMapper clientTypeMapper) {
        super(vatsimApi);
        this.airportRepository = airportRepository;
        this.callsignParser = callsignParser;
        this.clientTypeMapper = clientTypeMapper;

        // yikes, but it works, sooo...
        pilots = new ReadOnlyListWrapper<Pilot>(new FilteredList(list(), e -> e instanceof Pilot));
        controllers = new ReadOnlyListWrapper<Controller>(new FilteredList(list(), e -> e instanceof Controller));
    }

    @Override
    protected Client newViewModelInstance(final VatsimClient vatsimClient) {
        return switch (vatsimClient.getClientType()) {
            case CONTROLLER -> new Controller();
            case PILOT -> new Pilot();
            case ATIS -> new Atis();
            default -> null;
        };
    }

    @Override
    protected String keyFromModel(final VatsimClient vatsimClient) {
        return vatsimClient.getCid() + vatsimClient.getCallsign() + clientTypeMapper.map(vatsimClient.getClientType());
    }

    @Override
    protected String keyFromViewModel(final Client client) {
        return client.getCid() + client.getCallsign() + client.clientType();
    }

    @Override
    protected Collection<VatsimClient> extractModels(final VatsimApi api) throws VatsimApiException {
        try {
            return api.data().getClients();
        } catch (final Throwable t) {
            throw new VatsimApiException("Failed to load items", t);
        }
    }

    @Override
    protected void onAdd(final Client toAdd, final VatsimClient vatsimClient) {
        final ClientType type = toAdd.clientType();
        if (type == ClientType.CONTROLLER || type == ClientType.ATIS) {
            ((Controller) toAdd).setFromCallsignParserResult(callsignParser.parse((VatsimController) vatsimClient));
        } else if (type == ClientType.PILOT) {
            final Pilot pilot = (Pilot) toAdd;
            final VatsimFlightPlan flightPlan = ((VatsimPilot) vatsimClient).getFlightPlan();
            if (flightPlan != null) {
                setAirports(pilot, flightPlan);
            }
        }
    }

    private void setAirports(final Pilot pilot, final VatsimFlightPlan flightPlan) {
        pilot.getFlightPlan()
             .departureAirportPropertyWritable()
             .set(getAirport(flightPlan.getDepartureAirport().strip()));
        pilot.getFlightPlan()
             .arrivalAirportPropertyWritable()
             .set(getAirport(flightPlan.getArrivalAirport().strip()));
        pilot.getFlightPlan()
             .alternativeAirportPropertyWritable()
             .set(getAirport(flightPlan.getAlternateAirport().strip()));
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
    protected void updateList(final Collection<VatsimClient> updatedModels) {
        super.updateList(updatedModels);
        createRTree();
    }

    private void createRTree() {
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

    public List<Pilot> searchByPosition(final Point2D p, final double maxDistance, final int maxCount) {
        final double offsetX;

        if (p.getX() > 180) {
            offsetX = -360;
        } else if (p.getX() < -180) {
            offsetX = 360;
        } else {
            offsetX = 0;
        }

        final var spliterator = rTree.nearest(Geometries.point(p.getX() + offsetX, p.getY()), maxDistance, maxCount)
                                     .spliterator();
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
            log.info("Unknown airport \"%s\"".formatted(icao));
            return null;
        }

        if (airports.size() > 1) {
            log.warn("Multiple airports for " + icao + ": " + airports);
        }

        return airports.get(0);
    }
}
