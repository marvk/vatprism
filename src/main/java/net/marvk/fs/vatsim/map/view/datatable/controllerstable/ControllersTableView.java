package net.marvk.fs.vatsim.map.view.datatable.controllerstable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.datatable.clientstable.AbstractClientsTableView;

public class ControllersTableView extends AbstractClientsTableView<ControllersTableViewModel, Controller> {

    private final LocationToStringVisitor locationToStringVisitor;
    private final LocationTypeToStringVisitor locationTypeToStringVisitor;

    @Inject
    public ControllersTableView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
        this.locationToStringVisitor = new LocationToStringVisitor();
        this.locationTypeToStringVisitor = new LocationTypeToStringVisitor();
    }

    @Override
    protected void initializeColumns() {
        super.initializeColumns();

        this.<Data>newColumnBuilder()
                .title("Location")
                .objectObservableValueFactory(Controller::workingLocationProperty)
                .toStringMapper(locationTypeToStringVisitor::visit, true)
                .sortable()
                .mono(false)
                .build();

        this.<Data>newColumnBuilder()
                .title("ICAO")
                .objectObservableValueFactory(Controller::workingLocationProperty)
                .toStringMapper(locationToStringVisitor::visit, true)
                .sortable()
                .mono(true)
                .build();

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

    private static class LocationTypeToStringVisitor extends DefaultingDataVisitor<String> {
        public LocationTypeToStringVisitor() {
            super("Unknown");
        }

        @Override
        public String visit(final Airport airport) {
            return "Airport";
        }

        @Override
        public String visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
            return "FIR";
        }

        @Override
        public String visit(final UpperInformationRegion upperInformationRegion) {
            return "UIR";
        }
    }

    private static class LocationToStringVisitor extends DefaultingDataVisitor<String> {
        public LocationToStringVisitor() {
            super("Unknown");
        }

        @Override
        public String visit(final Airport airport) {
            return airport.getIcao();
        }

        @Override
        public String visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
            return flightInformationRegionBoundary.getIcao();
        }

        @Override
        public String visit(final UpperInformationRegion upperInformationRegion) {
            return upperInformationRegion.getIcao();
        }
    }
}
