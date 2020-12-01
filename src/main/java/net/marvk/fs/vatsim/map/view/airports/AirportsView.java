package net.marvk.fs.vatsim.map.view.airports;

import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.view.table.AbstractTableView;

public class AirportsView extends AbstractTableView<AirportsViewModel, Airport> {
    @Override
    public void initialize() {
        super.initialize();
        addColumn("ICAO", "icao");
//        addColumn("Name", "name");
        enableFilter();
    }
}
