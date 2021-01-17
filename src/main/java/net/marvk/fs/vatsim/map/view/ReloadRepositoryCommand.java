package net.marvk.fs.vatsim.map.view;

import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.ImmutableObjectProperty;
import net.marvk.fs.vatsim.map.data.ReloadableRepository;

@Log4j2
public final class ReloadRepositoryCommand extends DelegateCommand {
    public ReloadRepositoryCommand(final ReloadableRepository<?> repository) {
        this(repository, null);
    }

    public ReloadRepositoryCommand(final ReloadableRepository<?> repository, final Runnable onSucceed) {
        super(() -> new ReloadRepositoryAction(repository, onSucceed), new ImmutableObjectProperty<>(true), onSucceed != null);
    }

    private static final class ReloadRepositoryAction extends Action {
        private final ReloadableRepository<?> repository;
        private final Runnable onSucceed;

        public ReloadRepositoryAction(final ReloadableRepository<?> repository, final Runnable onSucceed) {
            this.repository = repository;
            this.onSucceed = onSucceed;
        }

        @Override
        protected void action() throws Exception {
            updateProgress(0, 1);
            if (onSucceed != null) {
                log.info("Loading %s".formatted(repository.getClass().getSimpleName()));
                repository.reloadAsync(onSucceed);
            } else {
                log.info("Async Loading %s".formatted(repository.getClass().getSimpleName()));
                repository.reload();
            }
            updateProgress(1, 1);
        }
    }
}
