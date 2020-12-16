package net.marvk.fs.vatsim.map.view.main;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class MainView implements FxmlView<MainViewModel> {
    @FXML
    private VBox container;

    @InjectViewModel
    private MainViewModel viewModel;

    public void initialize() {
        container.styleProperty().bind(viewModel.styleProperty());
    }
}
