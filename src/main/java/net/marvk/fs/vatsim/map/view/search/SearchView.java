package net.marvk.fs.vatsim.map.view.search;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.util.Optional;
import java.util.regex.Pattern;

public class SearchView implements FxmlView<SearchViewModel> {
    private final TextFlowHighlighter textFlowHighlighter;

    @FXML
    private VBox resultsHolder;
    @FXML
    private HBox progressIndicatorHolder;
    @FXML
    private ListView<Data> resultsList;
    @FXML
    private VBox container;
    @FXML
    private StackPane searchBoxHolder;
    @FXML
    private TextField searchBox;

    private final BooleanProperty resultsVisible = new SimpleBooleanProperty();

    @InjectViewModel
    private SearchViewModel viewModel;

    @Inject
    public SearchView(final TextFlowHighlighter textFlowHighlighter) {
        this.textFlowHighlighter = textFlowHighlighter;
    }

    public void initialize() {
        viewModel.queryProperty().bindBidirectional(searchBox.textProperty());
        searchBox.setOnAction(e -> viewModel.search());

        viewModel.getSearchCommand()
                 .runningProperty()
                 .addListener((observable, oldValue, newValue) -> searchingChanged(newValue));
        resultsHolder.getChildren().clear();
        resultsHolder.visibleProperty()
                     .bind(resultsVisible.and(viewModel.resultsProperty()
                                                       .isNotNull()
                                                       .or(viewModel.getSearchCommand().runningProperty())));

        resultsList.itemsProperty().bind(viewModel.resultsProperty());
        resultsList.setCellFactory(e -> new ResultCell(viewModel.queryProperty()));

        searchBoxHolder.getChildren().addAll(createSearchIcon(), createClearIcon());

        container.prefWidthProperty().bind(widthBinding());
        container.maxWidthProperty().bind(widthBinding());
        searchBox.prefWidthProperty().bind(widthBinding());
        searchBox.maxWidthProperty().bind(widthBinding());

        searchBox.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                viewModel.setQuery("");
                viewModel.search();
            }
        });

        Notifications.SEARCH.subscribe(() -> searchBox.requestFocus());
    }

    private DoubleBinding widthBinding() {
        return viewModel.fontSizeProperty().divide(12.0).multiply(200.0);
    }

    private FontIcon createClearIcon() {
        final FontIcon icon = FontIcon.of(Octicons.X_16);
        StackPane.setMargin(icon, new Insets(0, 3, 0, 0));
        StackPane.setAlignment(icon, Pos.CENTER_RIGHT);
        icon.visibleProperty().bind(searchBox.textProperty().isNotEmpty().or(viewModel.resultsProperty().isNotNull()));
        icon.setCursor(Cursor.HAND);
        icon.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                viewModel.clear();
                e.consume();
            }
        });
        icon.getStyleClass().add("clear-icon");
        icon.iconSizeProperty().bind(searchBox.heightProperty().divide(2.0));
        return icon;
    }

    private FontIcon createSearchIcon() {
        final FontIcon icon = FontIcon.of(Octicons.SEARCH_16);
        StackPane.setMargin(icon, new Insets(0, 0, 0, 3));
        StackPane.setAlignment(icon, Pos.CENTER_LEFT);
        icon.visibleProperty().bind(searchBox.textProperty().isEmpty().and(searchBox.focusedProperty().not()));
        icon.iconSizeProperty().bind(searchBox.heightProperty().divide(2.0));
        return icon;
    }

    private void searchingChanged(final boolean searching) {
        if (searching) {
            resultsHolder.getChildren().setAll(progressIndicatorHolder);
        } else {
            resultsHolder.getChildren().setAll(resultsList);
        }
    }

    public VBox getContainer() {
        return container;
    }

    public StackPane getSearchBoxHolder() {
        return searchBoxHolder;
    }

    public TextField getSearchBox() {
        return searchBox;
    }

    public boolean isResultsVisible() {
        return resultsVisible.get();
    }

    public BooleanProperty resultsVisibleProperty() {
        return resultsVisible;
    }

    public void setResultsVisible(final boolean resultsVisible) {
        this.resultsVisible.set(resultsVisible);
    }

    private class ResultCell extends ListCell<Data> implements OptionalDataVisitor<TextFlow> {
        private final ReadOnlyStringProperty query;
        private final ObjectProperty<Pattern> pattern = new SimpleObjectProperty<>();
        private final ObjectProperty<Data> data = new SimpleObjectProperty<>();
        private final HBox holder;
        private TextFlow textFlow;

        public ResultCell(final ReadOnlyStringProperty query) {
            this.query = query;
            this.pattern.bind(Bindings.createObjectBinding(
                    () -> Pattern.compile(Pattern.quote(query.get()), Pattern.CASE_INSENSITIVE),
                    query
            ));
            setStyle("-fx-padding: 0 0 0 5");

            setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    viewModel.setDataDetail(data.get());
                    if (e.isControlDown()) {
                        viewModel.goTo(data.get());
                    }
                    e.consume();
                }
            });
            cursorProperty().bind(Bindings.createObjectBinding(
                    () -> data.get() == null ? Cursor.DEFAULT : Cursor.HAND,
                    data
            ));

            this.holder = new HBox();
            this.holder.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(final Data data, final boolean empty) {
            super.updateItem(data, empty);

            this.data.set(data);

            final Optional<TextFlow> maybeTextFlow = visit(data);

            if (empty || data == null || maybeTextFlow.isEmpty()) {
                setText(null);
                setGraphic(null);
            } else {
                textFlow = maybeTextFlow.get();
                textFlow.setMaxHeight(USE_PREF_SIZE);
                textFlow.maxWidthProperty().bind(widthProperty().subtract(20));
                holder.getChildren().setAll(textFlow);
                setGraphic(holder);
            }
        }

        @Override
        public Optional<TextFlow> visit(final UpperInformationRegion uir) {
            return Optional.of(new TextFlow(textFlowHighlighter.textFlows("%m (%r)", pattern.get(), uir.getIcao(), uir.getName())));
        }

        @Override
        public Optional<TextFlow> visit(final FlightInformationRegionBoundary firb) {
            final FlightInformationRegion fir = firb
                    .getFlightInformationRegions()
                    .stream()
                    .filter(e -> StringUtils.containsIgnoreCase(e.getName(), query.get()))
                    .findFirst()
                    .orElse(firb.getFlightInformationRegions().isEmpty() ? null : firb.getFlightInformationRegions()
                                                                                      .get(0));

            final String firName = fir != null ? fir.getName() : "?";

            return Optional.of(new TextFlow(textFlowHighlighter.textFlows("%m (%r)", pattern.get(), firb.getIcao(), firName)));
        }

        @Override
        public Optional<TextFlow> visit(final Client client) {
            return Optional.of(new TextFlow(textFlowHighlighter.textFlows("%m (%r)", pattern.get(), client.getCallsign(), client
                    .getRealName())));
        }

        @Override
        public Optional<TextFlow> visit(final Airport airport) {
            final String name = airport
                    .getNames()
                    .stream()
                    .filter(e -> StringUtils.containsIgnoreCase(e.get(), query.get()))
                    .findFirst()
                    .map(ObservableObjectValue::get)
                    .orElse(airport.getNames().get(0).get());

            return Optional.of(new TextFlow(textFlowHighlighter.textFlows("%m (%r)", pattern.get(), airport.getIcao(), name)));
        }
    }
}
