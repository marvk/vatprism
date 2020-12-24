package net.marvk.fs.vatsim.map.view.datatable.clientstable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;

public class ClientsTableView extends AbstractClientsTableView<ClientsTableViewModel, Client> {
    @Inject
    public ClientsTableView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }
}
