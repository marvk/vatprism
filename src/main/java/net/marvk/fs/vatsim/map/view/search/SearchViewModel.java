package net.marvk.fs.vatsim.map.view.search;

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.StatusScope;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchViewModel implements ViewModel {
    private final StringProperty query = new SimpleStringProperty();
    private final ReadOnlyObjectWrapper<ObservableList<Data>> results = new ReadOnlyObjectWrapper<>();
    private final DelegateCommand searchCommand;

    @InjectScope
    private StatusScope statusScope;

    @Inject
    public SearchViewModel(final Provider<SearchActionSupplier> searchActionProvider) {
        final SearchActionSupplier supplier = searchActionProvider.get();
        this.searchCommand = new DelegateCommand(() -> supplier.createAction(query.get(), results), query.isNotEmpty(), true);
    }

    public void initialize() {
        results.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                statusScope.getSearchedData().clear();
            } else {
                statusScope.getSearchedData().setAll(newValue);
            }

            Notifications.REPAINT.publish();
        });
    }

    public void search() {
        if (searchCommand.isExecutable() && searchCommand.isNotRunning()) {
            searchCommand.execute();
        }
    }

    public void clear() {
        query.set("");
        results.set(null);
    }

    public String getQuery() {
        return query.get();
    }

    public StringProperty queryProperty() {
        return query;
    }

    public void setQuery(final String query) {
        this.query.set(query);
    }

    public List<? extends Data> getResults() {
        return results.get();
    }

    public ReadOnlyObjectProperty<ObservableList<Data>> resultsProperty() {
        return results.getReadOnlyProperty();
    }

    public void setDataDetail(final Data data) {
        if (data != null) {
            Notifications.SET_DATA_DETAIL.publish(data);
        }
    }

    public DelegateCommand getSearchCommand() {
        return searchCommand;
    }

    private static class SearchActionSupplier {
        private final ClientRepository clientRepository;
        private final AirportRepository airportRepository;
        private final FlightInformationRegionBoundaryRepository firbRepository;
        private final UpperInformationRegionRepository uirRepository;

        @Inject
        public SearchActionSupplier(
                final ClientRepository clientRepository,
                final AirportRepository airportRepository,
                final FlightInformationRegionBoundaryRepository firbRepository,
                final UpperInformationRegionRepository uirRepository
        ) {
            this.clientRepository = clientRepository;
            this.airportRepository = airportRepository;
            this.firbRepository = firbRepository;
            this.uirRepository = uirRepository;
        }

        public Action createAction(final String query, final ObjectProperty<ObservableList<Data>> result) {
            return new SearchAction(query, result);
        }

        private class SearchAction extends Action {
            private final String query;
            private final ObjectProperty<ObservableList<Data>> result;

            public SearchAction(final String query, final ObjectProperty<ObservableList<Data>> result) {
                this.query = query;
                this.result = result;
            }

            @Override
            protected void action() throws Exception {
                final FilteredList<? extends Data> clients = clientRepository
                        .list()
                        .filtered(this::containsQuery);

                final FilteredList<Airport> airports = airportRepository
                        .list()
                        .filtered(this::containsQuery);

                final FilteredList<FlightInformationRegionBoundary> firs = firbRepository
                        .list()
                        .filtered(this::containsQuery);

                final FilteredList<UpperInformationRegion> uirs = uirRepository
                        .list()
                        .filtered(this::containsQuery);

                final List<? extends Data> result = Stream
                        .of(clients, airports)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));

                this.result.set((ObservableList<Data>) result);
            }

            private boolean containsQuery(final Client client) {
                return StringUtils.containsIgnoreCase(client.getCallsign(), query);
            }

            private boolean containsQuery(final FlightInformationRegionBoundary firb) {
                final boolean namesContainQuery = firb
                        .getFlightInformationRegions()
                        .stream()
                        .map(FlightInformationRegion::getName)
                        .anyMatch(e -> StringUtils.containsIgnoreCase(e, query));

                return StringUtils.containsIgnoreCase(firb.getIcao(), query) || namesContainQuery;
            }

            private boolean containsQuery(final UpperInformationRegion uir) {
                return StringUtils.containsIgnoreCase(uir.getIcao(), query) ||
                        StringUtils.containsIgnoreCase(uir.getName(), query);
            }

            private boolean containsQuery(final Airport airport) {
                return StringUtils.containsIgnoreCase(airport.getIcao(), query) ||
                        airport.getNames().stream().anyMatch(name -> StringUtils.containsIgnoreCase(name, query));
            }
        }
    }
}
