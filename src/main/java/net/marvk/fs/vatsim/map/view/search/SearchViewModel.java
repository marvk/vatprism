package net.marvk.fs.vatsim.map.view.search;

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.BaseViewModel;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.StatusScope;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchViewModel extends BaseViewModel {
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
        statusScope.searchQueryProperty().bind(query);

        query.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isBlank()) {
                search();
            } else {
                clear();
            }
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
            private final ObjectProperty<ObservableList<Data>> result;
            private final DataVisitor<Boolean> predicateSupplier;

            public SearchAction(final String query, final ObjectProperty<ObservableList<Data>> result) {
                this.result = result;
                this.predicateSupplier = new SimplePredicatesDataVisitor(query);
            }

            @Override
            protected void action() {
                final FilteredList<? extends Data> clients = clientRepository
                        .list()
                        .filtered(predicateSupplier::visit);

                final FilteredList<Airport> airports = airportRepository
                        .list()
                        .filtered(predicateSupplier::visit);

                final FilteredList<FlightInformationRegionBoundary> firs = firbRepository
                        .list()
                        .filtered(predicateSupplier::visit);

                final FilteredList<UpperInformationRegion> uirs = uirRepository
                        .list()
                        .filtered(predicateSupplier::visit);

                final ObservableList<Data> result = Stream
                        .of(clients, airports)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toCollection(FXCollections::observableArrayList));

                this.result.set(result);
            }
        }
    }
}
