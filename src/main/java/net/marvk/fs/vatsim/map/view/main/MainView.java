package net.marvk.fs.vatsim.map.view.main;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class MainView implements FxmlView<MainViewModel> {
    @FXML
    private VBox container;

    @InjectViewModel
    private MainViewModel viewModel;

    public void initialize() {
        final IntegerProperty fontSize = viewModel.getPreferences().integerProperty("general.font_size");
        container.styleProperty().bind(Bindings.createStringBinding(
                () -> "-fx-font-size: %spx;".formatted(fontSize.get()),
                fontSize
        ));
    }
}
