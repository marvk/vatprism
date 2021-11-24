package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.api.VatprismApi;
import net.marvk.fs.vatsim.map.api.VatprismApiException;
import net.marvk.fs.vatsim.map.commons.motd.MessageOfTheDay;

import java.util.Collection;
import java.util.List;

public class MotdsRepository implements ReloadableRepository<MessageOfTheDay> {
    private final VatprismApi vatprismApi;
    private final VersionProvider versionProvider;
    private final Preferences preferences;

    private final UnmodifiableObservableListWrapper<MessageOfTheDay> motds =
            new UnmodifiableObservableListWrapper<>(FXCollections.observableArrayList());

    @Inject
    public MotdsRepository(
            final VatprismApi vatprismApi,
            final VersionProvider versionProvider,
            final Preferences preferences
    ) {
        this.vatprismApi = vatprismApi;
        this.versionProvider = versionProvider;
        this.preferences = preferences;
    }

    @Override
    public ObservableList<MessageOfTheDay> list() {
        return motds.getReadOnlyList();
    }

    @Override
    public MessageOfTheDay getByKey(final String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reload() throws RepositoryException {
        try {
            updateList(loadMotds());
        } catch (final VatprismApiException e) {
            throw new RepositoryException(e);
        }
    }

    private void updateList(final Collection<MessageOfTheDay> models) {
        motds.setAll(models);
    }

    private List<MessageOfTheDay> loadMotds() throws VatprismApiException {
        return vatprismApi.messagesOfTheDay(
                versionProvider.getVersion(),
                0.0,
                0.0,
                true
        );
    }

    @Override
    public void reloadAsync(final Runnable onSucceed) {
        new Thread(() -> {
            try {
                final Collection<MessageOfTheDay> models = loadMotds();
                Platform.runLater(() -> {
                    updateList(models);
                    if (onSucceed != null) {
                        onSucceed.run();
                    }
                });
            } catch (final VatprismApiException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
