package net.marvk.fs.vatsim.map.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import net.marvk.fs.vatsim.map.version.HttpVersionApi;
import net.marvk.fs.vatsim.map.version.UpdateChannel;
import net.marvk.fs.vatsim.map.version.VersionApi;

import java.time.Duration;

public class VersionModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(VersionApi.class).to(HttpVersionApi.class).in(Singleton.class);
        bind(UpdateChannel.class).toInstance(UpdateChannel.STABLE);
        bind(String.class).annotatedWith(Names.named("versionApiUrl"))
                          .toInstance("http://version.vatprism.org:6300?version=%s&channel=%s");
        bind(Duration.class).annotatedWith(Names.named("versionApiTimeout")).toInstance(Duration.ofMillis(2500));
    }
}
