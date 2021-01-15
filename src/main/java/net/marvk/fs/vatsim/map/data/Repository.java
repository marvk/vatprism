package net.marvk.fs.vatsim.map.data;

public interface Repository<T> extends ReadOnlyRepository<T> {
    void create(final Filter filter) throws RepositoryException;

    void update(final Filter filter) throws RepositoryException;

    void delete(final Filter filter) throws RepositoryException;
}
