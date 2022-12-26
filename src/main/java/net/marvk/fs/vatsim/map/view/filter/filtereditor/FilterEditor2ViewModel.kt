package net.marvk.fs.vatsim.map.view.filter.filtereditor

import de.saxsys.mvvmfx.InjectScope
import de.saxsys.mvvmfx.ViewModel
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.paint.Color
import lombok.extern.log4j.Log4j2
import net.marvk.fs.vatsim.map.Debouncer
import net.marvk.fs.vatsim.map.data.ControllerRating
import net.marvk.fs.vatsim.map.data.ControllerType
import net.marvk.fs.vatsim.map.data.Filter
import net.marvk.fs.vatsim.map.data.PilotRating
import net.marvk.fs.vatsim.map.extensions.createLogger
import net.marvk.fs.vatsim.map.view.extensions.getValue
import net.marvk.fs.vatsim.map.view.extensions.setValue
import net.marvk.fs.vatsim.map.view.extensions.unmodifiable
import net.marvk.fs.vatsim.map.view.filter.FilterScope
import net.marvk.fs.vatsim.map.view.filter.FilterStringListViewModel
import org.apache.logging.log4j.Logger
import java.time.Duration

@Log4j2
class FilterEditor2ViewModel : ViewModel {
    private val log: Logger = createLogger()

    val availableRatings: ObservableList<ControllerRating> by unmodifiable(FXCollections.observableArrayList(*ControllerRating.values()))
    val availableFacilities: ObservableList<ControllerType> by unmodifiable(FXCollections.observableArrayList(*ControllerType.values()))
    val availableFlightStatuses: ObservableList<Filter.FlightStatus> by unmodifiable(FXCollections.observableArrayList(*Filter.FlightStatus.values()))
    val availableFlightTypes: ObservableList<Filter.FlightType> by unmodifiable(FXCollections.observableArrayList(*Filter.FlightType.values()))

    val currentProperty get() = filterScope.filterProperty()
    var current: Filter
        get() = filterScope.filter
        private set(value) {
            filterScope.filter = value
        }

    val nameProperty = SimpleStringProperty()
    var name by nameProperty

    val textColorProperty = SimpleObjectProperty<Color>()
    var textColor by textColorProperty

    val backgroundColorProperty = SimpleObjectProperty<Color>()
    var backgroundColor by backgroundColorProperty

    val filterTypes = FXCollections.observableArrayList<Filter.Type>()

    val callsignCidOperatorProperty = SimpleObjectProperty<Filter.Operator>()
    var callsignCidOperator by callsignCidOperatorProperty

    val departuresArrivalsOperatorProperty = SimpleObjectProperty<Filter.Operator>()
    var departuresArrivalsOperator by departuresArrivalsOperatorProperty

    val callsigns: ObservableList<FilterStringListViewModel> = createFilterStringListObservableList()
    val cids: ObservableList<FilterStringListViewModel> = createFilterStringListObservableList()
    val departures: ObservableList<FilterStringListViewModel> = createFilterStringListObservableList()
    val arrivals: ObservableList<FilterStringListViewModel> = createFilterStringListObservableList()

    val ratings: ObservableList<ControllerRating> = FXCollections.observableArrayList()
    val facilities: ObservableList<ControllerType> = FXCollections.observableArrayList()
    val flightStatus: ObservableList<Filter.FlightStatus> = FXCollections.observableArrayList()
    val flightTypes: ObservableList<Filter.FlightType> = FXCollections.observableArrayList()

    val flightPlanFiledProperty = SimpleBooleanProperty()
    var flightPlanFiled by flightPlanFiledProperty

    @InjectScope
    private lateinit var filterScope: FilterScope

    fun initialize() {
        if (ControllerRating.values().isEmpty()) {
            log.error("No controller ratings available")
        }
        if (PilotRating.values().isEmpty()) {
            log.error("No pilot ratings available")
        }
        currentProperty.addListener { _, _, newValue ->
            load(newValue)
        }

        listOf(
            nameProperty,
            textColorProperty,
            backgroundColorProperty,
            callsignCidOperatorProperty,
            departuresArrivalsOperatorProperty,
            flightPlanFiledProperty,
        ).forEach {
            it.addListener { a, b, c ->
                save()
            }
        }

        listOf(
            filterTypes,
            callsigns,
            cids,
            departures,
            arrivals,
            ratings,
            facilities,
            flightStatus,
            flightTypes,
        ).forEach { list ->
            list.addListener(ListChangeListener {
                var actualMutation = false

                while (it.next() && !actualMutation) {
                    if (it.wasPermutated()) {
                        continue
                    }

                    if (it.wasReplaced() && it.removedSize == it.addedSize && it.addedSubList.zip(it.removed).all { it.first == it.second }) {
                        continue
                    }

                    actualMutation = true
                }

                if (actualMutation) {
                    save()
                }
            })
        }
    }

    fun load(filter: Filter?) {
        filter ?: return

        name = filter.name
        textColor = filter.textColor
        backgroundColor = filter.backgroundColor
        filterTypes.setAll(filter.types)
        callsignCidOperator = filter.callsignsCidsOperator
        departuresArrivalsOperator = filter.departuresArrivalsOperator
        setIfHasChanges(filter.callsignPredicates, callsigns)
        setIfHasChanges(filter.cidPredicates, cids)
        setIfHasChanges(filter.departureAirportPredicates, departures)
        setIfHasChanges(filter.arrivalAirportPredicates, arrivals)
        ratings.setAll(filter.controllerRatings)
        facilities.setAll(filter.controllerTypes)
        flightStatus.setAll(filter.flightStatuses)
        flightTypes.setAll(filter.flightTypes)
        flightPlanFiled = filter.isFlightPlanRequired
    }

    private fun setIfHasChanges(from: List<Filter.StringPredicate>, to: ObservableList<FilterStringListViewModel>) {
        from.forEachIndexed { index, predicate ->
            if (index < to.size) {
                to[index].apply {
                    name = predicate.name
                    content = predicate.content
                    isRegex = predicate.isRegex
                }
            } else {
                FilterStringListViewModel(
                    predicate.name,
                    predicate.content,
                    predicate.isRegex,
                ).also(to::add)
            }

        }

        if (from.size > to.size) {
            to.remove(from.size, to.size)
        }
    }

    fun compile() =
        Filter(
            current.uuid,
            name,
            textColor,
            backgroundColor,
            filterTypes,
            callsigns.let(::predicates),
            callsignCidOperator,
            cids.let(::predicates),
            departures.let(::predicates),
            departuresArrivalsOperator,
            arrivals.let(::predicates),
            emptyList(),
            ratings,
            flightStatus,
            facilities,
            flightTypes,
            emptyList(),
            flightPlanFiled,
        )

    private fun predicates(viewModels: List<FilterStringListViewModel>) =
        viewModels.mapNotNull { it.predicate }

    private val saveDebouncer = Debouncer({
        Platform.runLater {
            current = compile()
        }
    }, Duration.ofMillis(1000))

    fun save() {
        saveDebouncer.callDebounced()
    }
}

private fun createFilterStringListObservableList() =
    FXCollections.observableArrayList<FilterStringListViewModel> { arrayOf(it.nameProperty(), it.contentProperty(), it.predicateProperty(), it.regexProperty(), it.validProperty()) }
