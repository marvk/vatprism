package net.marvk.fs.vatsim.map.view.datatable.pilotstable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.GeomUtil;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.Eta;
import net.marvk.fs.vatsim.map.data.FlightRule;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.EtaToStringMapper;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.datatable.clientstable.AbstractClientsTableView;

import java.util.Comparator;

public class PilotsTableView extends AbstractClientsTableView<PilotsTableViewModel, Pilot> {

    @Inject
    public PilotsTableView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }

    @Override
    protected void initializeColumns() {
        super.initializeColumns();

        final EtaToStringMapper etaToStringMapper = new EtaToStringMapper(resourceBundle);

        this.<Airport>newColumnBuilder()
                .titleKey("common.departure")
                .objectObservableValueFactory(e -> e.getFlightPlan().departureAirportProperty())
                .toStringMapper(Airport::getIcao)
                .sortable(Comparator.nullsFirst(Comparator.comparing(Airport::getIcao)))
                .mono(true)
                .widthFactor(0.7)
                .build();

        this.<Airport>newColumnBuilder()
                .titleKey("common.arrival")
                .objectObservableValueFactory(e -> e.getFlightPlan().arrivalAirportProperty())
                .toStringMapper(Airport::getIcao)
                .sortable(Comparator.nullsFirst(Comparator.comparing(Airport::getIcao)))
                .mono(true)
                .widthFactor(0.7)
                .build();

        this.<Airport>newColumnBuilder()
                .titleKey("common.alternate")
                .objectObservableValueFactory(e -> e.getFlightPlan().alternativeAirportProperty())
                .toStringMapper(Airport::getIcao)
                .sortable(Comparator.nullsFirst(Comparator.comparing(Airport::getIcao)))
                .mono(true)
                .widthFactor(0.7)
                .build();

        this.<Number>newColumnBuilder()
                .titleKey("common.distance")
                .objectObservableValueFactory(e -> e.getFlightPlan().totalDistanceProperty())
                .toStringMapper(e -> Double.isNaN(e.doubleValue()) ? null : Math.round(GeomUtil.metersToNauticalMiles(e.doubleValue())) + "NM")
                .sortable(PilotsTableView::compareDistance)
                .mono(true)
                .widthFactor(0.6)
                .build();

        this.<String>newColumnBuilder()
                .titleKey("common.aircraft_type")
                .stringObservableValueFactory(e -> e.getFlightPlan().aircraftProperty())
                .sortable()
                .build();

        this.<FlightRule>newColumnBuilder()
                .titleKey("common.flight_rules")
                .objectObservableValueFactory(e -> e.getFlightPlan().flightRuleProperty())
                .toStringMapper(Enum::toString)
                .sortable()
                .widthFactor(0.85)
                .build();

        this.<Number>newColumnBuilder()
                .titleKey("common.ground_speed")
                .objectObservableValueFactory(Pilot::groundSpeedProperty)
                .toStringMapper(e -> "%dkts".formatted(e.intValue()))
                .sortable()
                .mono(true)
                .widthFactor(0.7)
                .build();

        this.<Number>newColumnBuilder()
                .titleKey("common.altitude")
                .objectObservableValueFactory(Pilot::altitudeProperty)
                .toStringMapper(e -> "%dft".formatted(e.intValue()))
                .sortable()
                .mono(true)
                .widthFactor(0.7)
                .build();

        this.<Eta>newColumnBuilder()
            .titleKey("common.eta")
            .objectObservableValueFactory(Pilot::etaProperty)
            .toStringMapper(etaToStringMapper::map)
            .sortable(Comparator.comparing(Eta::getDuration))
            .mono(true)
            .widthFactor(0.8)
            .build();
    }

    private static int compareDistance(final Number n1, final Number n2) {
        final boolean firstIsNaN = Double.isNaN(n1.doubleValue());
        final boolean secondIsNaN = Double.isNaN(n2.doubleValue());
        if (firstIsNaN && secondIsNaN) {
            return 0;
        } else if (firstIsNaN) {
            return -1;
        } else if (secondIsNaN) {
            return 1;
        } else {
            return Double.compare(n1.doubleValue(), n2.doubleValue());
        }
    }
}
