package net.marvk.fs.vatsim.map.view.statusbar;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.data.ControllerType;
import net.marvk.fs.vatsim.map.data.Pilot;

import java.util.Collection;

@Data
@AllArgsConstructor
public final class PlayerStats {
    private final int pilots;
    private final int controllers;
    private final int observers;
    private final int unknown;

    public PlayerStats() {
        this(0, 0, 0, 0);
    }

    public static PlayerStats read(final Collection<Client> clients) {
        int pilots = 0;
        int controllers = 0;
        int observers = 0;
        int unknown = 0;

        for (final Client client : clients) {
            if (client instanceof Pilot) {
                pilots++;
            } else if (client instanceof Controller) {
                final ControllerType controllerType = ((Controller) client).getControllerType();
                if (controllerType == ControllerType.OBS) {
                    observers++;
                } else if (controllerType == ControllerType.NONE) {
                    unknown++;
                } else {
                    controllers++;
                }
            } else {
                unknown++;
            }
        }

        return new PlayerStats(pilots, controllers, observers, unknown);
    }
}
