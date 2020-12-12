package net.marvk.fs.vatsim.map.view.datadetail.trafficdetail;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import net.marvk.fs.vatsim.map.data.Airline;
import net.marvk.fs.vatsim.map.data.FlightPlan;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DetailSubViewModel;

import java.util.Comparator;
import java.util.Locale;

public class TrafficDetailViewModel extends DetailSubViewModel<ObservableList<FlightPlan>> {
    private static final Comparator<FlightPlan> CALLSIGN_COMPARATOR;

    static {
        final Comparator<FlightPlan> airline = Comparator.comparing(
                e -> e.getPilot().getAirline(),
                Comparator.nullsLast(Airline.comparingByIcao())
        );

        final Comparator<FlightPlan> flightNumber = Comparator.comparing(
                e -> e.getPilot().getFlightNumber(),
                Comparator.nullsLast(String::compareTo)
        );

        final Comparator<FlightPlan> callsign = Comparator.comparing(
                e -> e.getPilot().getCallsign(),
                Comparator.nullsLast(String::compareTo)
        );

        CALLSIGN_COMPARATOR = airline.thenComparing(flightNumber).thenComparing(callsign);
    }

    private final ReadOnlyObjectWrapper<ObservableList<FlightPlan>> filteredSortedData = new ReadOnlyObjectWrapper<>();

    private final StringProperty query = new ReadOnlyStringWrapper();

    public TrafficDetailViewModel() {
        data.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                filteredSortedData.set(FXCollections.emptyObservableList());
            } else {
                final FilteredList<FlightPlan> filteredList = new FilteredList<>(newValue);
                filteredList.predicateProperty().bind(Bindings.createObjectBinding(
                        () -> this::matchesQuery,
                        query
                ));
                final SortedList<FlightPlan> sortedList = new SortedList<>(filteredList, CALLSIGN_COMPARATOR);
                filteredSortedData.set(sortedList);
            }
        });
    }

    private boolean matchesQuery(final FlightPlan flightPlan) {
        if (query.get() == null || query.get().isEmpty()) {
            return true;
        }

        return flightPlan
                .getPilot()
                .getCallsign()
                .toLowerCase(Locale.ROOT)
                .contains(query.get().toLowerCase(Locale.ROOT));
    }

    private final StringProperty title = new SimpleStringProperty();

    private final ObjectProperty<TrafficDetailView.Type> type = new SimpleObjectProperty<>();

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(final String title) {
        this.title.set(title);
    }

    public TrafficDetailView.Type getType() {
        return type.get();
    }

    public ObjectProperty<TrafficDetailView.Type> typeProperty() {
        return type;
    }

    public void setType(final TrafficDetailView.Type type) {
        this.type.set(type);
    }

    public ObservableList<FlightPlan> getFilteredSortedData() {
        return filteredSortedData.get();
    }

    public ReadOnlyObjectProperty<ObservableList<FlightPlan>> filteredSortedDataProperty() {
        return filteredSortedData.getReadOnlyProperty();
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

    public void clearQuery() {
        query.set("");
    }

}
