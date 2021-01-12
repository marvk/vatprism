package net.marvk.fs.vatsim.map.view.about;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.marvk.fs.vatsim.map.data.Dependency;
import net.marvk.fs.vatsim.map.view.ListNoneSelectionModel;

public class AboutView implements FxmlView<AboutViewModel> {
    @FXML
    public Label version;

    @FXML
    public ListView<Dependency> dependenciesList;

    @InjectViewModel
    private AboutViewModel viewModel;

    public void initialize() {
        setVersion();

        dependenciesList.setItems(viewModel.dependencies());
        dependenciesList.setCellFactory(param -> new DependencyListCell());
        dependenciesList.setSelectionModel(new ListNoneSelectionModel<>());
        dependenciesList.setFocusTraversable(false);
        version.requestFocus();
    }

    private void setVersion() {
        final String versionString = viewModel.getVersion();
        if (versionString != null) {
            this.version.setText(versionString);
        }
    }

    private class DependencyListCell extends ListCell<Dependency> {
        @Override
        protected void updateItem(final Dependency item, final boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setGraphic(getPane(item));
            }
        }

        private Pane pane;

        private Label name;
        private Label version;
        private Label license;

        private Pane getPane(final Dependency item) {
            if (pane == null) {
                name = new Label();
                version = new Label();
                license = new Label();

                final HBox hBox = new HBox(name, version);
                hBox.setSpacing(5);
                pane = new VBox(hBox, license);
            }

            name.setText(item.getProjectName());
            final String url = item.getProjectUrl();
            if (url != null && !url.isBlank()) {
                if (!name.getStyleClass().contains("hl")) {
                    name.getStyleClass().add("hl");
                }
                name.setOnMouseClicked(e -> viewModel.openDependencyUrlInBrowser(item));
            } else {
                name.getStyleClass().remove("hl");
                name.setOnMouseClicked(null);
            }
            version.setText(item.getVersion());
            license.setText(item.getLicenseName());

            return pane;
        }
    }

}
