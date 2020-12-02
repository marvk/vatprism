package net.marvk.fs.vatsim.map.view.toolbar;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ToolBarView implements FxmlView<ToolBarViewModel> {
    @InjectViewModel
    private ToolBarViewModel viewModel;

    @FXML
    private void refresh(final ActionEvent actionEvent) {
        viewModel.refresh();
    }
}
