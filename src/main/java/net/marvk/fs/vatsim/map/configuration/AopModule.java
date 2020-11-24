package net.marvk.fs.vatsim.map.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import net.marvk.fs.vatsim.map.aop.LifecycleLogger;
import net.marvk.fs.vatsim.map.aop.LogLifecycle;

public class AopModule extends AbstractModule {
    @Override
    protected void configure() {
        bindInterceptor(
                Matchers.any(),
                Matchers.annotatedWith(LogLifecycle.class),
                new LifecycleLogger()
        );
    }
}
