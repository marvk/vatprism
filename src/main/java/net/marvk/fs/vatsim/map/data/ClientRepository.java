package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimClient;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Slf4j
public class ClientRepository extends SimpleRepository<Client, VatsimClient> {
    private final ReadOnlyListWrapper<Pilot> pilots;
    private final ReadOnlyListWrapper<Controller> controllers;
    private final AirportRepository airportRepository;
    private final CallsignParser callsignParser;

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
            default -> new Client();
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
