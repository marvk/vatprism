package net.marvk.fs.vatsim.map.view.detailsubview;

import javafx.fxml.FXML;
import net.marvk.fs.vatsim.map.data.Data;

public abstract class DataDetailSubView<DataDetailViewModel extends DataDetailSubViewModel<ViewModel>, ViewModel extends Data> extends DetailSubView<DataDetailViewModel, ViewModel> {
    @FXML
    public void goTo() {
        viewModel.goTo();
    }
}
