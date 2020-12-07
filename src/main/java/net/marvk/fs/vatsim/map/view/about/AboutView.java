package net.marvk.fs.vatsim.map.view.about;

import de.saxsys.mvvmfx.FxmlView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class AboutView implements FxmlView<AboutViewController> {
    @FXML
    public Label version;

    public void initialize() {
        version.setText(getClass().getPackage().getImplementationVersion());
    }

    @FXML
    private void openSourceSoftwareDialog(final MouseEvent event) {
        System.out.println("oss");
    }
}
