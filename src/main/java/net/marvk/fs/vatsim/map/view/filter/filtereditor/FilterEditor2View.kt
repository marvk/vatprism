package net.marvk.fs.vatsim.map.view.filter.filtereditor

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.beans.binding.Bindings
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import net.marvk.fs.vatsim.map.data.ControllerRating
import net.marvk.fs.vatsim.map.data.ControllerType
import net.marvk.fs.vatsim.map.data.Filter
import net.marvk.fs.vatsim.map.view.filter.FilterStringListViewModel
import net.marvk.fs.vatsim.map.view.filter.filtereditor.patternbox.PatternBox
import net.marvk.fs.vatsim.map.view.filter.filtereditor.selectbox.SelectBox
import net.marvk.fs.vatsim.map.view.vatprism2.controls.AndOrToggle
import net.marvk.fs.vatsim.map.view.vatprism2.controls.VatprismColorPicker

class FilterEditor2View : FxmlView<FilterEditor2ViewModel> {
    @FXML
    private lateinit var pilotCheckbox: CheckBox

    @FXML
    private lateinit var controllerCheckbox: CheckBox

    @FXML
    private lateinit var flightPlanFiledCheckBox: CheckBox

    @FXML
    private lateinit var backgroundColorPicker: VatprismColorPicker

    @FXML
    private lateinit var textColorPicker: VatprismColorPicker

    @FXML
    private lateinit var nameField: TextField

    @FXML
    private lateinit var departureArrivalToggle: AndOrToggle

    @FXML
    private lateinit var callsignCidToggle: AndOrToggle

    @FXML
    private lateinit var ratingsSelectBox: SelectBox<ControllerRating>

    @FXML
    private lateinit var facilitiesSelectBox: SelectBox<ControllerType>

    @FXML
    private lateinit var flightStatusBox: SelectBox<Filter.FlightStatus>

    @FXML
    private lateinit var flightTypeBox: SelectBox<Filter.FlightType>

    @FXML
    private lateinit var columnConstraints1: ColumnConstraints

    @FXML
    private lateinit var columnConstraints2: ColumnConstraints

    @FXML
    private lateinit var columnConstraints3: ColumnConstraints

    @FXML
    private lateinit var grid: GridPane

    @FXML
    private lateinit var callsignPatternBox: PatternBox

    @FXML
    private lateinit var cidPatternBox: PatternBox

    @FXML
    private lateinit var departurePatternBox: PatternBox

    @FXML
    private lateinit var arrivalPatternBox: PatternBox

    @InjectViewModel
    private lateinit var viewModel: FilterEditor2ViewModel

    fun initialize() {
        initTypeCheckbox(pilotCheckbox, Filter.Type.PILOT)
        initTypeCheckbox(controllerCheckbox, Filter.Type.CONTROLLER)

        nameField.textProperty().bindBidirectional(viewModel.nameProperty)
        textColorPicker.valueProperty().bindBidirectional(viewModel.textColorProperty)
        backgroundColorPicker.valueProperty().bindBidirectional(viewModel.backgroundColorProperty)

        callsignCidToggle.valueProperty.bindBidirectional(viewModel.callsignCidOperatorProperty)
        departureArrivalToggle.valueProperty.bindBidirectional(viewModel.departuresArrivalsOperatorProperty)

        initPatternBox(callsignPatternBox, viewModel.callsigns)
        initPatternBox(cidPatternBox, viewModel.cids)
        initPatternBox(departurePatternBox, viewModel.departures)
        initPatternBox(arrivalPatternBox, viewModel.arrivals)

        initSelectBox(ratingsSelectBox, viewModel.availableRatings, viewModel.ratings) { "${it.longName} (${it.shortName})" }
        initSelectBox(facilitiesSelectBox, viewModel.availableFacilities, viewModel.facilities, ControllerType::name)
        initSelectBox(flightStatusBox, viewModel.availableFlightStatuses, viewModel.flightStatus, Filter.FlightStatus::name)
        initSelectBox(flightTypeBox, viewModel.availableFlightTypes, viewModel.flightTypes, Filter.FlightType::name)

        flightPlanFiledCheckBox.selectedProperty().bindBidirectional(viewModel.flightPlanFiledProperty)
    }

    private fun initTypeCheckbox(checkBox: CheckBox, type: Filter.Type) {
        checkBox.selectedProperty().addListener { _, _, newValue ->
            if (newValue) {
                if (!viewModel.filterTypes.contains(type)) {
                    viewModel.filterTypes += type
                }
            } else {
                viewModel.filterTypes -= type
            }
        }
        viewModel.filterTypes.addListener(ListChangeListener {
            checkBox.isSelected = viewModel.filterTypes.contains(type)
        })
    }

    private fun <Item> initSelectBox(ratingsSelectBox: SelectBox<Item>, availableItems: ObservableList<Item>, viewModel: ObservableList<Item>, cellValueFactory: (Item) -> String) {
        ratingsSelectBox.availableItems.setAll(availableItems)
        ratingsSelectBox.cellValueFactory = cellValueFactory
        Bindings.bindContentBidirectional(ratingsSelectBox.selectedItems, viewModel)
    }

    private fun initPatternBox(callsignPatternBoxController: PatternBox, viewModel: ObservableList<FilterStringListViewModel>) {
        Bindings.bindContentBidirectional(callsignPatternBoxController.items, viewModel)
    }
}
