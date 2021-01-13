package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;

public class RatingsLoader {
    private final VatsimApi api;

    @Inject
    public RatingsLoader(final VatsimApi api) {
        this.api = api;
    }

    public void loadRatings() throws VatsimApiException {
        api.data().getControllerRatings().forEach(ControllerRating::of);
        api.data().getPilotRatings().forEach(PilotRating::of);
    }
}
