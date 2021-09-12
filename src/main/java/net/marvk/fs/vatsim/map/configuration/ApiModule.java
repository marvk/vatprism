package net.marvk.fs.vatsim.map.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import net.marvk.fs.vatsim.map.api.HttpVatprismApi;
import net.marvk.fs.vatsim.map.api.VatprismApi;

import java.time.Duration;

public class ApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(VatprismApi.class).to(HttpVatprismApi.class).in(Singleton.class);
        bind(String.class).annotatedWith(Names.named("apiVersionUrl"))
                          .toInstance("https://version.vatprism.org:6300/versionRequest?version=%s&channel=%s");
        bind(String.class).annotatedWith(Names.named("apiThemeUrl"))
                          .toInstance("https://version.vatprism.org:6300/themeChoice?version=%s&theme=%s");
        bind(Duration.class).annotatedWith(Names.named("versionApiTimeout")).toInstance(Duration.ofMillis(2500));
    }
}
