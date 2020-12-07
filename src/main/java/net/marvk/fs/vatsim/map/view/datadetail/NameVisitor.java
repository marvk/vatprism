package net.marvk.fs.vatsim.map.view.datadetail;

import net.marvk.fs.vatsim.map.data.*;

public class NameVisitor extends DefaultingDataVisitor<String> {
    public NameVisitor() {
        super("Unknown");
    }

    @Override
    public String visit(final Controller controller) {
        return "Controller";
    }

    @Override
    public String visit(final UpperInformationRegion upperInformationRegion) {
        return "UIR";
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
    public String visit(final FlightInformationRegion flightInformationRegion) {
        return "FIR";
    }

    @Override
    public String visit(final Pilot pilot) {
        return "Flight";
    }
}
