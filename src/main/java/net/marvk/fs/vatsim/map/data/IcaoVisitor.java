package net.marvk.fs.vatsim.map.data;

public class IcaoVisitor extends DefaultingDataVisitor<String> {
    public IcaoVisitor(final String defaultValue) {
        super(defaultValue);
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

