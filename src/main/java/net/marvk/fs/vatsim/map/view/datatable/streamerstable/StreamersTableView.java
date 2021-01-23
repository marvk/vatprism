package net.marvk.fs.vatsim.map.view.datatable.streamerstable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Client;
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

        this.<String>newColumnBuilder()
                .title("Twitch Username")
                .objectObservableValueFactory(e -> e.getUrls().twitchUrlProperty())
                .toStringMapper(e -> e.replaceAll("(?:www\\.)?twitch\\.tv/", ""))
                .sortable()
                .widthFactor(2.0)
                .build();
    }
}
