package net.marvk.fs.vatsim.map.view.preloader;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PreloaderView implements FxmlView<PreloaderViewModel> {
    @FXML
    private VBox taskHolder;

    @FXML
    private Label error;

    @FXML
    private HBox errorHolder;

    @FXML
    private Label task;

    @FXML
    private ProgressBar progressBar;

    @InjectViewModel
    private PreloaderViewModel viewModel;

    public void initialize() {
        viewModel.progressPropertyWritable().bindBidirectional(progressBar.progressProperty());
        task.textProperty().bind(viewModel.taskDescriptionProperty());
        error.textProperty().bind(viewModel.errorProperty());

        taskHolder.getChildren().setAll(task);

        viewModel.errorProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                taskHolder.getChildren().setAll(errorHolder);
            } else {
                taskHolder.getChildren().setAll(task);
            }
        });
    }

    @FXML
    private void goToIssuePage(final MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            viewModel.goToIssuePage();
        }
    }
}
