package net.marvk.fs.vatsim.map.view.preferences;

import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import net.marvk.fs.vatsim.map.data.ColorScheme;

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
        comboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> viewModel.set(newValue));

        final TextField textField = new TextField();
        textField.setPrefWidth(150);
        textField.setPromptText("Color Scheme Name");

        final Button create = new Button("Create Color Scheme");
        create.setOnAction(e -> viewModel.saveCurrent(textField.getText()));

        final Label warning = new Label("This feature is experimental.");
        warning.setStyle("-fx-text-fill: darkred; -fx-font-weight: bold");
        final Label explanation = new Label("""
                Selecting a Scheme from the combo box below will override ALL colors with that schemes colors.
                This will override colors you have set.
                To save your own color scheme, enter a name and press the "Create Color Scheme" button.""");

        getChildren().addAll(warning, explanation, comboBox, textField, create);
    }

    private class ColorSchemeListCell extends ListCell<ColorScheme> {

        private Button button;

        @Override
        protected void updateItem(final ColorScheme item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                setText(item.getName());
                setGraphic(button(item));
            }
        }

        private Button button(final ColorScheme colorScheme) {
            if (button == null) {
                button = new Button("Delete");
            }
            button.setOnMousePressed(e -> viewModel.delete(colorScheme));
            return button;
        }
    }
}
