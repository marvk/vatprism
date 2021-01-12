package net.marvk.fs.vatsim.map.view.about;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.HostServices;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Dependency;
import net.marvk.fs.vatsim.map.data.DependencyRepository;

public class AboutViewModel implements ViewModel {
    private final HostServices hostServices;
    private final DependencyRepository dependencyRepository;

    @Inject
    public AboutViewModel(final HostServices hostServices, final DependencyRepository dependencyRepository) {
        this.hostServices = hostServices;
        this.dependencyRepository = dependencyRepository;
    }

    public ObservableList<Dependency> dependencies() {
        return dependencyRepository.list();
    }

    public String getVersion() {
        final String version = getClass().getPackage().getImplementationVersion();

        if (version == null || version.isBlank()) {
            return null;
        }

        return version;
    }

    public void openDependencyUrlInBrowser(final Dependency dependency) {
        if (dependency != null) {
            hostServices.showDocument(dependency.getProjectUrl());
        }
    }
}
