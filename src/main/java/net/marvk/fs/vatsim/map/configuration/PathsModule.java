package net.marvk.fs.vatsim.map.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import java.nio.file.Path;

public class PathsModule extends AbstractModule {
    @Provides
    @Singleton
    public AppDirs appDirs() {
        return AppDirsFactory.getInstance();
    }

    @Provides
    @Named("appName")
    public String appName() {
        return "VATprism";
    }

    @Provides
    @Singleton
    @Named("sharedDir")
    public Path sharedDir(final AppDirs appDirs, @Named("appName") final String appName) {
        return Path.of(appDirs.getSharedDir(appName, null, null));
    }

    @Provides
    @Singleton
    @Named("siteConfigDir")
    public Path siteConfigDir(final AppDirs appDirs, @Named("appName") final String appName) {
        return Path.of(appDirs.getSiteConfigDir(appName, null, null));
    }

    @Provides
    @Singleton
    @Named("siteDataDir")
    public Path siteDataDir(final AppDirs appDirs, @Named("appName") final String appName) {
        return Path.of(appDirs.getSiteDataDir(appName, null, null));
    }

    @Provides
    @Singleton
    @Named("userCacheDir")
    public Path userCacheDir(final AppDirs appDirs, @Named("appName") final String appName) {
        return Path.of(appDirs.getUserCacheDir(appName, null, null));
    }

    @Provides
    @Singleton
    @Named("userConfigDir")
    public Path userConfigDir(final AppDirs appDirs, @Named("appName") final String appName) {
        return Path.of(appDirs.getUserConfigDir(appName, null, null));
    }

    @Provides
    @Singleton
    @Named("userLogDir")
    public Path userLogDir(final AppDirs appDirs, @Named("appName") final String appName) {
        return Path.of(appDirs.getUserLogDir(appName, null, null));
    }

    @Provides
    @Singleton
    @Named("userDataDir")
    public Path userDataDir(final AppDirs appDirs, @Named("appName") final String appName) {
        return Path.of(appDirs.getUserDataDir(appName, null, null));
    }
}