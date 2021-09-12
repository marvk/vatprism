package net.marvk.fs.vatsim.map.view.about;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.HostServices;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Dependency;
import net.marvk.fs.vatsim.map.data.DependencyRepository;
import net.marvk.fs.vatsim.map.data.VersionProvider;

public class AboutViewModel implements ViewModel {
    private final VersionProvider versionProvider;
    private final HostServices hostServices;
    private final DependencyRepository dependencyRepository;

    @Inject
    public AboutViewModel(final VersionProvider versionProvider, final HostServices hostServices, final DependencyRepository dependencyRepository) {
        this.versionProvider = versionProvider;
        this.hostServices = hostServices;
        this.dependencyRepository = dependencyRepository;
    }

    public ObservableList<Dependency> dependencies() {
        return dependencyRepository.list();
    }

    public String getVersion() {
        return versionProvider.getString();
    }

    public void openDependencyUrlInBrowser(final Dependency dependency) {
        if (dependency != null) {
            hostServices.showDocument(dependency.getProjectUrl());
        }
    }

    public void openIssuePage() {
        hostServices.showDocument("https://github.com/marvk/vatprism/issues/new/choose");
    }

    public void openLicensePage() {
        hostServices.showDocument("https://github.com/marvk/vatprism/blob/master/LICENSE");
    }
}
