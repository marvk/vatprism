package net.marvk.fs.vatsim.map.view.datatable.pilotstable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.datatable.clientstable.AbstractClientsTableView;

public class PilotsTableView extends AbstractClientsTableView<PilotsTableViewModel, Pilot> {
    @Inject
    public PilotsTableView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }
}
