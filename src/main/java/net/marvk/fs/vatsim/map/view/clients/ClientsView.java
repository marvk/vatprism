package net.marvk.fs.vatsim.map.view.clients;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.table.AbstractTableView;

public class ClientsView extends AbstractTableView<ClientsViewModel, Client> {
    @FXML
    private SplitPane splitPane;

    @InjectContext
    private Context context;

    @Inject
    public ClientsView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }

    @Override
    public void initialize() {
        super.initialize();
        addColumnWithStringFactory("CID", Client::cidProperty, true);
        addColumnWithStringFactory("Callsign", Client::callsignProperty, true);
        addColumnWithStringFactory("Name", Client::realNameProperty, false);
    }
}
