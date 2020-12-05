package net.marvk.fs.vatsim.map.view.main;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.CompositeCommand;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.SettingsScope;
import net.marvk.fs.vatsim.map.view.StatusbarScope;
import net.marvk.fs.vatsim.map.view.ToolbarScope;

@ScopeProvider({StatusbarScope.class, ToolbarScope.class, SettingsScope.class})
@Slf4j
public class MainViewModel implements ViewModel {
    private final Command loadAirports;
    private final Command loadFirs;
    private final Command loadFirbs;
    private final Command loadUirs;
    private final Command loadClients;
    private final Command loadInternationalDateLine;

    private final NotificationCenter notificationCenter;
    private static final Duration RELOAD_PERIOD = Duration.minutes(2);
    private final ScheduledService<Void> clientReloadService;

    @InjectScope
    private ToolbarScope toolbarScope;

    @Inject
    public MainViewModel(
            final AirportRepository airportRepository,
            final ClientRepository clientRepository,
            final FlightInformationRegionRepository flightInformationRegionRepository,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
            final UpperInformationRegionRepository upperInformationRegionRepository,
            final InternationalDateLineRepository internationalDateLineRepository,
            final NotificationCenter notificationCenter
    ) {
        this.loadAirports = new ReloadRepositoryAction(airportRepository).asCommand();
        this.loadInternationalDateLine = new ReloadRepositoryAction(internationalDateLineRepository).asCommand();
        this.loadFirbs = new ReloadRepositoryAction(flightInformationRegionBoundaryRepository).asCommand();
        this.loadFirs = new ReloadRepositoryAction(flightInformationRegionRepository).asCommand();
        this.loadUirs = new ReloadRepositoryAction(upperInformationRegionRepository).asCommand();
        this.loadClients = new ReloadRepositoryAction(clientRepository).asCommand();
        this.notificationCenter = notificationCenter;

        new CompositeCommand(
                loadAirports,
                loadInternationalDateLine,
                loadFirbs,
                loadFirs,
                loadUirs,
                loadClients
        ).execute();

        notificationCenter.subscribe("RELOAD_CLIENTS", (key, payload) -> {
            loadClients.execute();
            triggerRepaint();
        });

        clientReloadService = new ReloadService(loadClients);
        clientReloadService.setPeriod(RELOAD_PERIOD);
        clientReloadService.setDelay(RELOAD_PERIOD);
        clientReloadService.setOnSucceeded(e -> triggerRepaint());
    }

    private void triggerRepaint() {
        notificationCenter.publish("REPAINT");
    }

    public void initialize() {
        toolbarScope.autoReloadProperty().addListener((observable, oldValue, newValue) -> setServiceRunning(newValue));
    }

    private void setServiceRunning(final boolean running) {
        if (running) {
            if (!clientReloadService.isRunning()) {
                log.info("Starting reload service");
                clientReloadService.start();
            }
        } else {
            log.info("Resetting reload service");
            clientReloadService.reset();
        }
    }

    private static final class ReloadRepositoryAction extends Action {
        private final Repository<?> repository;

        private ReloadRepositoryAction(final Repository<?> repository) {
            this.repository = repository;
        }

        @Override
        protected void action() throws RepositoryException {
            repository.reload();
        }

        private DelegateCommand asCommand() {
            return new DelegateCommand(() -> this, false);
        }
    }

    private static class ReloadService extends ScheduledService<Void> {
        private final Command command;

        public ReloadService(final Command command) {
            this.command = command;
        }

        @Override
        protected Task<Void> createTask() {
            return new ReloadTask(command);
        }

        private static class ReloadTask extends Task<Void> {
            private final Command command;

            public ReloadTask(final Command command) {
                this.command = command;
            }

            @Override
            protected Void call() {
                Platform.runLater(this.command::execute);
                return null;
            }
        }
    }
}
