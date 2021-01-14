package net.marvk.fs.vatsim.map.view.filter.filtereditor;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.ControllerRating;
import net.marvk.fs.vatsim.map.data.ControllerType;
import net.marvk.fs.vatsim.map.data.Filter;
import net.marvk.fs.vatsim.map.data.PilotRating;
import net.marvk.fs.vatsim.map.view.filter.FilterStringListViewModel;

@Log4j2
public class FilterEditorViewModel implements ViewModel {
    private final SimpleStringProperty name = new SimpleStringProperty();

    private final ReadOnlyListWrapper<ControllerRating> availableRatings;
    private final ReadOnlyListWrapper<ControllerType> availableFacilities;
    private final ReadOnlyListWrapper<Filter.FlightStatus> availableFlightStatuses;
    private final ReadOnlyListWrapper<Filter.FlightType> availableFlightTypes;

    private final ListProperty<FilterStringListViewModel> callsigns = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<FilterStringListViewModel> cids = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<FilterStringListViewModel> departures = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<FilterStringListViewModel> arrivals = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<ControllerRating> ratings = new SimpleListProperty<>();
    private final ListProperty<ControllerType> facilities = new SimpleListProperty<>();
    private final ListProperty<Filter.FlightStatus> flightStatuses = new SimpleListProperty<>();
    private final ListProperty<Filter.FlightType> flightTypes = new SimpleListProperty<>();

    private final BooleanProperty flightPlanFiled = new SimpleBooleanProperty();

    public FilterEditorViewModel() {
        if (ControllerRating.values().length == 0) {
            log.error("No controller ratings available");
        }
        if (PilotRating.values().length == 0) {
            log.error("No pilot ratings available");
        }

        availableRatings = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(ControllerRating.values()));
        availableFacilities = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(ControllerType.values()));
        availableFlightStatuses = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(Filter.FlightStatus.values()));
        availableFlightTypes = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(Filter.FlightType.values()));
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(final String name) {
        this.name.set(name);
    }

    public ObservableList<FilterStringListViewModel> getCallsigns() {
        return callsigns.get();
    }

    public ListProperty<FilterStringListViewModel> callsignsProperty() {
        return callsigns;
    }

    public void setCallsigns(final ObservableList<FilterStringListViewModel> callsigns) {
        this.callsigns.set(callsigns);
    }

    public ObservableList<FilterStringListViewModel> getCids() {
        return cids.get();
    }

    public ListProperty<FilterStringListViewModel> cidsProperty() {
        return cids;
    }

    public void setCids(final ObservableList<FilterStringListViewModel> cids) {
        this.cids.set(cids);
    }

    public ObservableList<FilterStringListViewModel> getDepartures() {
        return departures.get();
    }

    public ListProperty<FilterStringListViewModel> departuresProperty() {
        return departures;
    }

    public void setDepartures(final ObservableList<FilterStringListViewModel> departures) {
        this.departures.set(departures);
    }

    public ObservableList<FilterStringListViewModel> getArrivals() {
        return arrivals.get();
    }

    public ListProperty<FilterStringListViewModel> arrivalsProperty() {
        return arrivals;
    }

    public void setArrivals(final ObservableList<FilterStringListViewModel> arrivals) {
        this.arrivals.set(arrivals);
    }

    public ObservableList<ControllerRating> getRatings() {
        return ratings.get();
    }

    public ListProperty<ControllerRating> ratingsProperty() {
        return ratings;
    }

    public void setRatings(final ObservableList<ControllerRating> ratings) {
        this.ratings.set(ratings);
    }

    public ObservableList<ControllerType> getFacilities() {
        return facilities.get();
    }

    public ListProperty<ControllerType> facilitiesProperty() {
        return facilities;
    }

    public void setFacilities(final ObservableList<ControllerType> facilities) {
        this.facilities.set(facilities);
    }

    public ObservableList<Filter.FlightStatus> getFlightStatuses() {
        return flightStatuses.get();
    }

    public ListProperty<Filter.FlightStatus> flightStatusesProperty() {
        return flightStatuses;
    }

    public void setFlightStatuses(final ObservableList<Filter.FlightStatus> flightStatuses) {
        this.flightStatuses.set(flightStatuses);
    }

    public ObservableList<Filter.FlightType> getFlightTypes() {
        return flightTypes.get();
    }

    public ListProperty<Filter.FlightType> flightTypesProperty() {
        return flightTypes;
    }

    public void setFlightTypes(final ObservableList<Filter.FlightType> flightTypes) {
        this.flightTypes.set(flightTypes);
    }

    public boolean isFlightPlanFiled() {
        return flightPlanFiled.get();
    }

    public BooleanProperty flightPlanFiledProperty() {
        return flightPlanFiled;
    }

    public void setFlightPlanFiled(final boolean flightPlanFiled) {
        this.flightPlanFiled.set(flightPlanFiled);
    }

    public ReadOnlyListProperty<ControllerRating> getAvailableRatings() {
        return availableRatings.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<ControllerType> getAvailableFacilities() {
        return availableFacilities.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<Filter.FlightStatus> getAvailableFlightStatuses() {
        return availableFlightStatuses.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<Filter.FlightType> getAvailableFlightTypes() {
        return availableFlightTypes.getReadOnlyProperty();
    }
}
