package net.marvk.fs.vatsim.map.data;

import com.google.inject.Singleton;
import net.marvk.fs.vatsim.api.data.VatsimClientType;

@Singleton
public class ClientTypeMapper {
    public ClientType map(final VatsimClientType vatsimClientType) {
        return switch (vatsimClientType) {
            case PILOT -> ClientType.PILOT;
            case CONTROLLER -> ClientType.CONTROLLER;
            case ATIS -> ClientType.ATIS;
        };
    }
}
