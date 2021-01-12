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
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.BaseViewModel;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.StatusScope;
import net.marvk.fs.vatsim.map.view.ToolbarScope;
import net.marvk.fs.vatsim.map.view.preferences.Preferences;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class SearchViewModel extends BaseViewModel {
    private final StringProperty query = new SimpleStringProperty();
    private final ReadOnlyObjectWrapper<ObservableList<Data>> results = new ReadOnlyObjectWrapper<>();
    private final DelegateCommand searchCommand;
    private final Preferences preferences;

    @InjectScope
    private ToolbarScope toolbarScope;

    @InjectScope
    private StatusScope statusScope;

    @Inject
    public SearchViewModel(
            final Provider<SearchActionSupplier> searchActionProvider,
            final Preferences preferences
    ) {
        final SearchActionSupplier supplier = searchActionProvider.get();
        this.searchCommand = new DelegateCommand(() -> supplier.createAction(query.get(), results), true);
        this.preferences = preferences;
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

//        query.addListener((observable, oldValue, newValue) -> {
//            if (newValue != null && !newValue.isBlank()) {
//                search();
//            } else {
//                clear();
//            }
//        });
    }

    public void search() {
        if (searchCommand.isExecutable() && searchCommand.isNotRunning()) {
            searchCommand.execute();
        }
    }

    public void clear() {
        query.set("");
        search();
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

    public ReadOnlyIntegerProperty fontSizeProperty() {
        return preferences.integerProperty("general.font_size");
    }

    private static class SearchActionSupplier {
        private final FilteredList<Client> filteredClients;
        private final FilteredList<Airport> filteredAirports;
        private final FilteredList<FlightInformationRegionBoundary> filteredFirbs;
        private final FilteredList<UpperInformationRegion> filteredUirs;

        @Inject
        public SearchActionSupplier(
                final ClientRepository clientRepository,
                final AirportRepository airportRepository,
                final FlightInformationRegionBoundaryRepository firbRepository,
                final UpperInformationRegionRepository uirRepository,
                final ToolbarScope toolbarScope
        ) {
            this.filteredClients = new FilteredList<>(clientRepository.list());
            this.filteredAirports = new FilteredList<>(airportRepository.list());
            this.filteredFirbs = new FilteredList<>(firbRepository.list());
            this.filteredUirs = new FilteredList<>(uirRepository.list());

            toolbarScope.filteredClientsProperty().bindContent(filteredClients);
            toolbarScope.filteredAirportsProperty().bindContent(filteredAirports);
            toolbarScope.filteredFirbsProperty().bindContent(filteredFirbs);
            toolbarScope.filteredUirsProperty().bindContent(filteredUirs);
        }

        public Action createAction(final String query, final ObjectProperty<ObservableList<Data>> result) {
            return new SearchAction(query, result);
        }

        private class SearchAction extends Action {
            private final String query;
            private final ObjectProperty<ObservableList<Data>> result;
            private final DataVisitor<Boolean> predicateSupplier;

            public SearchAction(final String query, final ObjectProperty<ObservableList<Data>> result) {
                this.query = query;
                this.result = result;
                this.predicateSupplier = SimplePredicatesDataVisitor.nullOrBlankIsTrue(query);
            }

            @Override
            protected void action() {
                final long start = System.nanoTime();

                filteredClients.setPredicate(predicateSupplier::visit);
                filteredAirports.setPredicate(predicateSupplier::visit);
                filteredFirbs.setPredicate(predicateSupplier::visit);
                filteredUirs.setPredicate(predicateSupplier::visit);

                if (query.isBlank()) {
                    this.result.set(null);
                } else {
                    final ObservableList<Data> result = Stream
                            .of(filteredClients, filteredAirports, filteredFirbs, filteredUirs)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toCollection(FXCollections::observableArrayList));

                    this.result.set(result);
                }

                final Duration duration = Duration.ofNanos(System.nanoTime() - start);
                log.info("Search duration %sms".formatted(duration.toNanos() / 1_000_000.0));
            }
        }
    }
}
