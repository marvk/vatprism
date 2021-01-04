package net.marvk.fs.vatsim.map.view;

import net.marvk.fs.vatsim.map.data.Pilot;

public class EtaToStringMapper {
    public String map(final Pilot.Eta eta) {
        if (eta.isArriving()) {
            return "ARRIVING";
        } else if (eta.isDeparting()) {
            return "DEPARTING";
        } else if (eta.isUnknown()) {
            return "UNKNOWN";
        } else {
            return "%02d:%02d".formatted(eta.getDuration().toHoursPart(), eta.getDuration().toMinutesPart());
        }
    }
}
