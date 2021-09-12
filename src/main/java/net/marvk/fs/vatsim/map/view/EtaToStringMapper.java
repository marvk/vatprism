package net.marvk.fs.vatsim.map.view;

import net.marvk.fs.vatsim.map.data.Eta;

public class EtaToStringMapper {
    public String map(final Eta eta) {
        if (eta.is(Eta.Status.ARRIVING)) {
            return "ARRIVING";
        } else if (eta.is(Eta.Status.DEPARTING)) {
            return "DEPARTING";
        } else if (eta.is(Eta.Status.EN_ROUTE)) {
            return "%02d:%02d".formatted(eta.getDuration().toHours(), eta.getDuration().toMinutesPart());
        } else if (eta.is(Eta.Status.GROUND)) {
            return "ON GROUND";
        } else if (eta.is(Eta.Status.MID_AIR)) {
            return "MID AIR";
        } else {
            return "UNKNOWN";
        }
    }
}
