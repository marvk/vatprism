package net.marvk.fs.vatsim.map.view.datatable.airportstable;

import com.google.inject.Inject;
import javafx.beans.value.ObservableStringValue;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.map.GeomUtil;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.datatable.AbstractTableView;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

public class AirportsTableView extends AbstractTableView<AirportsTableViewModel, Airport> {
    @Inject
    public AirportsTableView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }

    @Override
    protected void initializeColumns() {
        this.<String>newColumnBuilder()
                .title("ICAO")
                .stringObservableValueFactory(Airport::icaoProperty)
                .sortable()
                .mono(true)
                .build();

        this.<String>newColumnBuilder()
                .title("Name")
                .stringObservableValueFactory(AirportsTableView::airportName)
                .sortable()
                .mono(true)
                .build();

        this.<Point2D>newColumnBuilder()
                .title("Longitude")
                .objectObservableValueFactory(Airport::positionProperty)
                .toStringMapper(e -> GeomUtil.formatLon(e.getY()))
                .sortable(Comparator.comparingDouble(Point2D::getX))
                .mono(true)
                .build();

        this.<Point2D>newColumnBuilder()
                .title("Latitude")
                .objectObservableValueFactory(Airport::positionProperty)
                .toStringMapper(e -> GeomUtil.formatLat(e.getX()))
                .sortable(Comparator.comparingDouble(Point2D::getY))
                .mono(true)
                .build();
    }

    private static ObservableStringValue airportName(final Airport airport, final ObservableStringValue query) {
        return airport
                .getNames()
                .stream()
                .filter(e -> StringUtils.containsIgnoreCase(e.get(), query.get()))
                .findFirst()
                .orElse(airport.getNames().get(0));

    }
}