package net.marvk.fs.vatsim.map.view.datatable.upperinformationregionstable;

import com.google.inject.Inject;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.UpperInformationRegion;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.datatable.AbstractTableView;

import java.util.stream.Collectors;

public class UpperInformationRegionsTableView extends AbstractTableView<UpperInformationRegionsTableViewModel, UpperInformationRegion> {
    @Inject
    public UpperInformationRegionsTableView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }

    @Override
    protected void initializeColumns() {
        newColumnBuilder()
                .title("ICAO")
                .stringObservableValueFactory(UpperInformationRegion::icaoProperty)
                .sortable()
                .mono(true)
                .build();

        newColumnBuilder()
                .title("Name")
                .stringObservableValueFactory(UpperInformationRegion::nameProperty)
                .sortable()
                .mono(true)
                .build();

        this.<ObservableList<FlightInformationRegionBoundary>>newColumnBuilder()
                .title("FIRs")
                .objectObservableValueFactory(UpperInformationRegion::getFlightInformationRegionBoundaries)
                .toStringMapper(UpperInformationRegionsTableView::firbIcaos)
                .mono(true)
                .build();

        this.<Number>newColumnBuilder()
                .title("# of Controllers")
                .objectObservableValueFactory(e -> e.getControllers().sizeProperty())
                .toStringMapper(AbstractTableView::emptyIfZero)
                .sortable()
                .mono(true)
                .build();
    }

    private static String firbIcaos(final ObservableList<FlightInformationRegionBoundary> firbs) {
        return firbs.stream().map(FlightInformationRegionBoundary::getIcao).collect(Collectors.joining(", "));
    }
}
