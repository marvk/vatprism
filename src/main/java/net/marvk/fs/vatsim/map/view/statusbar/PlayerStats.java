package net.marvk.fs.vatsim.map.view.statusbar;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.Controller;

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
            switch (client.getClientType()) {
                case PILOT -> pilots++;
                case CONTROLLER, ATIS -> {
                    final Controller controller = (Controller) client;
                    switch (controller.getControllerType()) {
                        case NONE -> unknown++;
                        case OBS -> observers++;
                        default -> controllers++;
                    }
                }
                default -> unknown++;
            }
        }

        return new PlayerStats(pilots, controllers, observers, unknown);
    }
}
