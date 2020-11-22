package net.marvk.fs.vatsim.map.data;

import lombok.Data;
import net.marvk.fs.vatsim.api.data.VatsimClient;

@Data
public class ClientStatus {
    private final Point position;
    private final Double heading;
    private final Double groundSpeed;

    public static ClientStatus fromVatsimClient(final VatsimClient vatsimClient) {
        return new ClientStatus(
                Point.from(vatsimClient.getLongitude(), vatsimClient.getLatitude()),
                ParseUtil.parseNullSafe(vatsimClient.getHeading(), Double::parseDouble),
                ParseUtil.parseNullSafe(vatsimClient.getGroundSpeed(), Double::parseDouble)
        );
    }
}
