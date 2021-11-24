package net.marvk.fs.vatsim.map.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.time.Duration;

public class LocalApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named("apiVersionUrl"))
                          .toInstance("http://localhost:6300/v1/version?version=%s&channel=%s");
        bind(String.class).annotatedWith(Names.named("apiThemeUrl"))
                          .toInstance("http://localhost:6300/v1/theme?version=%s&theme=%s");
        bind(String.class).annotatedWith(Names.named("apiMotdsUrl"))
                          .toInstance("http://localhost:6300/v1/motd/all?version=%s&focusedHours=%s&totalHours=%s&unfiltered=%s");
        bind(Duration.class).annotatedWith(Names.named("apiTimeout")).toInstance(Duration.ofMillis(2500));
    }
}
