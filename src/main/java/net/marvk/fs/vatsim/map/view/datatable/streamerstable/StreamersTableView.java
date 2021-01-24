package net.marvk.fs.vatsim.map.view.datatable.streamerstable;

import com.google.inject.Inject;
import javafx.scene.control.TableRow;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.ClientType;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.datatable.clientstable.AbstractClientsTableView;

public class StreamersTableView extends AbstractClientsTableView<StreamersTableViewModel, Client> {
    @Inject
    public StreamersTableView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }

    @Override
    protected void initializeColumns() {
        super.initializeColumns();

        this.<ClientType>newColumnBuilder()
                .title("Type")
                .objectObservableValueFactory(Client::clientTypeProperty)
                .toStringMapper(StreamersTableView::clientTypeToString)
                .sortable()
                .widthFactor(0.8)
                .build();

        this.<String>newColumnBuilder()
                .title("Twitch Username")
                .objectObservableValueFactory(e -> e.getUrls().twitchUrlProperty())
                .toStringMapper(e -> e.replaceAll("(?:www\\.)?twitch\\.tv/", ""))
                .sortable()
                .widthFactor(2.0)
                .build();
    }

    private static String clientTypeToString(final ClientType e) {
        return switch (e) {
            case PILOT -> "Pilot";
            case ATIS, CONTROLLER -> "Controller";
        };
    }

    @Override
    protected void onControlClick(final TableRow<Client> tableRow, final Client client) {
        viewModel.openStream(client);
    }
}
