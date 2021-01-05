package net.marvk.fs.vatsim.map.view.datatable.flightinformationregionboundariestable;

import com.google.inject.Inject;
import javafx.beans.property.ReadOnlyStringProperty;
import net.marvk.fs.vatsim.map.data.Country;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.datatable.AbstractTableView;

public class FlightInformationRegionBoundariesTableView extends AbstractTableView<FlightInformationRegionBoundariesTableViewModel, FlightInformationRegionBoundary> {
    @Inject
    public FlightInformationRegionBoundariesTableView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }

    @Override
    protected void initializeColumns() {
        this.<String>newColumnBuilder()
                .title("ICAO")
                .stringObservableValueFactory(FlightInformationRegionBoundary::icaoProperty)
                .sortable()
                .mono(true)
                .build();

        this.<String>newColumnBuilder()
                .title("Name")
                .stringObservableValueFactory(FlightInformationRegionBoundariesTableView::nameProperty)
                .sortable()
                .mono(true)
                .build();

        this.<Boolean>newColumnBuilder()
                .title("Oceanic")
                .objectObservableValueFactory(FlightInformationRegionBoundary::oceanicProperty)
                .toStringMapper(e -> e ? "Yes" : "")
                .sortable()
                .mono(true)
                .build();

        this.<Number>newColumnBuilder()
                .title("# of Controllers")
                .objectObservableValueFactory(e -> e.getControllers().sizeProperty())
                .toStringMapper(FlightInformationRegionBoundariesTableView::emptyIfZero)
                .sortable()
                .mono(true)
                .build();

        this.<Country>newColumnBuilder()
                .title("Country")
                .objectObservableValueFactory(FlightInformationRegionBoundary::countryProperty)
                .toStringMapper(Country::getName)
                .sortable()
                .build();
    }

    private static ReadOnlyStringProperty nameProperty(final FlightInformationRegionBoundary e) {
        if (e.getFlightInformationRegions().isEmpty()) {
            return EMPTY;
        }

        return e.getFlightInformationRegions().get(0).nameProperty();
    }
}
