package net.marvk.fs.vatsim.map.data;

public interface ReloadableRepository<ViewModel> extends ReadOnlyRepository<ViewModel> {
    void reload() throws RepositoryException;

    void reloadAsync(final Runnable onSucceed) throws RepositoryException;

    default void reloadAsync() throws RepositoryException {
        reloadAsync(null);
    }
}
