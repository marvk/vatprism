package net.marvk.fs.vatsim.map.view.about;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectResourceBundle;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.marvk.fs.vatsim.map.data.Dependency;
import net.marvk.fs.vatsim.map.view.ListNoneSelectionModel;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AboutView implements FxmlView<AboutViewModel> {
    @FXML
    private HBox licenseInformationHolder;

    @FXML
    private Label licenseQuestionMark;

    @FXML
    private Label version;

    @FXML
    private ListView<Dependency> dependenciesList;

    @FXML
    private Label createdBy;

    @InjectViewModel
    private AboutViewModel viewModel;

    @InjectResourceBundle
    private ResourceBundle resourceBundle;

    public void initialize() {
        appendLicenseInformationLabels();

        setVersion();

        dependenciesList.setItems(viewModel.dependencies());
        dependenciesList.setCellFactory(param -> new DependencyListCell());
        dependenciesList.setSelectionModel(new ListNoneSelectionModel<>());
        dependenciesList.setFocusTraversable(false);
        version.requestFocus();
        final String createdByText = "Copyright © 2020-%s Marvin Kuhnke".formatted(LocalDateTime.now().getYear());
        createdBy.setText(createdByText);
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(licenseQuestionMarkString().formatted(createdByText));
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(400);
        tooltip.setAutoHide(false);
        tooltip.setShowDuration(Duration.INDEFINITE);
        licenseQuestionMark.setTooltip(tooltip);
    }

    private void appendLicenseInformationLabels() {
        final String licensePlaceholder = "{license}";
        final String[] tokens = resourceBundle
                .getString("about.licensed_under")
                .split("(?<=\\%s)|(?=\\%s)".formatted(licensePlaceholder, licensePlaceholder));

        final List<Label> labels = Arrays
                .stream(tokens)
                .map(e -> {
                    final Label result;
                    if (e.equals(licensePlaceholder)) {
                        result = new Label("GNU AGPLv3");
                        result.setOnMouseClicked(event -> openLicensePage());
                        result.getStyleClass().add("hl-white");
                    } else {
                        result = new Label(e);
                        result.getStyleClass().add("white");
                    }
                    return result;
                })
                .collect(Collectors.toList());

        licenseInformationHolder.getChildren().addAll(0, labels);
    }

    private static String licenseQuestionMarkString() {
        return """
                VATprism is a data explorer for VATSIM, the Virtual Air Traffic Simulation Network. VATprism allows users to explore available ATC services, connected pilots, Airports, Flight and Upper Information Regions and more!
                %s

                This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

                This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

                You should have received a copy of the GNU General Public License along with this program. If not, see https://www.gnu.org/licenses/.
                            """;
    }

    private void setVersion() {
        final String versionString = viewModel.getVersion();
        if (versionString != null) {
            this.version.setText(versionString);
        }
    }

    @FXML
    private void openIssuePage() {
        viewModel.openIssuePage();
    }

    @FXML
    private void openLicensePage() {
        viewModel.openLicensePage();
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
                name.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        viewModel.openDependencyUrlInBrowser(item);
                        e.consume();
                    }
                });
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
