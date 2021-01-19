package net.marvk.fs.vatsim.map.view.filter.filteredclients;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.datatable.clientstable.AbstractClientsTableView;

public class FilteredClientsTableView extends AbstractClientsTableView<FilteredClientsTableViewModel, Client> {
    @Inject
    public FilteredClientsTableView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }

    @Override
    protected void initializeColumns() {
        super.initializeColumns();
    }
}
