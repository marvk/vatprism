package net.marvk.fs.vatsim.map.view.preloader;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;

public class PreloaderView implements FxmlView<PreloaderViewModel> {
    @FXML
    private ProgressBar progressBar;

    @InjectViewModel
    private PreloaderViewModel viewModel;

    public void initialize() {
        viewModel.progressPropertyWritable().bindBidirectional(progressBar.progressProperty());
    }
}
