package net.marvk.fs.vatsim.map.view.airports;

import com.google.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import net.marvk.fs.vatsim.map.view.table.AbstractTableView;
import org.apache.commons.lang3.StringUtils;

public class AirportsView extends AbstractTableView<AirportsViewModel, Airport> {
    @Inject
    public AirportsView(final TextFlowHighlighter textFlowHighlighter) {
        super(textFlowHighlighter);
    }

    @Override
    public void initialize() {
        super.initialize();
        addColumnWithStringFactory("ICAO", Airport::icaoProperty, true);
        addColumnWithStringFactory("Name", AirportsView::airportName, false);
    }

    private static StringProperty airportName(final Airport airport, final String query) {
        return new SimpleStringProperty(airport
                .getNames()
                .stream()
                .filter(e -> StringUtils.containsIgnoreCase(e, query))
                .findFirst()
                .orElse(airport.getNames().get(0))
        );
    }
}
