package net.marvk.fs.vatsim.map.view.datatable.controllerstable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.*;
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

        final LocationToStringVisitor locationToStringVisitor = new LocationToStringVisitor();
        final LocationTypeToStringVisitor locationTypeToStringVisitor = new LocationTypeToStringVisitor();

        this.<ControllerRating>newColumnBuilder()
            .titleKey("table.controllers.rating")
            .objectObservableValueFactory(Controller::ratingProperty)
            .toStringMapper(ControllerRating::getShortName)
            .sortable()
            .mono(true)
            .widthFactor(0.7)
            .build();

        this.<Data>newColumnBuilder()
            .titleKey("table.controllers.location")
            .objectObservableValueFactory(Controller::workingLocationProperty)
            .toStringMapper(locationTypeToStringVisitor::visit, true)
            .sortable()
            .mono(false)
            .widthFactor(0.7)
            .build();

        this.<Data>newColumnBuilder()
            .titleKey("common.icao")
            .objectObservableValueFactory(Controller::workingLocationProperty)
            .toStringMapper(locationToStringVisitor::visit, true)
            .sortable()
            .mono(true)
            .widthFactor(0.7)
            .build();

        this.<ControllerType>newColumnBuilder()
            .titleKey("table.controllers.type")
            .objectObservableValueFactory(Controller::controllerTypeProperty)
            .toStringMapper(Enum::toString)
            .sortable()
            .mono(true)
            .widthFactor(0.5)
            .build();

        this.<String>newColumnBuilder()
            .titleKey("table.controllers.frequency")
            .stringObservableValueFactory(Controller::frequencyProperty)
            .sortable()
            .mono(true)
            .widthFactor(0.7)
            .build();
    }

    private class LocationTypeToStringVisitor extends DefaultingDataVisitor<String> {
        public LocationTypeToStringVisitor() {
            super(resourceBundle.getString("common.unknown"));
        }

        @Override
        public String visit(final Airport airport) {
            return resourceBundle.getString("common.airport");
        }

        @Override
        public String visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
            return resourceBundle.getString("common.fir");
        }

        @Override
        public String visit(final UpperInformationRegion upperInformationRegion) {
            return resourceBundle.getString("common.uir");
        }
    }

    private class LocationToStringVisitor extends DefaultingDataVisitor<String> {
        public LocationToStringVisitor() {
            super(resourceBundle.getString("common.unknown"));
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
