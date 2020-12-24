package net.marvk.fs.vatsim.map.view.datatable.controllerstable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.data.ControllerType;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.datatable.clientstable.AbstractClientsTableView;

public class ControllersTableView extends AbstractClientsTableView<ControllersTableViewModel, Controller> {
    @Inject
    public ControllersTableView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }

    @Override
    protected void initializeColumns() {
        super.initializeColumns();

        this.<ControllerType>newColumnBuilder()
                .title("Type")
                .objectObservableValueFactory(Controller::controllerTypeProperty)
                .toStringMapper(Enum::toString)
                .sortable()
                .mono(true)
                .build();

        this.<String>newColumnBuilder()
                .title("Frequncy")
                .stringObservableValueFactory(Controller::frequencyProperty)
                .sortable()
                .mono(true)
                .build();
    }
}
