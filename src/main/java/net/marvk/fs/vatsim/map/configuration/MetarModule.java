package net.marvk.fs.vatsim.map.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import net.marvk.fs.vatsim.map.data.*;

public class MetarModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MetarDeserializer.class).annotatedWith(Names.named("vatsimApiMetarDeserializer"))
                                     .toInstance(new VatsimApiMetarDeserializer());
        bind(MetarApi.class).to(VatsimMetarApi.class).in(Singleton.class);
        bind(MetarService.class).to(SimpleMetarService.class).in(Singleton.class);
    }
}
