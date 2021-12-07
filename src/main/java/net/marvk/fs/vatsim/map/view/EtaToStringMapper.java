package net.marvk.fs.vatsim.map.view;

import net.marvk.fs.vatsim.map.data.Eta;

import java.util.ResourceBundle;

public class EtaToStringMapper {
    private final ResourceBundle resourceBundle;

    public EtaToStringMapper(final ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public String map(final Eta eta) {
        if (eta.is(Eta.Status.ARRIVING)) {
            return resourceBundle.getString("eta.arriving");
        } else if (eta.is(Eta.Status.DEPARTING)) {
            return resourceBundle.getString("eta.departing");
        } else if (eta.is(Eta.Status.EN_ROUTE)) {
            return "%02d:%02d".formatted(eta.getDuration().toHours(), eta.getDuration().toMinutesPart());
        } else if (eta.is(Eta.Status.GROUND)) {
            return resourceBundle.getString("eta.on_ground");
        } else if (eta.is(Eta.Status.MID_AIR)) {
            return resourceBundle.getString("eta.mid_air");
        } else {
            return resourceBundle.getString("common.unknown_full_caps");
        }
    }
}
