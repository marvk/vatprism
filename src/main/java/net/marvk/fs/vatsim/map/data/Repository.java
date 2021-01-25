package net.marvk.fs.vatsim.map.data;

public interface Repository<T> extends ReadOnlyRepository<T> {
    void create(final T filter) throws RepositoryException;

    void update(final T filter) throws RepositoryException;

    void delete(final T filter) throws RepositoryException;
}
