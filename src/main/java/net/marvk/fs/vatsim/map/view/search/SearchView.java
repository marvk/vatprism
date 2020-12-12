package net.marvk.fs.vatsim.map.view.search;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.data.OptionalDataVisitor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SearchView implements FxmlView<SearchViewModel> {
    private static final Font BOLD = Font.font(
            Font.getDefault().getFamily(),
            FontWeight.BOLD,
            Font.getDefault().getSize()
    );

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

    @InjectViewModel
    private SearchViewModel viewModel;

    public void initialize() {
        viewModel.queryProperty().bindBidirectional(searchBox.textProperty());
        searchBox.setOnAction(e -> viewModel.search());

        viewModel.getSearchCommand()
                 .runningProperty()
                 .addListener((observable, oldValue, newValue) -> searchingChanged(newValue));
        resultsHolder.getChildren().clear();
        resultsHolder.visibleProperty()
                     .bind(viewModel.resultsProperty().isNotNull().or(viewModel.getSearchCommand().runningProperty()));

        resultsList.itemsProperty().bind(viewModel.resultsProperty());
        resultsList.setCellFactory(e -> new ResultCell(viewModel.queryProperty()));

        searchBoxHolder.getChildren().addAll(createSearchIcon(), createClearIcon());
    }

    private FontIcon createClearIcon() {
        final FontIcon icon = FontIcon.of(Octicons.X_16);
        StackPane.setMargin(icon, new Insets(0, 3, 0, 0));
        StackPane.setAlignment(icon, Pos.CENTER_RIGHT);
        icon.visibleProperty().bind(searchBox.textProperty().isNotEmpty().or(viewModel.resultsProperty().isNotNull()));
        icon.setCursor(Cursor.HAND);
        icon.setOnMouseClicked(e -> viewModel.clear());
        icon.getStyleClass().add("clear-icon");
        return icon;
    }

    private FontIcon createSearchIcon() {
        final FontIcon icon = FontIcon.of(Octicons.SEARCH_16);
        StackPane.setMargin(icon, new Insets(0, 0, 0, 3));
        StackPane.setAlignment(icon, Pos.CENTER_LEFT);
        icon.visibleProperty().bind(searchBox.textProperty().isEmpty().and(searchBox.focusedProperty().not()));
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

    private class ResultCell extends ListCell<Data> implements OptionalDataVisitor<TextFlow> {
        private final ReadOnlyStringProperty query;
        private final ObjectProperty<Pattern> pattern = new SimpleObjectProperty<>();
        private final ObjectProperty<Data> data = new SimpleObjectProperty<>();

        public ResultCell(final ReadOnlyStringProperty query) {
            this.query = query;
            this.pattern.bind(Bindings.createObjectBinding(
                    () -> Pattern.compile(query.get(), Pattern.CASE_INSENSITIVE),
                    query
            ));
            setStyle("-fx-padding: 0 0 0 5");

            setOnMouseClicked(e -> viewModel.setDataDetail(data.get()));
            cursorProperty().bind(Bindings.createObjectBinding(
                    () -> data.get() == null ? Cursor.DEFAULT : Cursor.HAND,
                    data
            ));
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
                setGraphic(maybeTextFlow.get());
            }
        }

        @Override
        public Optional<TextFlow> visit(final Client client) {
            return Optional.of(
                    new TextFlow(createHighlightedText(client.getCallsign()).toArray(Text[]::new))
            );
        }

        private List<Text> createHighlightedText(final String s) {
            final Pattern pattern = this.pattern.get();

            final List<MatchResult> matches = pattern.matcher(s).results().collect(Collectors.toList());

            int index = 0;

            final List<Text> result = new ArrayList<>();
            for (final MatchResult match : matches) {
                final String prefix = s.substring(index, match.start());
                if (!prefix.isBlank()) {
                    result.add(defaultText(prefix));
                }
                result.add(highlightedText(s.substring(match.start(), match.end())));
                index = match.end();
            }

            if (index < s.length()) {
                result.add(defaultText(s.substring(index)));
            }

            return result;
        }

        private Text highlightedText(final String s) {
            final Text text = new Text(s);
            text.setStyle("-fx-fill: -vatsim-text-color-light");
            text.setFont(BOLD);
            return text;
        }

        private Text defaultText(final String s) {
            final Text text = new Text(s);
            text.setStyle("-fx-fill: -vatsim-text-color");
            return text;
        }
    }
}
