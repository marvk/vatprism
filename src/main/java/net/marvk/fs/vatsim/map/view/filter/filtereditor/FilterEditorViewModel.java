package net.marvk.fs.vatsim.map.view.filter.filtereditor;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.ControllerRating;
import net.marvk.fs.vatsim.map.data.ControllerType;
import net.marvk.fs.vatsim.map.data.Filter;
import net.marvk.fs.vatsim.map.data.PilotRating;
import net.marvk.fs.vatsim.map.view.filter.FilterScope;
import net.marvk.fs.vatsim.map.view.filter.FilterStringListViewModel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
public class FilterEditorViewModel implements ViewModel {
    private final ReadOnlyBooleanWrapper enabled = new ReadOnlyBooleanWrapper();

    private final ReadOnlyObjectWrapper<Filter> current = new ReadOnlyObjectWrapper<>();

    private final ObjectProperty<UUID> uuid = new SimpleObjectProperty<>();

    private final BooleanProperty filterEnabled = new SimpleBooleanProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<Color> textColor = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>();

    private final ObjectProperty<Filter.Type> filterType = new SimpleObjectProperty<>();

    private final ObjectProperty<Filter.Operator> callsignsCidsOperator = new SimpleObjectProperty<>();
    private final ObjectProperty<Filter.Operator> departuresArrivalsOperator = new SimpleObjectProperty<>();

    private final ReadOnlyListWrapper<ControllerRating> availableRatings;
    private final ReadOnlyListWrapper<ControllerType> availableFacilities;
    private final ReadOnlyListWrapper<Filter.FlightStatus> availableFlightStatuses;
    private final ReadOnlyListWrapper<Filter.FlightType> availableFlightTypes;

    private final ListProperty<FilterStringListViewModel> callsigns = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<FilterStringListViewModel> cids = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<FilterStringListViewModel> departures = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<FilterStringListViewModel> arrivals = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<ControllerRating> ratings = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<ControllerType> facilities = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<Filter.FlightStatus> flightStatuses = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<Filter.FlightType> flightTypes = new SimpleListProperty<>(FXCollections.observableArrayList(Filter.FlightType.ANY));

    private final BooleanProperty flightPlanFiled = new SimpleBooleanProperty();

    @InjectScope
    private FilterScope filterScope;

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

    public void initialize() {
        current.bindBidirectional(filterScope.filterProperty());
        current.addListener((observable, oldValue, newValue) -> load(newValue));
        enabled.bind(current.isNotNull());
    }

    private Filter compileFilter() {
        return new Filter(
                uuid.get(),
                name.get(),
                filterEnabled.get(),
                textColor.get(),
                backgroundColor.get(),
                filterType.get(),
                predicates(callsigns),
                callsignsCidsOperator.get(),
                predicates(cids),
                predicates(departures),
                departuresArrivalsOperator.get(),
                predicates(arrivals),
                Collections.emptyList(),
                ratings,
                flightStatuses,
                facilities,
                flightTypes,
                List.of(),
                flightPlanFiled.get()
        );
    }

    public void load(final Filter filter) {
        if (filter == null) {
            return;
        }

        setUuid(filter.getUuid());
        setName(filter.getName());
        setFilterEnabled(filter.isEnabled());
        setTextColor(filter.getTextColor());
        setBackgroundColor(filter.getBackgroundColor());
        setFilterType(filter.getType());
        callsigns.get().setAll(filterStringListViewModels(filter.getCallsignPredicates()));
        setCallsignsCidsOperator(filter.getCallsignsCidsOperator());
        cids.get().setAll(filterStringListViewModels(filter.getCidPredicates()));
        departures.get().setAll(filterStringListViewModels(filter.getDepartureAirportPredicates()));
        setDeparturesArrivalsOperator(filter.getDeparturesArrivalsOperator());
        arrivals.get().setAll(filterStringListViewModels(filter.getArrivalAirportPredicates()));
        // TODO PilotRating
        ratings.get().setAll(filter.getControllerRatings());
        flightStatuses.get().setAll(filter.getFlightStatuses());
        facilities.get().setAll(filter.getControllerTypes());
        flightTypes.get().setAll(filter.getFlightTypes());
        // TODO flight rules
        flightPlanFiled.set(filter.isFlightPlanRequired());
    }

    private static List<FilterStringListViewModel> filterStringListViewModels(final List<Filter.StringPredicate> predicates) {
        return predicates
                .stream()
                .map(e -> new FilterStringListViewModel(e.isRegex(), e.getContent()))
                .collect(Collectors.toList());
    }

    private static List<Filter.StringPredicate> predicates(final ListProperty<FilterStringListViewModel> viewModels) {
        return viewModels.stream()
                         .map(FilterStringListViewModel::getPredicate)
                         .filter(Objects::nonNull)
                         .collect(Collectors.toList());
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public ReadOnlyBooleanProperty enabledProperty() {
        return enabled.getReadOnlyProperty();
    }

    public UUID getUuid() {
        return uuid.get();
    }

    public ObjectProperty<UUID> uuidProperty() {
        return uuid;
    }

    public void setUuid(final UUID uuid) {
        this.uuid.set(uuid);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(final String name) {
        this.name.set(name);
    }

    public boolean isFilterEnabled() {
        return filterEnabled.get();
    }

    public BooleanProperty filterEnabledProperty() {
        return filterEnabled;
    }

    public void setFilterEnabled(final boolean filterEnabled) {
        this.filterEnabled.set(filterEnabled);
    }

    public Color getTextColor() {
        return textColor.get();
    }

    public ObjectProperty<Color> textColorProperty() {
        return textColor;
    }

    public void setTextColor(final Color textColor) {
        this.textColor.set(textColor);
    }

    public Color getBackgroundColor() {
        return backgroundColor.get();
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public void setBackgroundColor(final Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
    }

    public Filter.Type getFilterType() {
        return filterType.get();
    }

    public ObjectProperty<Filter.Type> filterTypeProperty() {
        return filterType;
    }

    public void setFilterType(final Filter.Type filterType) {
        this.filterType.set(filterType);
    }

    public Filter.Operator getCallsignsCidsOperator() {
        return callsignsCidsOperator.get();
    }

    public ObjectProperty<Filter.Operator> callsignsCidsOperatorProperty() {
        return callsignsCidsOperator;
    }

    public void setCallsignsCidsOperator(final Filter.Operator callsignsCidsOperator) {
        this.callsignsCidsOperator.set(callsignsCidsOperator);
    }

    public Filter.Operator getDeparturesArrivalsOperator() {
        return departuresArrivalsOperator.get();
    }

    public ObjectProperty<Filter.Operator> departuresArrivalsOperatorProperty() {
        return departuresArrivalsOperator;
    }

    public void setDeparturesArrivalsOperator(final Filter.Operator departuresArrivalsOperator) {
        this.departuresArrivalsOperator.set(departuresArrivalsOperator);
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

    public void save() {
        current.set(compileFilter());
    }
}
