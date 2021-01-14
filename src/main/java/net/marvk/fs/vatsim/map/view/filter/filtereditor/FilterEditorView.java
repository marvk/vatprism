package net.marvk.fs.vatsim.map.view.filter.filtereditor;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import net.marvk.fs.vatsim.map.data.ControllerRating;
import net.marvk.fs.vatsim.map.data.ControllerType;
import net.marvk.fs.vatsim.map.data.Filter;
import net.marvk.fs.vatsim.map.view.filter.FilterStringListViewModel;
import org.kordamp.ikonli.fileicons.FileIcons;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.util.function.Function;

public class FilterEditorView implements FxmlView<FilterEditorViewModel> {

    @FXML
    private Label filterName;
    @FXML
    private ToggleGroup filterType;

    @FXML
    private ToggleGroup callsignCidAndOr;
    @FXML
    private Label callsignCidOr;
    @FXML
    private Label callsignCidAnd;

    @FXML
    private ListView<FilterStringListViewModel> callsignList;
    @FXML
    private ToggleButton callsignRegex;
    @FXML
    private TextField callsignInput;
    @FXML
    private Button callsignSubmit;

    @FXML
    private ListView<FilterStringListViewModel> cidList;
    @FXML
    private ToggleButton cidRegex;
    @FXML
    private TextField cidInput;
    @FXML
    private Button cidSubmit;

    @FXML
    private ToggleGroup departuresArrivalsAndOr;
    @FXML
    private Label departuresArrivalsOr;
    @FXML
    private Label departuresArrivalsAnd;

    @FXML
    private ListView<FilterStringListViewModel> departuresList;
    @FXML
    private ToggleButton departuresRegex;
    @FXML
    private TextField departuresInput;
    @FXML
    private Button departuresSubmit;

    @FXML
    private ListView<FilterStringListViewModel> arrivalsList;
    @FXML
    private ToggleButton arrivalsRegex;
    @FXML
    private TextField arrivalsInput;
    @FXML
    private Button arrivalsSubmit;

    @FXML
    private ListView<ControllerRating> ratingsList;
    @FXML
    private ListView<ControllerType> facilitiesList;

    @FXML
    private ListView<Filter.FlightStatus> flightStatusList;
    @FXML
    private CheckBox flightPlanFiled;
    @FXML
    private ComboBox<Filter.FlightType> flightType;

    @InjectViewModel
    private FilterEditorViewModel viewModel;

    public void initialize() {
        setupStringFilterList(callsignList, callsignRegex, callsignInput, callsignSubmit, viewModel.getCallsigns());
        setupStringFilterList(cidList, cidRegex, cidInput, cidSubmit, viewModel.getCids());
        setupStringFilterList(departuresList, departuresRegex, departuresInput, departuresSubmit, viewModel.getDepartures());
        setupStringFilterList(arrivalsList, arrivalsRegex, arrivalsInput, arrivalsSubmit, viewModel.getArrivals());

        setupMultiSelection(
                ratingsList,
                viewModel.getAvailableRatings(),
                viewModel.ratingsProperty(),
                e -> "%s (%s)".formatted(e.getShortName(), e.getLongName())
        );
        setupMultiSelection(
                facilitiesList,
                viewModel.getAvailableFacilities(),
                viewModel.facilitiesProperty(),
                Enum::toString
        );
        setupMultiSelection(
                flightStatusList,
                viewModel.getAvailableFlightStatuses(),
                viewModel.flightStatusesProperty(),
                Enum::toString
        );
        setupMultiSelection(flightType, viewModel.getAvailableFlightTypes(), viewModel.flightTypesProperty());
        flightType.getSelectionModel().select(Filter.FlightType.ANY);

        setupToggleGroup(callsignCidAndOr, callsignCidOr, callsignCidAnd);
        setupToggleGroup(departuresArrivalsAndOr, departuresArrivalsOr, departuresArrivalsAnd);
    }

    private <T> void setupMultiSelection(
            final ListView<T> listView,
            final ReadOnlyListProperty<T> available,
            final ListProperty<T> selected,
            final Function<T, String> cellValueMapper
    ) {
        listView.setItems(available);
        listView.setCellFactory(param -> new StringMappedListCell<>(cellValueMapper));
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    }

    private void setupToggleGroup(final ToggleGroup toggleGroup, final Label or, final Label and) {
        or.setOnMouseClicked(e -> toggleGroup.selectToggle(toggleGroup.getToggles().get(0)));
        and.setOnMouseClicked(e -> toggleGroup.selectToggle(toggleGroup.getToggles().get(1)));
    }

    private <T> void setupMultiSelection(
            final ComboBox<T> comboBox,
            final ReadOnlyListProperty<T> available,
            final ListProperty<T> selected
    ) {
        comboBox.setItems(available);
    }

    private void setupStringFilterList(
            final ListView<FilterStringListViewModel> list,
            final ToggleButton regex,
            final TextField input,
            final Button submit,
            final ObservableList<FilterStringListViewModel> items
    ) {
        list.setItems(items);
        list.setCellFactory(param -> new FilterStringListViewModelListCell());
        input.setOnAction(e -> addToList(list, regex, input));
        submit.setOnAction(e -> addToList(list, regex, input));
    }

    private void addToList(final ListView<FilterStringListViewModel> list, final ToggleButton regex, final TextField input) {
        list.getItems().add(new FilterStringListViewModel(regex.isSelected(), input.getText()));
    }

    private static class StringMappedListCell<T> extends ListCell<T> {
        private final Function<T, String> cellValueMapper;

        public StringMappedListCell(final Function<T, String> cellValueMapper) {
            this.cellValueMapper = cellValueMapper;
        }

        @Override
        public void updateItem(final T item, final boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
            } else {
                setText(cellValueMapper.apply(item));
            }
        }
    }

    private static class FilterStringListViewModelListCell extends ListCell<FilterStringListViewModel> {
        private HBox content;
        private Label label;
        private Button button;
        private FontIcon fontIcon;

        private Parent content(final FilterStringListViewModel item) {
            if (content == null) {
                label = new Label();
                button = new Button();
                final FontIcon buttonIcon = new FontIcon(Octicons.X_16);
                buttonIcon.getStyleClass().add("filter-list-icon");
                button.setGraphic(buttonIcon);
                fontIcon = new FontIcon();
                fontIcon.getStyleClass().add("filter-list-icon");
                final Region region = new Region();
                region.setPrefWidth(0);
                region.setPrefHeight(0);
                HBox.setHgrow(region, Priority.ALWAYS);
                final VBox fontIconHolder = new VBox(fontIcon);
                fontIconHolder.setAlignment(Pos.CENTER);
                fontIconHolder.setPadding(new Insets(0, 5, 0, 5));
                content = new HBox(fontIconHolder, label, region, button);
                content.setAlignment(Pos.CENTER_LEFT);
            }

            button.setOnAction(e -> getListView().getItems().remove(item));

            label.textProperty().bind(item.contentProperty());
            fontIcon.iconCodeProperty().bind(Bindings.createObjectBinding(
                    () -> item.isRegex() ? FileIcons.REGEX : Octicons.TYPOGRAPHY_16,
                    item.regexProperty()
            ));

            return content;
        }

        @Override
        public void updateItem(final FilterStringListViewModel item, final boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
                setGraphic(null);
            } else {
                setGraphic(content(item));
            }
        }
    }
}
