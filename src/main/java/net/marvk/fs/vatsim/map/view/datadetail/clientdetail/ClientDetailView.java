package net.marvk.fs.vatsim.map.view.datadetail.clientdetail;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailPane;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientDetailView extends DataDetailSubView<ClientDetailViewModel, Client> {
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

                    return "%s (%s)".formatted(FORMATTER.format(client.getLogonTime()), onlineForString(client.getLogonTime()));
                },
                client.logonTimeProperty()
        ));
        server.textProperty().bind(client.serverProperty());
        cid.textProperty().bind(client.cidStringProperty());
        realName.textProperty().bind(client.realNameProperty());
        headerLabel.textProperty().bind(client.callsignProperty());
    }

    @FXML
    private void openStats() {
        viewModel.openStats();
    }

    @FXML
    private void openStream() {
        viewModel.openStream();
    }
}
