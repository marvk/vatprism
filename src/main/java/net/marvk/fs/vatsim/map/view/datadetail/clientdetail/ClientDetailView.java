package net.marvk.fs.vatsim.map.view.datadetail.clientdetail;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.Airline;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailPane;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubView;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Log4j2
public class ClientDetailView extends DataDetailSubView<ClientDetailViewModel, Client> {
    @FXML
    private FontIcon questionMarkIcon;
    @FXML
    private HBox headerLabelContainer;
    @FXML
    private Label headerLabel;
    @FXML
    private StackPane headerPane;
    @FXML
    private DataDetailPane root;
    @FXML
    private Label realName;
    @FXML
    private Label cid;
    @FXML
    private Label onlineSince;
    @FXML
    private Label server;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mmVV");

    private AirlineInformationTooltip airlineInformationTooltip;

    @Override
    protected List<StringProperty> stringProperties() {
        return List.of(root.headerTextProperty());
    }

    @Override
    protected List<Label> labels() {
        return List.of(
                realName,
                cid,
                onlineSince,
                server
        );
    }

    @Override
    public void initialize() {
        super.initialize();
        airlineInformationTooltip = new AirlineInformationTooltip();
        viewModel.twitchStreamProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                headerPane.getStyleClass().add("twitch-container");
            } else {
                headerPane.getStyleClass().remove("twitch-container");
            }
        });
    }

    @Override
    protected void setData(final Client client) {
        onlineSince.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    if (client.logonTimeProperty() == null) {
                        return "";
                    }

                    return "%s (%s)".formatted(FORMATTER.format(client.getLogonTime()), onlineForString(client.getLogonTime(), ZonedDateTime
                            .now(ZoneId.of("Z"))));
                },
                client.logonTimeProperty()
        ));
        server.textProperty().bind(client.serverProperty());
        cid.textProperty().bind(client.cidStringProperty());
        realName.textProperty().bind(client.realNameProperty());
        headerLabel.textProperty().bind(client.callsignProperty());
        if (client instanceof Pilot) {
            final Airline airline = ((Pilot) client).getAirline();

            if (airline != null) {
                installTooltip(airline);
            } else {
                uninstallTooltip();
            }
        }
    }

    private void installTooltip(final Airline airline) {
        if (!headerLabelContainer.getChildren().contains(questionMarkIcon)) {
            headerLabelContainer.getChildren().add(1, questionMarkIcon);
        }
        airlineInformationTooltip.setFromAirline(airline);
        addIfNotContains(questionMarkIcon.getStyleClass(), "question-mark-icon-active");
        addIfNotContains(headerLabel.getStyleClass(), "header-label-active");
        Tooltip.install(headerLabelContainer, airlineInformationTooltip);
    }

    private void uninstallTooltip() {
        headerLabelContainer.getChildren().remove(questionMarkIcon);
        questionMarkIcon.getStyleClass().remove("question-mark-icon-active");
        headerLabel.getStyleClass().remove("header-label-active");
        Tooltip.uninstall(headerLabelContainer, airlineInformationTooltip);
    }

    private static <T> void addIfNotContains(final Collection<T> collection, final T t) {
        if (!collection.contains(t)) {
            collection.add(t);
        }
    }

    @Override
    protected void clear(final Client oldValue) {
        super.clear(oldValue);
        uninstallTooltip();
    }

    @FXML
    private void openStats() {
        viewModel.openStats();
    }

    @FXML
    private void openStream() {
        viewModel.openStream();
    }

    private class AirlineInformationTooltip extends Tooltip {
        private final GridPane grid = new GridPane();
        private final Row name = createRow(resourceBundle.getString("detail.client.airline"), variableWidthLabel(null));
        private final Row icao = createRow(resourceBundle.getString("common.icao"), monoLabel(null));
        private final Row callsign = createRow(resourceBundle.getString("detail.client.callsign"), monoLabel(null));
        private final Row country = createRow(resourceBundle.getString("detail.client.country"), variableWidthLabel(null));

        public AirlineInformationTooltip() {
            grid.setHgap(10);
            setShowDelay(Duration.millis(50));
            setShowDuration(Duration.INDEFINITE);
            setGraphic(grid);
        }

        private Row createRow(final String airline, final Label value) {
            final Row row = new Row(
                    grid.getRowConstraints().size(),
                    new RowConstraints(),
                    variableWidthLabel(airline),
                    value
            );

            row.rowConstraints.setMinHeight(0);

            grid.getRowConstraints().add(row.rowConstraints);
            grid.add(row.key, 0, row.index);
            grid.add(row.value, 1, row.index);

            return row;
        }

        public void setFromAirline(final Airline airline) {
            this.name.setValueText(airline.getName() + aliasAppendage(airline.getAlias()));
            this.icao.setValueText(airline.getIcao());
            this.callsign.setValueText(airline.getCallsign());
            this.country.setValueText(airline.getCountry());
        }
    }

    private static Label variableWidthLabel(final String s) {
        final Label result = new Label(s);
        result.getStyleClass().add("variable-width");
        return result;
    }

    private static Label monoLabel(final String text) {
        final Label result = new Label(text);
        result.getStyleClass().add("mono");
        return result;
    }

    private static String aliasAppendage(final String alias) {
        return (alias != null && !alias.isEmpty()) ? " (%s)".formatted(alias) : "";
    }

    @Data
    private static class Row {
        private final int index;
        private final RowConstraints rowConstraints;
        private final Label key;
        private final Label value;

        public void setValueText(final String s) {
            if (s == null) {
                rowConstraints.setMaxHeight(0);
                key.setVisible(false);
                value.setVisible(false);
            } else {
                rowConstraints.setMaxHeight(Tooltip.USE_COMPUTED_SIZE);
                key.setVisible(true);
                value.setVisible(true);
                value.setText(s);
            }
        }
    }
}
