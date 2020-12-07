package net.marvk.fs.vatsim.map.view.clientdetail;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubView;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubViewModel;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class ClientDetailView extends DataDetailSubView<DataDetailSubViewModel<Client>, Client> {
    @FXML
    private Label callsign;
    @FXML
    private Label onlineSince;
    @FXML
    private Label server;
    @FXML
    private Pane root;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mmVV");

    @Override
    protected List<TextArea> textAreas() {
        return Collections.emptyList();
    }

    @Override
    protected List<Label> labels() {
        return List.of(
                onlineSince,
                server,
                callsign
        );
    }

    @Override
    protected void setData(final Client client) {
        onlineSince.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    if (client.logonTimeProperty() == null) {
                        return "";
                    }

                    return FORMATTER.format(client.getLogonTime());
                },
                client.logonTimeProperty()
        ));
        server.textProperty().bind(client.serverProperty());
        callsign.textProperty().bind(client.callsignProperty());
    }
}
