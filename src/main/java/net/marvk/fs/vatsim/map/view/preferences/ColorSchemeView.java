package net.marvk.fs.vatsim.map.view.preferences;

import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import net.marvk.fs.vatsim.map.data.ColorScheme;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

public class ColorSchemeView extends VBox implements JavaView<ColorSchemeViewModel> {
    @InjectViewModel
    private ColorSchemeViewModel viewModel;

    public void initialize() {
        setSpacing(10);

        final ComboBox<ColorScheme> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(250);
        comboBox.setItems(viewModel.colorSchemes());
        comboBox.setCellFactory(param -> new ColorSchemeListCell());
        comboBox.setButtonCell(new ColorSchemeListCell());
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> comboBox.getSelectionModel().clearSelection());
                viewModel.set(newValue);
            }
        });

        final TextField textField = new TextField();
        textField.setPrefWidth(150);
        textField.setPromptText("Color Scheme Name");

        final Button create = new Button();
        create.textProperty().bind(Bindings.createStringBinding(
                () -> viewModel.isUpdating() ? "Update Color Scheme" : "Create Color Scheme",
                viewModel.updatingProperty()
        ));
        create.setOnAction(e -> viewModel.saveCurrent());
        create.disableProperty().bind(viewModel.nameInputProperty().isEmpty());

        viewModel.nameInputProperty().bind(textField.textProperty());

        final Label warning = new Label("This feature is experimental.");
        warning.setStyle("-fx-text-fill: darkred; -fx-font-weight: bold");
        final Label explanation = new Label("""
                Selecting a Scheme from the combo box below will override ALL colors with that schemes colors.
                This will override colors you have set.
                To save your own color scheme, enter a name and press the "Create Color Scheme" button.""");

        final Label outdatedSchemes = new Label("""
                You have outdated color schemes.
                Save them again in order to save all newly added values.
                """);
        outdatedSchemes.setStyle("-fx-text-fill: darkred;");

        final HBox outdatedSchemesContainer = new HBox(5, generateAlertIcon(), outdatedSchemes);
        outdatedSchemesContainer.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(warning, explanation, comboBox, textField, create, outdatedSchemesContainer);

        viewModel.colorSchemes().addListener((ListChangeListener<ColorScheme>) c ->
                outdatedSchemesContainer.setVisible(viewModel.colorSchemes().stream().anyMatch(ColorScheme::isOutdated))
        );
    }

    private class ColorSchemeListCell extends ListCell<ColorScheme> {
        private Node graphic;
        private Label label;
        private Button button;
        private HBox warningHolder;

        @Override
        protected void updateItem(final ColorScheme item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(graphic(item));
            }
        }

        private Node graphic(final ColorScheme colorScheme) {
            if (graphic == null) {
                button = new Button("Delete");
                final Region region = new Region();
                region.setPrefWidth(0);
                region.setPrefHeight(0);
                HBox.setHgrow(region, Priority.ALWAYS);
                final FontIcon warningIcon = generateAlertIcon();
                final Label warningLabel = new Label("Outdated");
                warningHolder = new HBox(3, warningLabel, warningIcon);
                warningLabel.setStyle("""
                        -fx-text-fill: darkred;
                        -fx-font-size: 10;
                        """);
                warningHolder.setAlignment(Pos.CENTER);

                label = new Label();
                final HBox container = new HBox(5, button, label, region, warningHolder);
                container.setPadding(new Insets(0, 10, 0, 0));
                container.setAlignment(Pos.CENTER_LEFT);
                this.graphic = container;
            }
            button.setOnMousePressed(e -> viewModel.delete(colorScheme));
            label.setText(colorScheme.getName());
            warningHolder.setVisible(colorScheme.isOutdated());
            return graphic;
        }

    }

    private static FontIcon generateAlertIcon() {
        final FontIcon warningIcon = FontIcon.of(Octicons.ALERT_16);
        warningIcon.setStyle(warningIcon.getStyle() + """
                -fx-icon-color: darkred;
                -fx-icon-size: 16;
                """);
        return warningIcon;
    }
}
